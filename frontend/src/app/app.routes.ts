import { Routes } from '@angular/router';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('./features/border-crossing/border-crossing.component').then(
                m => m.BorderCrossingComponent
            ),
    },
    {
        path: 'cep',
        loadComponent: () =>
            import('./features/cep-monitor/cep-monitor.component').then(
                m => m.CepMonitorComponent
            ),
    },
    { path: '**', redirectTo: '' },
];
