import { Observable, Subscription } from 'rxjs';

import { Directive, OnDestroy, OnInit, Optional, Self } from '@angular/core';

import { Dropdown } from 'primeng/dropdown';

import { debounceTime, map, switchMap, tap } from 'rxjs/operators';

import { DotContainersService } from '@dotcms/data-access';
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
    private defaultOptions: Array<DotDropdownSelectOption<DotContainerEntity>>;
    private onFilterSubscription: Subscription;

    constructor(
        @Optional() @Self() private readonly primeDropdown: Dropdown,
        private readonly dotContainersService: DotContainersService
    ) {
        this.control = this.primeDropdown;

        if (this.control) {
            this.control.optionLabel = DEFAULT_LABEL_NAME_INDEX;
            this.control.optionValue = DEFAULT_VALUE_NAME_INDEX;
            this.control.showClear = this.control instanceof Dropdown ? true : false;
        } else {
            console.warn(
                'ContainerOptionsDirective is for use with PrimeNg Dropdown or MultiSelect'
            );
        }
    }

    ngOnInit() {
        this.fetchContainerOptions('').subscribe((options) => {
            this.control.options = options;
            this.defaultOptions = options;
        });
        this.onFilterSubscription = this.control.onFilter
            .pipe(
                debounceTime(500),
                switchMap((event: { filter: string }) => {
                    return this.fetchContainerOptions(event.filter);
                }),
                tap((options) => this.setOptions(options))
            )
            .subscribe();
    }

    private setOptions(options: Array<DotDropdownSelectOption<DotContainerEntity>>) {
        this.control.options = options;
    }

    private fetchContainerOptions(
        filter: string
    ): Observable<DotDropdownSelectOption<DotContainerEntity>[]> {
        return this.dotContainersService.getFiltered(filter, this.maxOptions).pipe(
            map((containerEntities) =>
                containerEntities.map((containerEntity) => ({
                    label: containerEntity.container.friendlyName,
                    value: containerEntity,
                    inactive: false
                }))
            )
        );
    }

    ngOnDestroy() {
        this.onFilterSubscription.unsubscribe();
    }
}
