import {Injectable} from '@angular/core';
import {MrzResult} from '../models';

@Injectable({ providedIn: 'root' })
export class MrzParserService {
    private readonly WEIGHTS = [7, 3, 1];
    private readonly CHAR_VALUES: Record<string, number> = {
        '<': 0, '0': 0, '1': 1, '2': 2, '3': 3, '4': 4,
        '5': 5, '6': 6, '7': 7, '8': 8, '9': 9,
        'A': 10, 'B': 11, 'C': 12, 'D': 13, 'E': 14, 'F': 15,
        'G': 16, 'H': 17, 'I': 18, 'J': 19, 'K': 20, 'L': 21,
        'M': 22, 'N': 23, 'O': 24, 'P': 25, 'Q': 26, 'R': 27,
        'S': 28, 'T': 29, 'U': 30, 'V': 31, 'W': 32, 'X': 33,
        'Y': 34, 'Z': 35,
    };

    parseLines(lines: string[]): MrzResult | null {
        if (!lines || lines.length === 0) return null;

        // looking for TD3 format (passport - 2 consecutive lines of ~44 characters)
        const td3Lines = lines.filter(l => l.length >= 38 && l.length <= 48);
        for (let i = 0; i < td3Lines.length - 1; i++) {
            let l1 = td3Lines[i];
            let l2 = td3Lines[i + 1];

            // length normalization to the standard 44 characters for passports
            if (l1.length < 44) l1 = l1.padEnd(44, '<');
            if (l2.length < 44) l2 = l2.padEnd(44, '<');

            // passport always starts with the letter 'P'
            if (l1[0] === 'P') {
                try {
                    return this.parseTd3(l1.substring(0, 44), l2.substring(0, 44));
                } catch (e) {
                    console.error('Error parsing passport (TD3):', e);
                }
            }
        }

        // looking for TD1 format (id card - 3 consecutive lines of ~30 characters)
        const td1Lines = lines.filter(l => l.length >= 25 && l.length <= 36);
        for (let i = 0; i < td1Lines.length - 2; i++) {
            let l1 = td1Lines[i];
            let l2 = td1Lines[i + 1];
            let l3 = td1Lines[i + 2];

            // length normalization to the standard 30 characters for id cards
            if (l1.length < 30) l1 = l1.padEnd(30, '<');
            if (l2.length < 30) l2 = l2.padEnd(30, '<');
            if (l3.length < 30) l3 = l3.padEnd(30, '<');

            // id cards usually start with letters 'I', 'C', 'A'
            if (['I', 'C', 'A'].includes(l1[0]) || l1.length === 30) {
                try {
                    return this.parseTd1(l1.substring(0, 30), l2.substring(0, 30), l3.substring(0, 30));
                } catch (e) {
                    console.error('Error parsing id card (TD1):', e);
                }
            }
        }

        return null;
    }

    private parseTd3(line1: string, line2: string): MrzResult {
        const issuingCountry = line1.substring(2, 5).replace(/</g, '');

        // name and surname in passport (first line, from index 5 to the end)
        const nameField = line1.substring(5, 44);
        const [surnameRaw, givenRaw] = nameField.split('<<');
        const surname = (surnameRaw ?? '').replace(/</g, ' ').trim();
        const givenNames = (givenRaw ?? '').replace(/</g, ' ').trim();

        // second line data
        const documentNumber = line2.substring(0, 9).replace(/</g, '');
        const docNumCheckDigit = parseInt(line2[9], 10);

        const nationality = line2.substring(10, 13).replace(/</g, '');

        // dates
        let dobRaw = line2.substring(13, 19).replace(/[OIT]/g, '0');
        const dobCheckDigit = parseInt(line2[19], 10);

        const sex = line2.substring(20, 21);

        let expiryRaw = line2.substring(21, 27).replace(/[OIT]/g, '0');
        const expiryCheckDigit = parseInt(line2[27], 10);

        // control digits validation
        const isDocNumValid = this.checkDigit(documentNumber) === docNumCheckDigit;
        const isDobValid = this.checkDigit(dobRaw) === dobCheckDigit;
        const isExpiryValid = this.checkDigit(expiryRaw) === expiryCheckDigit;

        return {
            docType: 'PASSPORT',
            surname,
            givenNames,
            documentNumber,
            nationality,
            issuingCountry,
            dateOfBirth: this.parseDate(dobRaw, true),
            expiryDate: this.parseDate(expiryRaw, false),
            sex,
            checkDigitsValid: isDocNumValid && isDobValid && isExpiryValid,
            rawLines: [line1, line2],
        };
    }

    private parseTd1(line1: string, line2: string, line3: string): MrzResult {
        const issuingCountry = line1.substring(2, 5).replace(/</g, '');

        let docNumRaw = line1.substring(5, 14);
        let docNumPart1 = docNumRaw.substring(0, 6).replace(/T/g, '0');
        let docNumPart2 = docNumRaw.substring(6);
        const documentNumber = (docNumPart1 + docNumPart2).replace(/</g, '');

        let docNumCheckChar = line1[14];
        if (docNumCheckChar === 'T') docNumCheckChar = '0';
        const docNumCheckDigit = parseInt(docNumCheckChar, 10);

        let dobRaw = line2.substring(0, 6).replace(/[OHIT]/g, '0');
        const dobCheckDigit = parseInt(line2[6], 10);

        const sex = line2.substring(7, 8);

        let expiryRaw = line2.substring(8, 14).replace(/[OHIT]/g, '0');
        const expiryCheckDigit = parseInt(line2[14], 10);

        const nationality = line2.substring(15, 18).replace(/</g, '');

        const [surnameRaw, givenRaw] = line3.split('<<');
        const surname = (surnameRaw ?? '').replace(/</g, ' ').trim();
        const givenNames = (givenRaw ?? '').replace(/</g, ' ').trim();

        const isDocNumValid = this.checkDigit(documentNumber) === docNumCheckDigit;
        const isDobValid = this.checkDigit(dobRaw) === dobCheckDigit;
        const isExpiryValid = this.checkDigit(expiryRaw) === expiryCheckDigit;

        return {
            docType: 'ID_CARD',
            surname,
            givenNames,
            documentNumber,
            nationality,
            issuingCountry,
            dateOfBirth: this.parseDate(dobRaw, true),
            expiryDate: this.parseDate(expiryRaw, false),
            sex,
            checkDigitsValid: isDocNumValid && isDobValid && isExpiryValid,
            rawLines: [line1, line2, line3],
        };
    }

    private parseDate(raw: string, isBirth: boolean): string {
        if (!raw || raw.length < 6) return '';
        const yy = parseInt(raw.substring(0, 2), 10);
        const mm = raw.substring(2, 4);
        const dd = raw.substring(4, 6);
        let yyyy: number;
        if (isBirth) {
            yyyy = yy <= new Date().getFullYear() % 100 ? 2000 + yy : 1900 + yy;
        } else {
            yyyy = yy < 30 ? 2000 + yy : 1900 + yy;
        }
        return `${yyyy}-${mm}-${dd}`;
    }

    private checkDigit(value: string): number {
        return (
            value
                .split('')
                .reduce(
                    (sum, ch, i) => sum + (this.CHAR_VALUES[ch] ?? 0) * this.WEIGHTS[i % 3],
                    0
                ) % 10
        );
    }
}