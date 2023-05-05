import { Observable } from 'rxjs';

import {
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnInit,
    Output,
    ViewChild
} from '@angular/core';

import { map } from 'rxjs/operators';

import { IframeComponent } from '@components/_common/iframe/iframe-component';
import { DotPropertiesService } from '@dotcms/data-access';
import { FeaturedFlags } from '@dotcms/dotcms-models';

import { DotTemplateItem } from '../store/dot-template.store';

@Component({
    selector: 'dot-template-builder',
    templateUrl: './dot-template-builder.component.html',
    styleUrls: ['./dot-template-builder.component.scss']
})
export class DotTemplateBuilderComponent implements OnInit, OnChanges {
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

    enableNewBuilder$: Observable<{ enableNewBuilder: boolean }>;

    constructor(private readonly dotPropertiesService: DotPropertiesService) {}
    ngOnInit() {
        this.enableNewBuilder$ = this.dotPropertiesService
            .getKey(FeaturedFlags.FEATURE_FLAG_TEMPLATE_BUILDER)
            .pipe(map((result) => ({ enableNewBuilder: result && result === 'true' })));

        this.permissionsUrl = `/html/templates/permissions.jsp?templateId=${this.item.identifier}&popup=true`;
        this.historyUrl = `/html/templates/push_history.jsp?templateId=${this.item.identifier}&popup=true`;
    }

    ngOnChanges(): void {
        if (this.historyIframe) {
            this.historyIframe.iframeElement.nativeElement.contentWindow.location.reload();
        }
    }
}
