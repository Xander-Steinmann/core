/* eslint-disable @typescript-eslint/no-explicit-any */
import { of } from 'rxjs';

import { Component, DebugElement, EventEmitter, Input, Output } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';

import { ConfirmationService } from 'primeng/api';

import { DotIframeService } from '@components/_common/iframe/service/dot-iframe/dot-iframe.service';
import { DotContentCompareTableComponent } from '@components/dot-content-compare/components/dot-content-compare-table/dot-content-compare-table.component';
import { dotContentCompareTableDataMock } from '@components/dot-content-compare/components/dot-content-compare-table/dot-content-compare-table.component.spec';
import { DotContentCompareModule } from '@components/dot-content-compare/dot-content-compare.module';
import { DotContentCompareStore } from '@components/dot-content-compare/store/dot-content-compare.store';
import { DotFormatDateService } from '@dotcms/app/api/services/dot-format-date-service';
import { DotAlertConfirmService, DotMessageService } from '@dotcms/data-access';
import { DotcmsConfigService } from '@dotcms/dotcms-js';
import { DotCMSContentlet } from '@dotcms/dotcms-models';
import { MockDotMessageService } from '@dotcms/utils-testing';

import {
    DotContentCompareComponent,
    DotContentCompareEvent
} from './dot-content-compare.component';

const DotContentCompareEventMOCK = {
    inode: '1',
    identifier: '2',
    language: 'es'
};

const storeMock = jasmine.createSpyObj(
    'DotContentCompareStore',
    ['loadData', 'updateShowDiff', 'updateCompare', 'bringBack'],
    {
        vm$: of({ data: dotContentCompareTableDataMock, showDiff: false })
    }
);

@Component({
    selector: 'dot-test-host-component',
    template:
        '<dot-content-compare [data]="data"  (shutdown)="shutdown.emit(true)" ></dot-content-compare>'
})
class TestHostComponent {
    @Input() data: DotContentCompareEvent;
    @Output() shutdown = new EventEmitter<boolean>();
}

describe('DotContentCompareComponent', () => {
    let hostComponent: TestHostComponent;
    let hostFixture: ComponentFixture<TestHostComponent>;
    let de: DebugElement;
    let dotContentCompareStore: DotContentCompareStore;
    let contentCompareTableComponent: DotContentCompareTableComponent;
    let dotAlertConfirmService: DotAlertConfirmService;
    let dotIframeService: DotIframeService;

    const messageServiceMock = new MockDotMessageService({
        Confirm: 'Confirm',
        'folder.replace.contentlet.working.version':
            'Are you sure you would like to replace your working version with this contentlet version?'
    });

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [DotContentCompareComponent, TestHostComponent],
            imports: [DotContentCompareModule],
            providers: [
                { provide: DotMessageService, useValue: messageServiceMock },
                DotAlertConfirmService,
                ConfirmationService,
                {
                    provide: DotIframeService,
                    useValue: {
                        run: jest.fn()
                    }
                },
                DotFormatDateService,
                {
                    provide: DotcmsConfigService,
                    useValue: {
                        getSystemTimeZone: () =>
                            of({
                                id: 'America/Costa_Rica',
                                label: 'Central Standard Time (America/Costa_Rica)',
                                offset: -21600000
                            })
                    }
                }
            ]
        });
        TestBed.overrideProvider(DotContentCompareStore, { useValue: storeMock });

        hostFixture = TestBed.createComponent(TestHostComponent);
        de = hostFixture.debugElement;

        dotContentCompareStore = TestBed.inject(DotContentCompareStore);
        dotAlertConfirmService = TestBed.inject(DotAlertConfirmService);
        dotIframeService = TestBed.inject(DotIframeService);
        hostComponent = hostFixture.componentInstance;
        hostComponent.data = DotContentCompareEventMOCK;
        hostFixture.detectChanges();
        contentCompareTableComponent = de.query(
            By.css('dot-content-compare-table')
        ).componentInstance;
    });

    it('should pass data correctly', () => {
        expect(dotContentCompareStore.loadData).toHaveBeenCalledWith(DotContentCompareEventMOCK);
        expect(contentCompareTableComponent.data).toEqual(dotContentCompareTableDataMock);
        expect(contentCompareTableComponent.showDiff).toEqual(false);
    });

    it('should update diff flag', () => {
        contentCompareTableComponent.changeDiff.emit(true);
        expect(dotContentCompareStore.updateShowDiff).toHaveBeenCalledOnceWith(true);
    });

    it('should update compare content', () => {
        contentCompareTableComponent.changeVersion.emit('value' as unknown as DotCMSContentlet);
        expect(dotContentCompareStore.updateCompare).toHaveBeenCalledOnceWith(
            'value' as unknown as DotCMSContentlet
        );
    });

    it('should bring back version after confirm and emit shutdown', () => {
        jest.spyOn(dotAlertConfirmService, 'confirm').mockImplementation((conf) => {
            conf.accept();
        });
        jest.spyOn(hostComponent.shutdown, 'emit').mockImplementation(() => {});

        contentCompareTableComponent.bringBack.emit('123');

        expect<any>(dotAlertConfirmService.confirm).toHaveBeenCalledWith({
            accept: expect.any(Function),
            reject: expect.any(Function),
            header: 'Confirm',
            message:
                'Are you sure you would like to replace your working version with this contentlet version?'
        });

        expect(dotIframeService.run).toHaveBeenCalledOnceWith({
            name: 'getVersionBack',
            args: ['123']
        });
        expect(hostComponent.shutdown.emit).toHaveBeenCalledOnceWith(true);
    });
});
