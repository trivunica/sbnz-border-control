import { Pipe, PipeTransform } from '@angular/core';
import { CepAlarm } from '../models';

@Pipe({ name: 'alarmCount', standalone: true })
export class AlarmCountPipe implements PipeTransform {
    transform(alarms: CepAlarm[], keyword: string): number {
        return alarms.filter(a => a.type?.toUpperCase().includes(keyword.toUpperCase())).length;
    }
}
