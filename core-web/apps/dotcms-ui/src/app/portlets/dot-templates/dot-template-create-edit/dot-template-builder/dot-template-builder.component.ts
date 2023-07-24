import { Subject } from 'rxjs';

import {
    Component,
    EventEmitter,
    Input,
    OnDestroy,
    OnInit,
    Output,
    ViewChild
} from '@angular/core';

import { debounceTime, map, takeUntil } from 'rxjs/operators';

import { IframeComponent } from '@components/_common/iframe/iframe-component';
import { DotPropertiesService } from '@dotcms/data-access';
import { FeaturedFlags } from '@dotcms/dotcms-models';

import { DotTemplateItem } from '../store/dot-template.store';

export const DEBOUNCE_TIME = 5000;

@Component({
    selector: 'dot-template-builder',
    templateUrl: './dot-template-builder.component.html',
    styleUrls: ['./dot-template-builder.component.scss']
})
export class DotTemplateBuilderComponent implements OnInit, OnDestroy {
    @Input() item: DotTemplateItem;
    @Input() didTemplateChanged: boolean;
    @Output() saveAndPublish = new EventEmitter<DotTemplateItem>();
    @Output() updateTemplate = new EventEmitter<DotTemplateItem>();
    @Output() save = new EventEmitter<DotTemplateItem>();
    @Output() cancel = new EventEmitter();
    @Output() custom: EventEmitter<CustomEvent> = new EventEmitter();
    @ViewChild('historyIframe') historyIframe: IframeComponent;
    permissionsUrl = '';
    historyUrl = '';
    readonly featureFlag = FeaturedFlags.FEATURE_FLAG_TEMPLATE_BUILDER;
    featureFlagIsOn$ = this.propertiesService
        .getKey(this.featureFlag)
        .pipe(map((result) => result && result === 'true'));
    templateUpdate$ = new Subject<DotTemplateItem>();
    destroy$: Subject<boolean> = new Subject<boolean>();

    constructor(private propertiesService: DotPropertiesService) {}

    ngOnInit() {
        this.permissionsUrl = `/html/templates/permissions.jsp?templateId=${this.item.identifier}&popup=true`;
        this.historyUrl = `/html/templates/push_history.jsp?templateId=${this.item.identifier}&popup=true`;
        this.saveTemplateDebounce();
    }

    ngOnDestroy() {
        this.destroy$.next(true);
        this.destroy$.complete();
    }

    /**
     * Update template and publish it
     *
     * @param {DotTemplateItem} item
     * @memberof DotTemplateBuilderComponent
     */
    onTemplateItemChange(item: DotTemplateItem) {
        this.updateTemplate.emit(item);
        if (this.historyIframe) {
            this.historyIframe.iframeElement.nativeElement.contentWindow.location.reload();
        }

        this.templateUpdate$.next(item);
    }

    saveTemplateDebounce() {
        // Approach based on DotEditLayoutComponent, see that component for more info
        this.templateUpdate$
            .pipe(debounceTime(DEBOUNCE_TIME), takeUntil(this.destroy$))
            .subscribe((templateItem) => {
                this.saveAndPublish.emit(templateItem);
            });
    }
}
