import { catchError, map, pluck, take } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { Observable } from 'rxjs';
import { CoreWebService, DotRequestOptionsArgs } from '@dotcms/dotcms-js';

import { DotHttpErrorManagerService } from '@services/dot-http-error-manager/dot-http-error-manager.service';
import { DotActionBulkResult } from '@models/dot-action-bulk-result/dot-action-bulk-result.model';
import { DotContainer } from '@dotcms/app/shared/models/container/dot-container.model';

export const CONTAINER_API_URL = '/api/v1/containers/';

/**
 * Provide util methods to handle containers in the system.
 * @export
 * @class DotContainersService
 */
@Injectable()
export class DotContainersService {
    constructor(
        private coreWebService: CoreWebService,
        private httpErrorManagerService: DotHttpErrorManagerService
    ) {}

    /**
     * Return a list of containers.
     * @returns Observable<DotContainer[]>
     * @memberof DotContainersService
     */
    get(): Observable<DotContainer[]> {
        return this.request<DotContainer[]>({ url: CONTAINER_API_URL });
    }

    /**
     * Get the container, pass the version default working
     *
     * @param {string} id
     * @param {string} [version='working']
     * @returns {Observable<DotContainer>}
     * @memberof DotContainersService
     */
    getById(id: string, version = 'working'): Observable<DotContainer> {
        const url = `${CONTAINER_API_URL}${id}/${version}`;

        return this.request<DotContainer>({
            url
        });
    }

    /**
     * Get the container filtered by tittle or inode .
     *
     * @param {string} filter
     * @returns {Observable<DotContainer>}
     * @memberof DotContainersService
     */
    getFiltered(filter: string): Observable<DotContainer[]> {
        const url = `${CONTAINER_API_URL}?filter=${filter}`;

        return this.request<DotContainer[]>({
            url
        });
    }

    /**
     * Creates a container
     *
     * @param {DotContainer} values
     * @return Observable<DotContainer>
     * @memberof DotContainersService
     */
    create(values: DotContainer): Observable<DotContainer> {
        return this.request<DotContainer>({ method: 'POST', url: CONTAINER_API_URL, body: values });
    }

    /**
     * Updates a container
     * @returns Observable<DotContainer>
     * @memberof DotContainersService
     */
    update(values: DotContainer): Observable<DotContainer> {
        return this.request<DotContainer>({ method: 'PUT', url: CONTAINER_API_URL, body: values });
    }

    /**
     * Save and Publish a container
     * @param {DotContainer} values
     * @returns Observable<DotContainer>
     * @memberof DotContainersService
     */
    saveAndPublish(values: DotContainer): Observable<DotContainer> {
        return this.request<DotContainer>({
            method: 'PUT',
            url: `${CONTAINER_API_URL}_savepublish`,
            body: values
        });
    }

    /**
     * Delete a container
     * @param {string[]} identifiers
     * @returns Observable<DotActionBulkResult>
     * @memberof DotContainersService
     */
    delete(identifiers: string[]): Observable<DotActionBulkResult> {
        return this.request<DotActionBulkResult>({
            method: 'DELETE',
            url: `${CONTAINER_API_URL}bulkdelete`,
            body: identifiers
        });
    }

    /**
     * Unarchive a container
     * @param {string[]} identifiers
     * @returns Observable<DotActionBulkResult>
     * @memberof DotContainersService
     */
    unArchive(identifiers: string[]): Observable<DotActionBulkResult> {
        const url = `${CONTAINER_API_URL}_bulkunarchive`;

        return this.request<DotActionBulkResult>({ method: 'PUT', url, body: identifiers });
    }

    /**
     * Archive a container
     * @param {string[]} identifiers
     * @returns Observable<DotActionBulkResult>
     * @memberof DotContainersService
     */
    archive(identifiers: string[]): Observable<DotActionBulkResult> {
        const url = `${CONTAINER_API_URL}_bulkarchive`;

        return this.request<DotActionBulkResult>({ method: 'PUT', url, body: identifiers });
    }

    /**
     * Unpublish a container00
     * @param {string[]} identifiers
     * @returns Observable<DotActionBulkResult>
     * @memberof DotContainersService
     */
    unPublish(identifiers: string[]): Observable<DotActionBulkResult> {
        const url = `${CONTAINER_API_URL}_bulkunpublish`;

        return this.request<DotActionBulkResult>({ method: 'PUT', url, body: identifiers });
    }

    /**
     * Publish a container
     * @param {string[]} identifiers
     * @returns Observable<DotActionBulkResult>
     * @memberof DotContainersService
     */
    publish(identifiers: string[]): Observable<DotActionBulkResult> {
        const url = `${CONTAINER_API_URL}_bulkpublish`;

        return this.request<DotActionBulkResult>({ method: 'PUT', url, body: identifiers });
    }

    /**
     * Copy a container
     * @param {string} identifier
     * @returns Observable<DotContainer>
     * @memberof DotContainersService
     */
    copy(identifier: string): Observable<DotContainer> {
        const url = `${CONTAINER_API_URL}${identifier}/_copy`;

        return this.request<DotContainer>({ method: 'PUT', url });
    }

    private request<T>(options: DotRequestOptionsArgs): Observable<T> {
        const response$ = this.coreWebService.requestView<T>(options);

        return response$.pipe(
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
