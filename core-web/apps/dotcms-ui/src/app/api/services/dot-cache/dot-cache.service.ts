import { Observable } from 'rxjs';

import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { LazyLoadEvent } from 'primeng/api';

import { catchError, map, pluck, take } from 'rxjs/operators';

import { CoreWebService } from '@dotcms/dotcms-js';
import { DotApps } from '@dotcms/dotcms-models';
import { DotCacheProvider } from '@models/dot-cache/dot-cache.model';
import { DotHttpErrorManagerService } from '@services/dot-http-error-manager/dot-http-error-manager.service';

export const CACHE_API_URL = 'v1/cache';

/**
 * Service to wrap /api/v1/cache endpoint.
 * @author jsanca
 */
@Injectable()
export class DotCacheService {
    constructor(
        private coreWebService: CoreWebService,
        private httpErrorManagerService: DotHttpErrorManagerService
    ) {}

    /**
     * Get providers according to pagination and search
     * @param {string} [group]
     * @return {*}  {Observable<DotCacheProvider[]>}
     * @memberof DotCacheService
     */
    getProviders(group: string): Observable<DotCacheProvider[] | null> {
        const url = `${CACHE_API_URL}/providers/${group}/`;

        return this.coreWebService
            .requestView<DotCacheProvider[]>({
                url
            })
            .pipe(
                pluck('entity'),
                catchError((error: HttpErrorResponse) => {
                    return this.httpErrorManagerService.handle(error).pipe(
                        take(1),
                        map(() => null)
                    );
                })
            );
    }
}
