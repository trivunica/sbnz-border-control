import { Injectable } from '@angular/core';
import { BorderCrossingResult, BorderCrossingRequest } from '../models';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';

@Injectable({ providedIn: 'root' })
export class PdfReportService {

    async generateReport(
        result: BorderCrossingResult,
        request: BorderCrossingRequest
    ): Promise<void> {
        const html = this.buildHtml(result, request);
        const plate   = request.vehicleRegistration?.registrationNumber ?? 'potvrda';
        const dateStr = new Date().toISOString().slice(0, 10);

        const container = document.createElement('div');
        container.style.position = 'fixed';
        container.style.top = '0';
        container.style.left = '0';
        container.style.width = '794px'; // A4
        container.style.zIndex = '-9999';
        container.style.pointerEvents = 'none';
        container.style.transform = 'translateX(-9999px)';
        container.innerHTML = html;
        document.body.appendChild(container);

        try {
            const canvas = await html2canvas(container, { scale: 2, useCORS: true });

            const pdf = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
            const margin = 10;
            const pageW = pdf.internal.pageSize.getWidth();
            const pageH = pdf.internal.pageSize.getHeight();
            const printW = pageW - margin * 2;
            const printH = pageH - margin * 2;

            const scale = canvas.width / printW;
            const pageHeightPx = Math.floor(printH * scale);

            let offsetPx = 0;
            let pageNum  = 0;

            while (offsetPx < canvas.height) {
                if (pageNum > 0) pdf.addPage();

                const sliceH = Math.min(pageHeightPx, canvas.height - offsetPx);

                const sliceCanvas = document.createElement('canvas');
                sliceCanvas.width = canvas.width;
                sliceCanvas.height = sliceH;
                const ctx = sliceCanvas.getContext('2d')!;
                ctx.drawImage(canvas, 0, offsetPx, canvas.width, sliceH, 0, 0, canvas.width, sliceH);

                const imgData = sliceCanvas.toDataURL('image/png');
                const sliceHmm = sliceH / scale;

                pdf.addImage(imgData, 'PNG', margin, margin, printW, sliceHmm);

                offsetPx += sliceH;
                pageNum++;
            }

            pdf.save(`granična-kontrola_${plate}_${dateStr}.pdf`);
        } finally {
            document.body.removeChild(container);
        }
    }

    private buildHtml(r: BorderCrossingResult, req: BorderCrossingRequest): string {
        const now = new Date();
        const dateStr = now.toLocaleDateString('sr-RS');
        const timeStr = now.toLocaleTimeString('sr-RS');
        const rec = r.finalDecision?.recommendation ?? 'NEPOZNATO';
        const plate = req.vehicleRegistration?.registrationNumber ?? '-';
        const driver = `${req.driver?.name ?? ''} ${req.driver?.surname ?? ''}`.trim() || '-';
        const citizenship = req.driver?.citizenship ?? '-';
        const licence = req.drivingLicence?.licenceNumber ?? '-';
        const origin = req.cmrDocument?.originCountry ?? '-';
        const dest = req.cmrDocument?.destinationCountry ?? '-';
        const company = req.cmrDocument?.senderIdentity ?? '-';
        const totalFine = r.finalDecision?.totalFine ?? 0;

        const recLabels: Record<string, string> = {
            ALLOW: 'PROPUŠTENO',
            HOLD_FINE: 'ZADRŽANO - NAPLATA KAZNE',
            FORBID_ENTRY: 'ZABRANA PRELASKA',
            ARREST: 'HAPŠENJE',
        };
        const recLabel = recLabels[rec] ?? rec;

        const regularViolations = (r.violations ?? []).filter(
            v => v.type !== 'HIGH_RISK_ENTITY' && v.type !== 'MEDIUM_RISK_ENTITY'
        );
        const riskViolation = (r.violations ?? []).find(
            v => v.type === 'HIGH_RISK_ENTITY' || v.type === 'MEDIUM_RISK_ENTITY'
        );

        const violationsHtml = regularViolations.map(v => `
            <tr class="${v.canContinue ? '' : 'row-blocking'}">
                <td>${this.violationLabel(v.type)}</td>
                <td>${v.legalBasis}</td>
                <td class="right">${v.fineAmount > 0 ? v.fineAmount.toFixed(2) + ' BAM' : '-'}</td>
                <td class="center">${v.canContinue ? 'Da' : '<strong>Ne</strong>'}</td>
            </tr>
            <tr class="exp-row">
                <td colspan="4" class="exp-cell">${v.explanation}</td>
            </tr>
        `).join('');

        const bcSectionHtml = this.buildBcSection(r, riskViolation);

        return `<!DOCTYPE html>
<html lang="sr">
<head>
<meta charset="UTF-8"/>
<title>Potvrda o graničnoj kontroli - ${plate}</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: 'Times New Roman', serif; font-size: 12px; color: #111; padding: 20mm 20mm 15mm; }

  .header { display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 2px solid #111; padding-bottom: 8px; margin-bottom: 16px; }
  .header-left h1 { font-size: 16px; font-weight: bold; text-transform: uppercase; }
  .header-left h2 { font-size: 11px; font-weight: normal; margin-top: 2px; }
  .header-right { text-align: right; font-size: 10px; color: #444; }
  .doc-number { font-size: 13px; font-weight: bold; color: #111; }

  .decision-banner { padding: 10px 16px; margin-bottom: 16px; border-radius: 4px; font-size: 15px; font-weight: bold; text-transform: uppercase; letter-spacing: 0.05em; text-align: center; }
  .banner-forbid  { background: #fee2e2; border: 2px solid #dc2626; color: #dc2626; }
  .banner-arrest  { background: #1e1b4b; border: 2px solid #1e1b4b; color: #fff; }
  .banner-hold    { background: #fef9c3; border: 2px solid #ca8a04; color: #92400e; }
  .banner-allow   { background: #dcfce7; border: 2px solid #16a34a; color: #166534; }

  .section { margin-bottom: 14px; }
  .section-title { font-size: 11px; font-weight: bold; text-transform: uppercase; letter-spacing: 0.06em; color: #444; border-bottom: 1px solid #ccc; padding-bottom: 3px; margin-bottom: 8px; }

  table { width: 100%; border-collapse: collapse; }
  td, th { padding: 5px 8px; font-size: 11px; vertical-align: top; }
  th { background: #f3f4f6; font-weight: bold; text-align: left; border-bottom: 1px solid #ddd; }
  tr { border-bottom: 1px solid #eee; }
  .label-cell { width: 30%; font-weight: bold; color: #444; }
  .right { text-align: right; }
  .center { text-align: center; }

  .row-blocking { background: #fef2f2; }
  .exp-row td { padding: 3px 8px 8px; font-size: 10px; color: #555; border-bottom: 1px solid #eee; }
  .exp-cell { font-style: italic; }
  .total-row td { font-weight: bold; background: #f9fafb; }

  .bc-section { border: 1.5px solid #1d4ed8; border-radius: 4px; padding: 10px 12px; margin-bottom: 14px; background: #eff6ff; }
  .bc-section .section-title { color: #1d4ed8; border-color: #93c5fd; }
  .bc-score-row { display: flex; align-items: center; gap: 16px; margin-bottom: 8px; }
  .bc-score-badge { font-size: 18px; font-weight: bold; color: #1d4ed8; }
  .bc-score-badge.high { color: #dc2626; }
  .bc-score-badge.medium { color: #ca8a04; }
  .bc-score-badge.low { color: #16a34a; }
  .bc-level { font-size: 13px; font-weight: bold; }
  .bc-chain { font-family: monospace; font-size: 10px; background: #dbeafe; padding: 6px 8px; border-radius: 3px; margin: 6px 0; word-break: break-all; line-height: 1.5; }
  .bc-factors { margin-top: 6px; }
  .bc-factor { font-size: 11px; padding: 2px 0; color: #1e3a5f; }
  .bc-factor::before { content: "• "; }
  .bc-risk-violation { margin-top: 8px; padding: 6px 8px; background: #fee2e2; border-radius: 3px; font-size: 11px; color: #7f1d1d; }

  .signatures { display: flex; justify-content: space-between; margin-top: 24px; }
  .sig-block { text-align: center; width: 40%; }
  .sig-line { border-top: 1px solid #111; margin-top: 32px; padding-top: 4px; font-size: 10px; }

  .footer { border-top: 1px solid #ccc; padding-top: 6px; margin-top: 20px; font-size: 9px; color: #777; text-align: center; }

  @media print {
    body { padding: 0; }
    @page { size: A4; margin: 20mm; }
  }
</style>
</head>
<body>

<div class="header">
  <div class="header-left">
    <h1>Uprava za indirektno oporezivanje</h1>
    <h2>Granična policija - Potvrda o graničnoj kontroli</h2>
  </div>
  <div class="header-right">
    <div class="doc-number">POTVRDA BR. GK-${Date.now()}</div>
    <div>Datum: ${dateStr}</div>
    <div>Vreme: ${timeStr}</div>
  </div>
</div>

<div class="decision-banner ${this.bannerClass(rec)}">
  ${recLabel}${totalFine > 0 ? ` - ukupna kazna: ${totalFine.toFixed(2)} BAM` : ''}
</div>

<div class="section">
  <div class="section-title">Podaci o vozaču i vozilu</div>
  <table>
    <tr><td class="label-cell">Vozač</td><td>${driver}</td>
        <td class="label-cell">Registarski broj</td><td>${plate}</td></tr>
    <tr><td class="label-cell">Državljanstvo</td><td>${citizenship}</td>
        <td class="label-cell">Broj vozačke dozvole</td><td>${licence}</td></tr>
    <tr><td class="label-cell">Kompanija</td><td>${company}</td>
        <td class="label-cell">Ruta</td><td>${origin} → ${dest}</td></tr>
  </table>
</div>

${bcSectionHtml}

${regularViolations.length > 0 ? `
<div class="section">
  <div class="section-title">Utvrđena kršenja</div>
  <table>
    <thead>
      <tr>
        <th>Vrsta kršenja</th><th>Pravni osnov</th>
        <th class="right">Kazna (BAM)</th><th class="center">Može nastaviti</th>
      </tr>
    </thead>
    <tbody>
      ${violationsHtml}
      ${totalFine > 0 ? `<tr class="total-row"><td colspan="2"><strong>UKUPNO</strong></td><td class="right">${totalFine.toFixed(2)} BAM</td><td></td></tr>` : ''}
    </tbody>
  </table>
</div>` : `
<div class="section">
  <div class="section-title">Utvrđena kršenja</div>
  <p style="padding:8px;color:#166534;font-weight:bold">Nema utvrđenih kršenja.</p>
</div>`}

${r.finalDecision?.requiresDriverReplacement
            ? '<p style="margin:8px 0;font-weight:bold">Potrebna zamena vozača.</p>' : ''}
${r.finalDecision?.requiresCargoOffLoad
            ? '<p style="margin:8px 0;font-weight:bold">Potreban istovar viška tereta.</p>' : ''}

<div class="signatures">
  <div class="sig-block"><div class="sig-line">Granični službenik</div></div>
  <div class="sig-block"><div class="sig-line">Potpis vozača</div></div>
</div>

<div class="footer">
  Generisano automatski od strane ekspertskog sistema za kontrolu teretnih vozila na graničnom prelazu. 
  Žalba se podnosi u roku od 8 dana od dana izdavanja.
</div>

</body>
</html>`;
    }

    private buildBcSection(r: BorderCrossingResult, riskViolation: any): string {
        const ra = r.riskAssessment;
        if (!ra && !riskViolation) return '';

        const score     = ra?.riskScore ?? 0;
        const level     = ra?.riskLevel ?? 'LOW';
        const levelLabel: Record<string, string> = {
            HIGH: 'VISOK RIZIK', MEDIUM: 'SREDNJI RIZIK', LOW: 'NIZAK RIZIK'
        };
        const levelColor: Record<string, string> = {
            HIGH: '#dc2626', MEDIUM: '#ca8a04', LOW: '#16a34a'
        };
        const color = levelColor[level] ?? '#1d4ed8';

        const treeHtml = ra?.explanationTree
            ? this.buildTreeSvg(ra.explanationTree)
            : '';

        return `
    <div class="bc-section">
      <div class="section-title">Procena rizika</div>
      <div class="bc-score-row">
        <span class="bc-score-badge" style="color:${color}; font-size:22px;">${score} / 125</span>
        <span class="bc-level" style="color:${color};">${levelLabel[level] ?? level}</span>
      </div>
      ${treeHtml ? `<div style="margin-top:10px;">${treeHtml}</div>` : ''}
    </div>`;
    }


    private buildTreeSvg(root: any): string {
        const NODE_COLORS: Record<string, string> = {
            DECISION:    '#1e3a5f',
            ENTITY:      '#1d4ed8',
            RISK_FACTOR: '#dc2626',
            EVIDENCE:    '#16a34a',
        };

        interface FlatNode {
            data: any;
            depth: number;
            parentIdx: number | null;
            x: number;
            y: number;
        }

        const flat: FlatNode[] = [];
        const traverse = (node: any, depth: number, parentIdx: number | null) => {
            const idx = flat.length;
            flat.push({ data: node, depth, parentIdx, x: 0, y: 0 });
            for (const child of (node.children ?? [])) {
                traverse(child, depth + 1, idx);
            }
        };
        traverse(root, 0, null);

        const SVG_W = 730;
        const ROW_H = 42;
        const INDENT = 22;
        const NODE_H = 32;
        const MARGIN_L = 8;
        const totalH = flat.length * ROW_H + 10;

        flat.forEach((n, i) => {
            n.x = n.depth * INDENT + MARGIN_L;
            n.y = i * ROW_H + 5;
        });

        const lines = flat
            .filter(n => n.parentIdx !== null)
            .map(n => {
                const p = flat[n.parentIdx!];
                const px = p.x + 10;
                const py = p.y + NODE_H / 2;
                const cy = n.y + NODE_H / 2;
                const cx = n.x;
                return `<path d="M${px},${py} V${cy} H${cx}" fill="none" stroke="#94a3b8" stroke-width="1.5"/>`;
            })
            .join('\n');

        const nodes = flat.map(n => {
            const color = NODE_COLORS[n.data.type] ?? '#64748b';
            const rectW = SVG_W - n.x - 16;
            const label = (n.data.label ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
            const detail = n.data.detail && n.data.type === 'RISK_FACTOR'
                ? `<tspan font-size="9" fill="#666" dx="6">${n.data.detail.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')}</tspan>`
                : '';
            const scoreText = n.data.score > 0 ? `+${n.data.score}` : '';
            const textY = n.y + NODE_H / 2 + 4;

            return `
              <rect x="${n.x}" y="${n.y}" width="${rectW}" height="${NODE_H}"
                    rx="4" ry="4" fill="white" stroke="${color}" stroke-width="1.5"/>
              <text x="${n.x + 8}" y="${textY}" font-family="Times New Roman, serif"
                    font-size="10" fill="#111" dominant-baseline="auto">
                <tspan font-weight="600">${label}</tspan>${detail}
              </text>
              ${scoreText ? `<text x="${n.x + rectW - 8}" y="${textY}" font-family="Times New Roman, serif"
                    font-size="10" font-weight="700" fill="${color}" text-anchor="end"
                    dominant-baseline="auto">${scoreText}</text>` : ''}`;
                    }).join('\n');

                    return `<svg xmlns="http://www.w3.org/2000/svg"
                 viewBox="0 0 ${SVG_W} ${totalH}"
                 width="${SVG_W}" height="${totalH}"
                 style="display:block; overflow:visible;">
              ${lines}
              ${nodes}
            </svg>`;
        }


    private bannerClass(rec: string): string {
        const map: Record<string, string> = {
            ALLOW: 'banner-allow', HOLD_FINE: 'banner-hold',
            FORBID_ENTRY: 'banner-forbid', ARREST: 'banner-arrest',
        };
        return map[rec] ?? 'banner-hold';
    }

    private violationLabel(type: string): string {
        const map: Record<string, string> = {
            EXPIRED_DRIVING_LICENCE: 'Istekla vozačka dozvola',
            INVALID_DRIVING_CATEGORY: 'Nevalidna kategorija dozvole',
            EXPIRED_ID_CARD: 'Istekla lična karta',
            EXPIRED_PASSPORT: 'Istekao pasoš',
            PASSPORT_SHORT_VALIDITY: 'Kratka preostala važnost pasoša',
            SUSPECTED_FORGERY: 'Sumnja na falsifikat',
            STOLEN_LOST_DOCUMENT: 'Prijavljeni nestali dokument',
            MISSING_VISA_SUPPLEMENT: 'Nedostaje viza/dopunska isprava',
            INSUFFICIENT_FUNDS: 'Nedovoljno finansijskih sredstava',
            TRAILER_OVERLOAD: 'Preopterećenje prikolice',
            TOTAL_WEIGHT_OVERLOAD: 'Ukupno preopterećenje',
            MISSING_THIRD_COUNTRY_PERMIT: 'Nedostaje dozvola za treću zemlju',
            INTERPOL_WARRANT: 'Interpol potraga',
            DOMESTIC_WARRANT: 'Domaća potraga',
            EXPIRED_REGISTRATION: 'Istekla registracija vozila',
            PENDING_WEIGHT_CHECK: 'Potrebna provera težine',
            MISSING_ADR_CERTIFICATE: 'Nedostaje ADR sertifikat',
            MISSING_VETERINARY_CERTIFICATE: 'Nedostaje veterinarski sertifikat',
            MISSING_ATP_CERTIFICATE: 'Nedostaje ATP sertifikat',
            MISSING_WASTE_TRANSPORT_CERTIFICATE: 'Nedostaje sertifikat za otpad',
            MISSING_PHARMACEUTICAL_CERTIFICATE: 'Nedostaje farmaceutski sertifikat',
            MISSING_RADIOACTIVE_CERTIFICATE: 'Nedostaje sertifikat za radioaktivne materije',
            MISSING_EXPLOSIVES_CERTIFICATE: 'Nedostaje sertifikat za eksplozive',
            BILATERAL_THIRD_COUNTRY_VIOLATION: 'Kršenje bilateralne dozvole',
            BILATERAL_WEIGHT_EXCEEDED: 'Prekoračenje bilateralne težine',
            INSUFFICIENT_DRIVING_EXPERIENCE: 'Nedovoljno iskustvo vozača',
            HIGH_RISK_ENTITY: 'Visokorizični entitet',
            MEDIUM_RISK_ENTITY: 'Srednje rizični entitet',
        };
        return map[type] ?? type;
    }
}