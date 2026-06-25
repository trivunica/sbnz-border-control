import { Injectable } from '@angular/core';
import { MrzParserService } from './mrz-parser.service';
import { MrzResult } from '../models';

declare const Tesseract: any;


@Injectable({ providedIn: 'root' })
export class MrzScannerService {
    private tesseractReady = false;
    private loadPromise: Promise<void> | null = null;

    constructor(private parser: MrzParserService) {}

    private loadTesseract(): Promise<void> {
        if (this.tesseractReady) return Promise.resolve();
        if (this.loadPromise) return this.loadPromise;

        this.loadPromise = new Promise((resolve, reject) => {
            if (typeof Tesseract !== 'undefined') {
                this.tesseractReady = true;
                resolve();
                return;
            }
            const script = document.createElement('script');
            script.src = 'https://cdn.jsdelivr.net/npm/tesseract.js@5/dist/tesseract.min.js';
            script.onload = () => { this.tesseractReady = true; resolve(); };
            script.onerror = () => reject(new Error('Tesseract.js se nije učitao'));
            document.head.appendChild(script);
        });

        return this.loadPromise;
    }

    async scanFile(
        file: File,
        onProgress?: (pct: number) => void
    ): Promise<MrzResult> {
        await this.loadTesseract();

        const worker = await Tesseract.createWorker('eng', 1, {
            logger: (m: any) => {
                if (m.status === 'recognizing text' && onProgress) {
                    onProgress(Math.round(m.progress * 100));
                }
            },
        });

        await worker.setParameters({
            tessedit_char_whitelist: 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789<',
            tessedit_pageseg_mode: '3',
        });

        const url = URL.createObjectURL(file);
        const { data } = await worker.recognize(url);
        URL.revokeObjectURL(url);
        await worker.terminate();

        const lines = data.text
            .split('\n')
            .map((l: string) => {
                let cleanLine = l.trim().toUpperCase().replace(/\s/g, '');

                cleanLine = cleanLine
                    .replace(/[««“’”\"„\{\}\(\)\[\]]/g, '<')
                    .replace(/≤/g, '<');

                const match = cleanLine.match(/[A-Z0-9<]+/);
                return match ? match[0] : '';
            })
            .filter((l: string) => l.length >= 25 && l.length <= 48);

        const result = this.parser.parseLines(lines);
        if (!result) {
            throw new Error('MRZ zona nije prepoznata. Proverite osvetljenje i ugao dokumenta.');
        }
        return result;
    }

    async scanCanvas(canvas: HTMLCanvasElement, onProgress?: (pct: number) => void): Promise<MrzResult> {
        return new Promise((resolve, reject) => {
            canvas.toBlob(async blob => {
                if (!blob) { reject(new Error('Canvas greška')); return; }
                try { resolve(await this.scanFile(blob as File, onProgress)); }
                catch (e) { reject(e); }
            }, 'image/jpeg', 0.95);
        });
    }

    captureFrame(video: HTMLVideoElement): HTMLCanvasElement {
        const canvas = document.createElement('canvas');
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        canvas.getContext('2d')!.drawImage(video, 0, 0);
        return canvas;
    }

    async startCamera(): Promise<MediaStream> {
        return navigator.mediaDevices.getUserMedia({
            video: { facingMode: 'environment', width: { ideal: 1280 }, height: { ideal: 720 } },
        });
    }

    stopCamera(stream: MediaStream): void {
        stream.getTracks().forEach(t => t.stop());
    }
}