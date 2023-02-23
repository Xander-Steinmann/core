import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';

import { DialogService } from 'primeng/dynamicdialog';
import { MenuModule } from 'primeng/menu';

import { DotAddToBundleModule } from '@components/_common/dot-add-to-bundle';
import { DotRouterService } from '@dotcms/app/api/services/dot-router/dot-router.service';
import { DotTempFileUploadService } from '@dotcms/app/api/services/dot-temp-file-upload/dot-temp-file-upload.service';
import { DotWorkflowEventHandlerService } from '@dotcms/app/api/services/dot-workflow-event-handler/dot-workflow-event-handler.service';
import {
    DotESContentService,
    DotLanguagesService,
    DotWorkflowActionsFireService,
    DotWorkflowsActionsService,
    DotPageRenderService,
    DotPageTypesService
} from '@dotcms/data-access';
import { SiteService } from '@dotcms/dotcms-js';

import { DotPagesCreatePageDialogComponent } from './dot-pages-create-page-dialog/dot-pages-create-page-dialog.component';
import { DotPagesFavoritePanelModule } from './dot-pages-favorite-panel/dot-pages-favorite-panel.module';
import { DotPagesListingPanelModule } from './dot-pages-listing-panel/dot-pages-listing-panel.module';
import { DotPagesRoutingModule } from './dot-pages-routing.module';
import { DotPageStore } from './dot-pages-store/dot-pages.store';
import { DotPagesComponent } from './dot-pages.component';

@NgModule({
    declarations: [DotPagesComponent],
    imports: [
        CommonModule,
        DotAddToBundleModule,
        DotPagesCreatePageDialogComponent,
        DotPagesFavoritePanelModule,
        DotPagesListingPanelModule,
        DotPagesRoutingModule,
        MenuModule
    ],
    providers: [
        DialogService,
        DotESContentService,
        DotLanguagesService,
        DotPageRenderService,
        DotPageTypesService,
        DotTempFileUploadService,
        DotWorkflowsActionsService,
        DotWorkflowActionsFireService,
        DotWorkflowEventHandlerService,
        DotRouterService,
        SiteService,
        DotPageStore
    ]
})
export class DotPagesModule {}
