import { Subject } from 'rxjs';

import { HttpClient, HttpHandler } from '@angular/common/http';
import { Component, DebugElement, EventEmitter, Injectable, Input, Output } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';

import { DialogService } from 'primeng/dynamicdialog';
import { MenuModule } from 'primeng/menu';

import { of } from 'rxjs/internal/observable/of';

import { DotMessageDisplayServiceMock } from '@components/dot-message-display/dot-message-display.component.spec';
import { DotMessageSeverity, DotMessageType } from '@components/dot-message-display/model';
import { DotMessageDisplayService } from '@components/dot-message-display/services';
import { DotRouterService } from '@dotcms/app/api/services/dot-router/dot-router.service';
import { DotEventsService } from '@dotcms/data-access';
import { CoreWebService, CoreWebServiceMock, mockSites, SiteService } from '@dotcms/dotcms-js';
import {
    dotcmsContentletMock,
    dotcmsContentTypeBasicMock,
    MockDotRouterService
} from '@dotcms/utils-testing';

import { DotPageStore } from './dot-pages-store/dot-pages.store';
import { DotActionsMenuEventParams, DotPagesComponent } from './dot-pages.component';

@Component({
    selector: 'dot-pages-favorite-panel',
    template: ''
})
class MockDotPagesFavoritePanelComponent {
    @Output() goToUrl = new EventEmitter<string>();
    @Output() showActionsMenu = new EventEmitter<DotActionsMenuEventParams>();
}

@Component({
    selector: 'dot-pages-listing-panel',
    template: ''
})
class MockDotPagesListingPanelComponent {
    @Output() goToUrl = new EventEmitter<string>();
    @Output() showActionsMenu = new EventEmitter<DotActionsMenuEventParams>();
}

@Component({
    selector: 'dot-add-to-bundle',
    template: ''
})
class MockDotAddToBundleComponent {
    @Input() assetIdentifier: string;
    @Output() cancel = new EventEmitter<boolean>();
}

export const favoritePagesInitialTestData = [
    {
        ...dotcmsContentletMock,
        live: true,
        baseType: 'CONTENT',
        modDate: '2020-09-02 16:45:15.569',
        title: 'preview1',
        screenshot: 'test1',
        url: '/index1?host_id=A&language_id=1&device_inode=123',
        owner: 'admin'
    },
    {
        ...dotcmsContentletMock,
        title: 'preview2',
        modDate: '2020-09-02 16:45:15.569',
        screenshot: 'test2',
        url: '/index2',
        owner: 'admin2'
    }
];

const storeMock = {
    get actionMenuDomId$() {
        return of('');
    },
    get languageOptions$() {
        return of([]);
    },
    get languageLabels$() {
        return of({});
    },
    get pageTypes$() {
        return of([{ ...dotcmsContentTypeBasicMock }]);
    },
    clearMenuActions: jasmine.createSpy(),
    getFavoritePages: jasmine.createSpy(),
    getPages: jasmine.createSpy(),
    getPagesRetry: jasmine.createSpy(),
    getPageTypes: jasmine.createSpy(),
    showActionsMenu: jasmine.createSpy(),
    setInitialStateData: jasmine.createSpy(),
    limitFavoritePages: jasmine.createSpy(),
    vm$: of({
        favoritePages: {
            items: [],
            showLoadMoreButton: false,
            total: 0
        },
        isEnterprise: true,
        environments: true,
        languages: [],
        loggedUser: {
            id: 'admin',
            canRead: { contentlets: true, htmlPages: true },
            canWrite: { contentlets: true, htmlPages: true }
        },
        pages: {
            actionMenuDomId: '',
            items: [],
            addToBundleCTId: 'test1'
        },
        pageTypes: []
    })
};
@Injectable()
export class DialogServiceMock {
    open(): void {
        /* */
    }
}

describe('DotPagesComponent', () => {
    let fixture: ComponentFixture<DotPagesComponent>;
    let component: DotPagesComponent;
    let de: DebugElement;
    let store: DotPageStore;
    let dialogService: DialogService;
    let dotRouterService: DotRouterService;
    let dotMessageDisplayService: DotMessageDisplayService;

    const switchSiteSubject = new Subject();

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [
                MockDotPagesFavoritePanelComponent,
                MockDotPagesListingPanelComponent,
                MockDotAddToBundleComponent,
                DotPagesComponent
            ],
            imports: [MenuModule],
            providers: [
                DotEventsService,
                HttpClient,
                HttpHandler,
                { provide: CoreWebService, useClass: CoreWebServiceMock },
                { provide: DotMessageDisplayService, useClass: DotMessageDisplayServiceMock },
                { provide: DotRouterService, useClass: MockDotRouterService },
                { provide: DialogService, useClass: DialogServiceMock },
                {
                    provide: SiteService,
                    useValue: {
                        get currentSite() {
                            return undefined;
                        },

                        get switchSite$() {
                            return switchSiteSubject.asObservable();
                        }
                    }
                }
            ]
        }).compileComponents();
    });

    beforeEach(() => {
        TestBed.overrideProvider(DotPageStore, {
            useValue: storeMock
        });
        store = TestBed.inject(DotPageStore);
        dialogService = TestBed.inject(DialogService);
        dotRouterService = TestBed.inject(DotRouterService);
        dotMessageDisplayService = TestBed.inject(DotMessageDisplayService);
        fixture = TestBed.createComponent(DotPagesComponent);
        de = fixture.debugElement;
        component = fixture.componentInstance;

        fixture.detectChanges();
        spyOn(component.menu, 'hide');
        spyOn(dotMessageDisplayService, 'push');
        spyOn(dialogService, 'open').and.callThrough();
    });

    it('should init store', () => {
        expect(store.setInitialStateData).toHaveBeenCalledWith(5);
    });

    it('should have favorite page panel, menu, pages panel and DotAddToBundle components', () => {
        expect(de.query(By.css('dot-pages-favorite-panel'))).toBeTruthy();
        expect(de.query(By.css('p-menu'))).toBeTruthy();
        expect(de.query(By.css('dot-pages-listing-panel'))).toBeTruthy();
        expect(de.query(By.css('dot-add-to-bundle'))).toBeTruthy();
    });

    it('should call goToUrl method from DotPagesFavoritePanel', () => {
        const elem = de.query(By.css('dot-pages-favorite-panel'));
        elem.triggerEventHandler('goToUrl', '/page/1?lang=1');

        expect(dotRouterService.goToEditPage).toHaveBeenCalledWith({
            lang: '1',
            url: '/page/1'
        });
    });

    it('should call showActionsMenu method from DotPagesFavoritePanel', () => {
        const eventMock = new MouseEvent('click');
        Object.defineProperty(eventMock, 'currentTarget', {
            value: { id: 'test' },
            enumerable: true
        });

        const actionMenuParam = {
            event: eventMock,
            actionMenuDomId: 'test1',
            item: dotcmsContentletMock
        };

        const elem = de.query(By.css('dot-pages-favorite-panel'));
        elem.triggerEventHandler('showActionsMenu', actionMenuParam);

        expect(component.menu.hide).toHaveBeenCalledTimes(1);
        expect(store.showActionsMenu).toHaveBeenCalledWith({
            item: dotcmsContentletMock,
            actionMenuDomId: 'test1'
        });
    });

    it('should call goToUrl method from DotPagesListingPanel', () => {
        const elem = de.query(By.css('dot-pages-listing-panel'));
        elem.triggerEventHandler('goToUrl', '/page/1?lang=1');

        expect(dotRouterService.goToEditPage).toHaveBeenCalledWith({
            lang: '1',
            url: '/page/1'
        });
    });

    it('should call showActionsMenu method from DotPagesListingPanel', () => {
        const eventMock = new MouseEvent('click');
        Object.defineProperty(eventMock, 'currentTarget', {
            value: { id: 'test' },
            enumerable: true
        });

        const actionMenuParam = {
            event: eventMock,
            actionMenuDomId: 'test1',
            item: dotcmsContentletMock
        };

        const elem = de.query(By.css('dot-pages-listing-panel'));
        elem.triggerEventHandler('showActionsMenu', actionMenuParam);

        expect(component.menu.hide).toHaveBeenCalledTimes(1);
        expect(store.showActionsMenu).toHaveBeenCalledWith({
            item: dotcmsContentletMock,
            actionMenuDomId: 'test1'
        });
    });

    it('should call closedActionsMenu method from p-menu', () => {
        const elem = de.query(By.css('p-menu'));
        elem.triggerEventHandler('onHide', {});

        expect(store.clearMenuActions).toHaveBeenCalledTimes(1);
    });

    it('should call push method in dotMessageDisplayService once a save-page is received', () => {
        const dotEventsService: DotEventsService = de.injector.get(DotEventsService);

        dotEventsService.notify('save-page', { value: 'test3' });

        expect(dotMessageDisplayService.push).toHaveBeenCalledWith({
            life: 3000,
            message: 'test3',
            severity: DotMessageSeverity.SUCCESS,
            type: DotMessageType.SIMPLE_MESSAGE
        });
        expect(store.getPages).toHaveBeenCalledWith({ offset: 0 });
    });

    it('should try to reload a page when a dotEventsService message save-page is received with retryLoading = true', () => {
        const dotEventsService: DotEventsService = de.injector.get(DotEventsService);

        dotEventsService.notify('save-page', { value: 'test3', retryLoading: true });

        expect(store.getPagesRetry).toHaveBeenCalledWith({ offset: 0 });
    });

    it('should reload portlet only when the site change', () => {
        switchSiteSubject.next(mockSites[0]); // setting the site
        switchSiteSubject.next(mockSites[1]); // switching the site
        expect(store.getPages).toHaveBeenCalledWith({ offset: 0 });
    });
});
