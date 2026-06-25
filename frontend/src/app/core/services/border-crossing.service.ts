import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
    BorderCrossingRequest,
    BorderCrossingResult,
    CepAlarm,
    CrossingEvent,
} from '../models';

@Injectable({ providedIn: 'root' })
export class BorderCrossingService {
    private readonly base = '/api/border';

    constructor(private http: HttpClient) {}

    evaluate(request: BorderCrossingRequest): Observable<BorderCrossingResult> {
        return this.http.post<BorderCrossingResult>(`${this.base}/evaluate`, request);
    }

    registerCrossing(event: CrossingEvent): Observable<void> {
        return this.http.post<void>(`${this.base}/crossing-event`, event);
    }

    getAlarms(): Observable<CepAlarm[]> {
        return this.http.get<CepAlarm[]>(`${this.base}/alarms`);
    }

    getAlarmsForVehicle(plateNumber: string): Observable<CepAlarm[]> {
        return this.http.get<CepAlarm[]>(
            `${this.base}/alarms/${encodeURIComponent(plateNumber)}`
        );
    }
}
