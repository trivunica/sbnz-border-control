import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BorderCrossingComponent } from './border-crossing.component';

describe('BorderCrossing', () => {
    let component: BorderCrossingComponent;
    let fixture: ComponentFixture<BorderCrossingComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [BorderCrossingComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(BorderCrossingComponent);
        component = fixture.componentInstance;
        await fixture.whenStable();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
