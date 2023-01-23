import { Subject } from 'rxjs';

import { Component, Injector, Input, OnDestroy, OnInit, ViewContainerRef } from '@angular/core';

import { debounceTime, takeUntil } from 'rxjs/operators';

import { AnyExtension, Content, Editor } from '@tiptap/core';
import CharacterCount, { CharacterCountStorage } from '@tiptap/extension-character-count';
import { HeadingOptions, Level } from '@tiptap/extension-heading';
import { Highlight } from '@tiptap/extension-highlight';
import { Link } from '@tiptap/extension-link';
import Placeholder from '@tiptap/extension-placeholder';
import { TableRow } from '@tiptap/extension-table-row';
import { TextAlign } from '@tiptap/extension-text-align';
import { Underline } from '@tiptap/extension-underline';
import StarterKit, { StarterKitOptions } from '@tiptap/starter-kit';

import {
    ActionsMenu,
    BubbleFormExtension,
    BubbleLinkFormExtension,
    DotBubbleMenuExtension,
    DEFAULT_LANG_ID,
    DotConfigExtension,
    DotTableCellExtension,
    DotTableHeaderExtension,
    DotTableExtension,
    DragHandler,
    DotFloatingButton,
    BubbleImageTabviewFormExtension,
    ImageUpload
} from '../../extensions';
import { ContentletBlock, ImageNode } from '../../nodes';
import { formatHTML, SetDocAttrStep } from '../../shared/utils';
import { from } from 'rxjs';

function toTitleCase(str) {
    return str.replace(/\p{L}+('\p{L}+)?/gu, function (txt) {
        return txt.charAt(0).toUpperCase() + txt.slice(1);
    });
}

@Component({
    selector: 'dot-block-editor',
    templateUrl: './dot-block-editor.component.html',
    styleUrls: ['./dot-block-editor.component.scss']
})
export class DotBlockEditorComponent implements OnInit, OnDestroy {
    @Input() lang = DEFAULT_LANG_ID;
    @Input() allowedContentTypes: string;
    @Input() customStyles: string;
    @Input() displayCountBar: boolean | string = true;
    @Input() charLimit: number;
    @Input() content: Content;

    @Input() set allowedBlocks(blocks: string) {
        this._allowedBlocks = [
            ...this._allowedBlocks,
            ...(blocks ? blocks.replace(/ /g, '').split(',').filter(Boolean) : [])
        ];
    }

    @Input() set value(content: Content) {
        this.content = typeof content === 'string' ? formatHTML(content) : content;
    }

    editor: Editor;
    subject = new Subject();

    private _allowedBlocks = ['paragraph']; //paragraph should be always.
    private destroy$: Subject<boolean> = new Subject<boolean>();

    get characterCount(): CharacterCountStorage {
        return this.editor.storage.characterCount;
    }

    get showCharData() {
        try {
            return JSON.parse(this.displayCountBar as string);
        } catch (e) {
            return true;
        }
    }

    get readingTime() {
        // The constant used by Medium for words an adult can read per minute is 265
        // More Information here: https://help.medium.com/hc/en-us/articles/214991667-Read-time
        return Math.ceil(this.characterCount.words() / 265);
    }

    constructor(private injector: Injector, public viewContainerRef: ViewContainerRef) {}

    ngOnInit() {
        //  const extensions = await this.setEditorExtensions()
        from(this.loadImageNode()).subscribe((image) => {
            console.log('image', image);
            // Todo el Codigo
            this.editor = new Editor({
                extensions: [StarterKit.configure(this.setStarterKitOptions()), image]
            });
            this.editor.commands.setContent(
                '<img src="https://img.freepik.com/free-vector/flat-design-abstract-geometric-real-estate-youtube-thumbnail_23-2149124845.jpg?w=2000" />'
            );
            console.log(this.editor);

            /*   this.editor.on('create', () => this.updateChartCount());
        this.subject
            .pipe(takeUntil(this.destroy$), debounceTime(250))
            .subscribe(() => this.updateChartCount()); */
        });
    }

    ngOnDestroy() {
        this.destroy$.next(true);
        this.destroy$.complete();
    }

    private updateChartCount(): void {
        const tr = this.editor.state.tr
            .step(new SetDocAttrStep('chartCount', this.characterCount.characters()))
            .step(new SetDocAttrStep('wordCount', this.characterCount.words()))
            .step(new SetDocAttrStep('readingTime', this.readingTime));
        this.editor.view.dispatch(tr);
    }

    private async loadImageNode() {
        const url =
            'https://cdn.skypack.dev/-/@tiptap/extension-image@v2.0.0-beta.209-ZTnvxbVFzhKCGGatBhJQ/dist=es2019,mode=imports/optimized/@tiptap/extension-image.js';
        const module = await import(/* webpackIgnore: true */ url);
        console.log('Module', module);
        return module.Image;
    }

    private async setEditorExtensions(): Promise<AnyExtension[]> {
        const defaultExtensions: AnyExtension[] = [
            DotConfigExtension({
                lang: this.lang,
                allowedContentTypes: this.allowedContentTypes,
                allowedBlocks: this._allowedBlocks
            }),
            ActionsMenu(this.viewContainerRef),
            DragHandler(this.viewContainerRef),
            ImageUpload(this.injector, this.viewContainerRef),
            BubbleLinkFormExtension(this.viewContainerRef),
            DotBubbleMenuExtension(this.viewContainerRef),
            BubbleFormExtension(this.viewContainerRef),
            DotFloatingButton(this.injector, this.viewContainerRef),
            BubbleImageTabviewFormExtension(this.viewContainerRef),
            // Marks Extensions
            Underline,
            CharacterCount,
            TextAlign.configure({ types: ['heading', 'paragraph', 'listItem', 'dotImage'] }),
            Highlight.configure({ HTMLAttributes: { style: 'background: #accef7;' } }),
            Link.configure({ autolink: false, openOnClick: false }),
            Placeholder.configure({
                placeholder: ({ node }) => {
                    if (node.type.name === 'heading') {
                        return `${toTitleCase(node.type.name)} ${node.attrs.level}`;
                    }

                    return 'Type "/" for commmands';
                }
            }),
            DotTableCellExtension(this.viewContainerRef),
            DotTableHeaderExtension(),
            TableRow
        ];

        const customExtensions: Map<string, AnyExtension> = new Map([
            ['contentlets', ContentletBlock(this.injector)],
            ['table', DotTableExtension()],
            ['image', await this.loadImageNode()]
        ]);

        return [
            ...defaultExtensions,
            ...(this._allowedBlocks.length > 1
                ? [
                      StarterKit.configure(this.setStarterKitOptions()),
                      ...this.setCustomExtensions(customExtensions)
                  ]
                : [StarterKit, ...customExtensions.values()])
        ];
    }

    /**
     *
     * Check if the starter kit keys are part of the _allowedBlocks,
     * ONLY if is not present will add an attribute with false to disable it. ex. {orderedList: false}.
     * Exception, headings fill the HeadingOptions or false.
     */
    private setStarterKitOptions(): Partial<StarterKitOptions> {
        // These are the keys that meter for the starter kit.
        const staterKitOptions = [
            'orderedList',
            'bulletList',
            'blockquote',
            'codeBlock',
            'horizontalRule'
        ];
        const headingOptions: HeadingOptions = { levels: [], HTMLAttributes: {} };

        //Heading types supported by default in the editor.
        ['heading1', 'heading2', 'heading3', 'heading4', 'heading5', 'heading6'].forEach(
            (heading) => {
                if (this._allowedBlocks.includes(heading)) {
                    headingOptions.levels.push(+heading.slice(-1) as Level);
                }
            }
        );

        return {
            heading: headingOptions.levels.length ? headingOptions : false,
            ...staterKitOptions.reduce(
                (object, item) => ({
                    ...object,
                    ...(this._allowedBlocks.includes(item) ? {} : { [item]: false })
                }),
                {}
            )
        };
    }

    private setCustomExtensions(customExtensions: Map<string, AnyExtension>): AnyExtension[] {
        return [
            ...(this._allowedBlocks.includes('contentlets')
                ? [customExtensions.get('contentlets')]
                : []),
            ...(this._allowedBlocks.includes('dotImage') ? [customExtensions.get('dotImage')] : [])
        ];
    }
}
