import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { BorderCrossingService } from '../../core/services/border-crossing.service';
import { CepAlarm } from '../../core/models';
import { AlarmCountPipe } from '../../core/pipes/alarm-count.pipe';

@Component({
    selector: 'app-cep-monitor',
    standalone: true,
    imports: [CommonModule, RouterLink, AlarmCountPipe],
    templateUrl: './cep-monitor.component.html',
    styleUrls: ['./cep-monitor.component.scss'],
})
export class CepMonitorComponent implements OnInit, OnDestroy {
    alarms = signal<CepAlarm[]>([]);
    filterPlate = signal('');
    private pollSub?: Subscription;

    constructor(private service: BorderCrossingService) {}

    ngOnInit(): void {
        this.loadAlarms();
        this.pollSub = interval(10_000)
            .pipe(switchMap(() => this.service.getAlarms()))
            .subscribe(alarms => this.alarms.set(alarms));
    }

    ngOnDestroy(): void {
        this.pollSub?.unsubscribe();
    }

    loadAlarms(): void {
        this.service.getAlarms().subscribe({
            next: a => this.alarms.set(a),
            error: () => {},
        });
    }

    filteredAlarms(): CepAlarm[] {
        const f = this.filterPlate().trim().toLowerCase();
        if (!f) return this.alarms();
        return this.alarms().filter(a => a.plateNumber?.toLowerCase().includes(f));
    }

    alarmsOfType(keyword: string): CepAlarm[] {
        return this.filteredAlarms().filter(a =>
            a.type?.toUpperCase().includes(keyword.toUpperCase())
        );
    }

    formatTime(ts?: string): string {
        if (!ts) return '';
        return new Date(ts).toLocaleTimeString('sr-Latn', { hour: '2-digit', minute: '2-digit' });
    }
}