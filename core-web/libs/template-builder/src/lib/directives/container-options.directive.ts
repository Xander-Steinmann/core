import { Observable, of, Subject, Subscription } from 'rxjs';

import { Directive, OnDestroy, OnInit, Optional, Self } from '@angular/core';

import { Dropdown } from 'primeng/dropdown';

import { catchError, debounceTime, map, switchMap, take, takeUntil } from 'rxjs/operators';

import { DotContainersService, DotMessageService } from '@dotcms/data-access';
import { DotContainerEntity, DotDropdownSelectOption } from '@dotcms/dotcms-models';

const DEFAULT_LABEL_NAME_INDEX = 'label';
const DEFAULT_VALUE_NAME_INDEX = 'value';

/**
 * Directive to set a default configuration of Dropdown or MultiSelect (PrimeNG) and translate the label of the options
 *
 * @export
 * @class DotDropdownDirective
 */
@Directive({
    selector: 'p-dropdown[dotcmsContainerOptions]',
    standalone: true
})
export class ContainerOptionsDirective implements OnInit, OnDestroy {
    private readonly control: Dropdown;
    private readonly maxOptions = 10;
    private readonly loadErrorMessage: string;
    private onFilterSubscription: Subscription;
    private destroy$: Subject<boolean> = new Subject<boolean>();

    constructor(
        @Optional() @Self() private readonly primeDropdown: Dropdown,
        private readonly dotContainersService: DotContainersService,
        private readonly dotMessageService: DotMessageService
    ) {
        this.control = this.primeDropdown;
        this.loadErrorMessage = this.dotMessageService.get(
            'dot.template.builder.box.containers.error'
        );

        if (this.control) {
            this.control.optionLabel = DEFAULT_LABEL_NAME_INDEX;
            this.control.optionValue = DEFAULT_VALUE_NAME_INDEX;
            this.control.optionDisabled = 'inactive';
        } else {
            console.warn('ContainerOptionsDirective is for use with PrimeNg Dropdown');
        }
    }

    ngOnInit() {
        this.fetchContainerOptions()
            .pipe(catchError(() => this.handleContainersLoadError()))
            .subscribe((options) => {
                this.control.options = this.control.options || options; // avoid overwriting if they were already set
            });
        this.onFilterSubscription = this.control.onFilter
            .pipe(
                takeUntil(this.destroy$),
                debounceTime(500),
                switchMap((event: { filter: string }) => {
                    return this.fetchContainerOptions(event.filter);
                }),
                catchError(() => this.handleContainersLoadError())
            )
            .subscribe((options) => this.setOptions(options));
    }

    private fetchContainerOptions(
        filter: string = ''
    ): Observable<DotDropdownSelectOption<DotContainerEntity>[]> {
        return this.dotContainersService.getFiltered(filter, this.maxOptions).pipe(
            take(1),
            map((containerEntities) =>
                containerEntities.map((containerEntity) => ({
                    label: containerEntity.container.friendlyName,
                    value: containerEntity,
                    inactive: false
                }))
            )
        );
    }

    private getErrorOptions(): DotDropdownSelectOption<DotContainerEntity>[] {
        return [
            {
                label: this.loadErrorMessage,
                inactive: true,
                value: null
            }
        ];
    }

    private handleContainersLoadError() {
        return of(this.getErrorOptions());
    }

    private setOptions(options: Array<DotDropdownSelectOption<DotContainerEntity>>) {
        this.control.options = options;
    }

    ngOnDestroy() {
        this.destroy$.next(true);
        this.destroy$.complete();
    }
}
