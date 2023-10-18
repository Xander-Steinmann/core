import { Mark, mergeAttributes } from '@tiptap/core';

export interface PropertyMarkOptions {
    HTMLAttributes: Record<string, string>;
}

declare module '@tiptap/core' {
    interface Commands<ReturnType> {
        io_property: {
            setProperty: () => ReturnType;
            toggleProperty: () => ReturnType;
            unsetProperty: () => ReturnType;
        };
    }
}

export const IOPropertyMark = Mark.create<PropertyMarkOptions>({
    name: 'io_property',
    addOptions: () => ({
        HTMLAttributes: {
            class: 'property',
            style: 'background-color: #fafafa; border: 1px solid #e2e1e6;'
        }
    }),
    parseHTML: () => [
        {
            tag: 'span'
        },
        {
            class: 'property'
        }
    ],
    renderHTML({ HTMLAttributes: n }) {
        return ['span', mergeAttributes(this.options.HTMLAttributes, n), 0];
    },
    addCommands() {
        return {
            setProperty:
                () =>
                ({ commands }) =>
                    commands.setMark(this.name),
            toggleProperty:
                () =>
                ({ commands }) =>
                    commands.toggleMark(this.name),
            unsetProperty:
                () =>
                ({ commands }) =>
                    commands.unsetMark(this.name)
        };
        // },
        // addKeyboardShortcuts() {
        //     return {
        //         "Mod-i": ()=>this.editor.commands.toggleProperty(),
        //         "Mod-I": ()=>this.editor.commands.toggleProperty()
        //     }
        // },
        // addInputRules() {
        //     return [rl({
        //         find: Rde,
        //         type: this.type
        //     }), rl({
        //         find: Nde,
        //         type: this.type
        //     })]
        // },
        // addPasteRules() {
        //     return [Gs({
        //         find: Fde,
        //         type: this.type
        //     }), Gs({
        //         find: Lde,
        //         type: this.type
        //     })]
    }
});
