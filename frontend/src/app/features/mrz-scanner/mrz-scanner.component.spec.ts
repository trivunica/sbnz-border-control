import { ComponentFixture, TestBed } from '@angular/core/testing';
import {MrzScannerComponent} from "./mrz-scanner.component";


describe('MrzScanner', () => {
    let component: MrzScannerComponent;
    let fixture: ComponentFixture<MrzScannerComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [MrzScannerComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(MrzScannerComponent);
        component = fixture.componentInstance;
        await fixture.whenStable();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
