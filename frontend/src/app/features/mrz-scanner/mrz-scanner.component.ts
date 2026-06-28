import {
    Component, Output, EventEmitter, ViewChild,
    ElementRef, OnDestroy, signal, computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MrzScannerService } from '../../core/services/mrz-scanner.service';
import { MrzResult } from '../../core/models';

type Mode = 'upload' | 'camera';

@Component({
    selector: 'app-mrz-scanner',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './mrz-scanner.component.html',
    styleUrls: ['./mrz-scanner.component.scss'],
})
export class MrzScannerComponent implements OnDestroy {
    @Output() mrzScanned = new EventEmitter<MrzResult>();
    @ViewChild('videoEl') videoRef!: ElementRef<HTMLVideoElement>;
    @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

    mode = signal<Mode>('upload');
    scanning = signal(false);
    progress = signal(0);
    error = signal<string | null>(null);
    result = signal<MrzResult | null>(null);
    cameraActive = signal(false);

    private stream: MediaStream | null = null;

    isExpired = computed(() => {
        const r = this.result();
        if (!r) return false;
        return new Date(r.expiryDate) < new Date();
    });

    constructor(private scanner: MrzScannerService) {}

    ngOnDestroy(): void {
        this.stopCamera();
    }

    selectMode(m: Mode): void {
        if (m === 'camera') { this.startCamera(); return; }
        this.stopCamera();
        this.mode.set('upload');
    }

    triggerFileInput(): void {
        this.fileInputRef.nativeElement.click();
    }

    onDragOver(e: DragEvent): void { e.preventDefault(); }

    onDrop(e: DragEvent): void {
        e.preventDefault();
        const file = e.dataTransfer?.files[0];
        if (file) this.processFile(file);
    }

    onFileSelected(e: Event): void {
        const file = (e.target as HTMLInputElement).files?.[0];
        if (file) this.processFile(file);
        (e.target as HTMLInputElement).value = '';
    }

    private async processFile(file: File): Promise<void> {
        this.scanning.set(true);
        this.error.set(null);
        this.result.set(null);
        this.progress.set(0);
        try {
            const r = await this.scanner.scanFile(file, p => this.progress.set(p));
            this.result.set(r);
        } catch (err: any) {
            this.error.set(err.message ?? 'Greška pri skeniranju.');
        } finally {
            this.scanning.set(false);
        }
    }

    private async startCamera(): Promise<void> {
        this.mode.set('camera');
        this.error.set(null);
        try {
            this.stream = await this.scanner.startCamera();
            this.cameraActive.set(true);
            setTimeout(() => {
                if (this.videoRef?.nativeElement) {
                    this.videoRef.nativeElement.srcObject = this.stream;
                }
            }, 100);
        } catch {
            this.error.set('Kamera nije dostupna. Proveri dozvole u browseru.');
            this.mode.set('upload');
        }
    }

    private stopCamera(): void {
        if (this.stream) { this.scanner.stopCamera(this.stream); this.stream = null; }
        this.cameraActive.set(false);
    }

    async captureFromCamera(): Promise<void> {
        const video = this.videoRef?.nativeElement;
        if (!video) return;
        this.scanning.set(true);
        this.error.set(null);
        this.result.set(null);
        try {
            const canvas = this.scanner.captureFrame(video);
            const r = await this.scanner.scanCanvas(canvas, p => this.progress.set(p));
            this.result.set(r);
            this.stopCamera();
            this.mode.set('upload');
        } catch (err: any) {
            this.error.set(err.message ?? 'Greška pri skeniranju.');
        } finally {
            this.scanning.set(false);
        }
    }

    applyToForm(): void {
        const r = this.result();
        if (r) this.mrzScanned.emit(r);
    }

    reset(): void {
        this.result.set(null);
        this.error.set(null);
        this.progress.set(0);
    }
}
