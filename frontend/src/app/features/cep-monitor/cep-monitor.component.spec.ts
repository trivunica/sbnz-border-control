import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CepMonitorComponent } from './cep-monitor.component';

describe('CepMonitorComponent', () => {
    let component: CepMonitorComponent;
    let fixture: ComponentFixture<CepMonitorComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [CepMonitorComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(CepMonitorComponent);
        component = fixture.componentInstance;
        await fixture.whenStable();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
