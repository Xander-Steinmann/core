/* eslint-disable @typescript-eslint/no-explicit-any */

import { of as observableOf, of } from 'rxjs';

import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

import { DotAppsService } from '@dotcms/app/api/services/dot-apps/dot-apps.service';
import { DotLicenseService } from '@dotcms/data-access';

import { DotAppsListResolver } from './dot-apps-list-resolver.service';
import { appsResponse, AppsServicesMock } from './dot-apps-list.component.spec';

class DotLicenseServicesMock {
    canAccessEnterprisePortlet(_url: string) {
        of(true);
    }
}

const activatedRouteSnapshotMock: any = {
    toString: jest.fn()
};

const routerStateSnapshotMock = {
    toString: jest.fn()
};
routerStateSnapshotMock.url = '/apps';

describe('DotAppsListResolver', () => {
    let dotLicenseServices: DotLicenseService;
    let dotAppsService: DotAppsService;
    let dotAppsListResolver: DotAppsListResolver;

    beforeEach(() => {
        const testbed = TestBed.configureTestingModule({
            providers: [
                DotAppsListResolver,
                { provide: DotLicenseService, useClass: DotLicenseServicesMock },
                { provide: DotAppsService, useClass: AppsServicesMock },
                {
                    provide: ActivatedRouteSnapshot,
                    useValue: activatedRouteSnapshotMock
                }
            ]
        });
        dotAppsService = testbed.get(DotAppsService);
        dotLicenseServices = testbed.get(DotLicenseService);
        dotAppsListResolver = testbed.get(DotAppsListResolver);
    });

    it('should get if portlet can be accessed', () => {
        jest.spyOn(dotLicenseServices, 'canAccessEnterprisePortlet').mockReturnValue(
            observableOf(true)
        );
        jest.spyOn(dotAppsService, 'get').mockReturnValue(of(appsResponse));

        dotAppsListResolver
            .resolve(activatedRouteSnapshotMock, routerStateSnapshotMock)
            .subscribe((resolverData: any) => {
                expect(resolverData).toEqual({
                    apps: appsResponse,
                    isEnterpriseLicense: true
                });
            });
        expect(dotLicenseServices.canAccessEnterprisePortlet).toHaveBeenCalledWith('/apps');
    });
});
