import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SuggestionsService } from '@dotcms/block-editor';

import { DotBlockEditorComponent } from './dot-block-editor.component';

describe('DotBlockEditorComponent', () => {
    let component: DotBlockEditorComponent;
    let fixture: ComponentFixture<DotBlockEditorComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DotBlockEditorComponent],
            providers: [SuggestionsService]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(DotBlockEditorComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
