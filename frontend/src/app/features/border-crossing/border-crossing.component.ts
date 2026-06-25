import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BorderCrossingService } from '../../core/services/border-crossing.service';
import {
    BorderCrossingRequest,
    BorderCrossingResult,
    MrzResult,
    CepAlarm,
    DriverCertificate, CrossingEvent,
} from '../../core/models';
import { MrzScannerComponent } from '../mrz-scanner/mrz-scanner.component';

@Component({
    selector: 'app-border-crossing',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink, MrzScannerComponent],
    templateUrl: './border-crossing.component.html',
    styleUrls: ['./border-crossing.component.scss'],
})
export class BorderCrossingComponent implements OnInit {
    form!: FormGroup;
    result = signal<BorderCrossingResult | null>(null);
    vehicleAlarms = signal<CepAlarm[]>([]);
    loading = signal(false);
    error = signal<string | null>(null);
    activeSection = signal(0);
    showMrzScanner = signal(false);

    readonly borderCrossings = [
        'RACA', 'POPOVI', 'SEPAK', 'BRATUNAC', 'KARAKAJ', 'SKELANI'
    ];

    readonly sections = [
        { label: 'Vozač' },
        { label: 'Dokumenti' },
        { label: 'Vozilo' },
        { label: 'Teret' },
        { label: 'Dozvole' },
        { label: 'Sertifikati' },
    ];

    constructor(private fb: FormBuilder, private service: BorderCrossingService) {}

    ngOnInit(): void {
        this.form = this.fb.group({
            driver: this.fb.group({
                name: ['', Validators.required],
                surname: ['', Validators.required],
                citizenship: ['', Validators.required],
                foreignCitizen: [false],
                interpolWarrant: [false],
                domesticWarrant: [false],
                photoMatches: [true],
                documentReportedStolen: [false],
                hasVisa: [false],
                hasSupplementaryDocument: [false],
                financialFunds: [null],
                plannedStayDays: [null],
                dob: [null],
            }),
            drivingLicence: this.fb.group({
                licenceNumber: ['', Validators.required],
                expiryDate: ['', Validators.required],
                category: ['CE', Validators.required],
            }),
            identificationDocument: this.fb.group({
                documentNumber: ['', Validators.required],
                type: ['PASSPORT', Validators.required],
                issuingCountry: ['', Validators.required],
                expiryDate: ['', Validators.required],
            }),
            vehicleRegistration: this.fb.group({
                registrationNumber: [''],
                expiryDate: [''],
                trailerCapacity: [null],
                truckWeight: [null],
            }),
            cmrDocument: this.fb.group({
                originCountry: [''],
                destinationCountry: [''],
                goodsWeight: [null],
                goodsDescription: [''],
                senderIdentity: [''],
                receiverIdentity: [''],
            }),
            transportPermits: this.fb.array([]),
            driverCertificates: this.fb.array([]),
            includeLiveWeight: [false],
            liveWeightMeasurement: this.fb.group({
                measuredTotalWeight: [null],
                visualSag: [false],
            }),
            borderCrossingId: ['RACA', Validators.required],
        });
    }

    get permits(): FormArray {
        return this.form.get('transportPermits') as FormArray;
    }

    get certificates(): FormArray {
        return this.form.get('driverCertificates') as FormArray;
    }

    addPermit(): void {
        this.permits.push(this.fb.group({
            type: ['CEMT', Validators.required],
            expiryDate: ['', Validators.required],
            coveredRoutes: ['ALL'],
        }));
    }

    removePermit(i: number): void {
        this.permits.removeAt(i);
    }


    addCertificate(): void {
        this.certificates.push(this.fb.group({
            goodsCertificate: ['ADR_CERTIFICATE', Validators.required],
            yearsExperience: [0, [Validators.required, Validators.min(0)]],
            expiryDate: ['', Validators.required],
        }));
    }

    removeCertificate(i: number): void {
        this.certificates.removeAt(i);
    }


    goTo(i: number): void {
        this.activeSection.set(i);
    }

    onMrzScanned(mrz: MrzResult): void {
        this.form.patchValue({
            driver: {
                name: mrz.givenNames,
                surname: mrz.surname,
                citizenship: mrz.nationality,
                dob: mrz.dateOfBirth,
            },
            identificationDocument: {
                documentNumber: mrz.documentNumber,
                type: mrz.docType,
                issuingCountry: mrz.issuingCountry,
                expiryDate: mrz.expiryDate,
            },
        });
        this.showMrzScanner.set(false);
        this.activeSection.set(0);
    }

    submit(): void {
        if (this.form.invalid) return;
        this.loading.set(true);
        this.error.set(null);
        this.result.set(null);
        this.vehicleAlarms.set([]);

        const v = this.form.value;

        const request: BorderCrossingRequest = {
            driver: v.driver,
            drivingLicence: v.drivingLicence,
            identificationDocument: v.identificationDocument,
        };

        if (v.vehicleRegistration?.registrationNumber) {
            request.vehicleRegistration = v.vehicleRegistration;
        }

        if (v.cmrDocument?.originCountry) {
            request.cmrDocument = v.cmrDocument;
        }

        if (v.transportPermits?.length) {
            request.transportPermits = v.transportPermits;
        }

        if (v.driverCertificates?.length) {
            request.driverCertificates = v.driverCertificates as DriverCertificate[];
        }

        if (v.includeLiveWeight && v.liveWeightMeasurement?.measuredTotalWeight) {
            request.liveWeightMeasurement = v.liveWeightMeasurement;
        }


        this.service.evaluate(request).subscribe({
            next: (res) => {
                this.result.set(res);
                this.loading.set(false);

                const plate = v.vehicleRegistration?.registrationNumber;
                const documentNumber = v.identificationDocument?.documentNumber;

                if (plate && documentNumber) {
                    const event: CrossingEvent = {
                        plateNumber: plate,
                        driverDocumentNumber: documentNumber,
                        borderCrossingId: v.borderCrossingId,
                        companyName: v.cmrDocument?.senderIdentity ?? '',
                        destinationCountry: v.cmrDocument?.destinationCountry ?? '',
                        crossedAt: new Date().toISOString(),
                        timestamp: Date.now()
                    };
                    this.service.registerCrossing(event).subscribe({
                        next: () => this.service.getAlarmsForVehicle(plate).subscribe({
                            next: alarms => this.vehicleAlarms.set(alarms),
                            error: () => {},
                        }),
                        error: () => {}
                    });
                }
            },
            error: () => {
                this.error.set('Greška pri komunikaciji sa serverom!');
                this.loading.set(false);
            },
        });
    }

    reset(): void {
        this.form.reset({
            driver: { photoMatches: true, foreignCitizen: false, interpolWarrant: false,
                domesticWarrant: false, documentReportedStolen: false,
                hasVisa: false, hasSupplementaryDocument: false },
            drivingLicence: { category: 'CE' },
            identificationDocument: { type: 'PASSPORT' },
            includeLiveWeight: false,
        });
        this.permits.clear();
        this.certificates.clear();
        this.result.set(null);
        this.vehicleAlarms.set([]);
        this.error.set(null);
        this.activeSection.set(0);
        this.showMrzScanner.set(false);
    }


    recClass(rec: string): string {
        const map: Record<string, string> = {
            ALLOW: 'rec-allow',
            HOLD_FINE: 'rec-hold',
            FORBID_ENTRY: 'rec-forbid',
            ARREST: 'rec-arrest',
        };
        return map[rec] ?? '';
    }

    recLabel(rec: string): string {
        const map: Record<string, string> = {
            ALLOW: 'Propusti',
            HOLD_FINE: 'Zadrži - naplati kaznu',
            FORBID_ENTRY: 'Zabrana prelaska',
            ARREST: 'Hapšenje',
        };
        return map[rec] ?? rec;
    }

    recIcon(rec: string): string {
        const map: Record<string, string> = {
            ALLOW: 'ti-circle-check',
            HOLD_FINE: 'ti-alert-triangle',
            FORBID_ENTRY: 'ti-ban',
            ARREST: 'ti-gavel',
        };
        return map[rec] ?? 'ti-info-circle';
    }

    validityClass(status: string): string {
        const map: Record<string, string> = {
            VALID: 'chip-valid',
            PROBLEM: 'chip-problem',
            FORBIDDEN: 'chip-forbidden',
            ARREST: 'chip-arrest',
        };
        return map[status] ?? '';
    }

    alarmClass(type: string): string {
        if (type.includes('COORDIN')) return 'alarm-coord';
        if (type.includes('FREQUENT')) return 'alarm-freq';
        return 'alarm-avoid';
    }

    isForeign(): boolean {
        return this.form.get('driver.foreignCitizen')?.value === true;
    }

    includeLiveWeight(): boolean {
        return this.form.get('includeLiveWeight')?.value === true;
    }

    hasCertificateViolation(): boolean {
        return this.result()?.violations?.some(v =>
            v.type?.includes('CERTIFICATE') || v.type === 'INSUFFICIENT_DRIVING_EXPERIENCE'
        ) ?? false;
    }

    getCertificateChipLabel(): string {
        const violations = this.result()?.violations ?? [];
        if (violations.some(v => v.type === 'INSUFFICIENT_DRIVING_EXPERIENCE')) {
            return 'NEDOVOLJNO ISKUSTVO';
        }
        if (violations.some(v => v.type?.includes('CERTIFICATE'))) {
            return 'NEDOSTAJE';
        }
        return 'OK';
    }
}