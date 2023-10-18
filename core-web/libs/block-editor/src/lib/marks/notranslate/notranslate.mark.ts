import { Mark, mergeAttributes } from '@tiptap/core';

declare module '@tiptap/core' {
    interface Commands<ReturnType> {
        io_notranslate: {
            setNoTranslate: () => ReturnType;
            toggleNoTranslate: () => ReturnType;
            unsetNoTranslate: () => ReturnType;
        };
    }
}

export const IONoTranslateMark = Mark.create({
    name: 'io_notranslate',
    addOptions: () => ({
        HTMLAttributes: {
            'no-translate': 'true',
            style: 'background-color: yellow'
        }
    }),
    parseHTML: () => [
        {
            //"no-translate": "true"
        }
    ],
    renderHTML({ HTMLAttributes: n }) {
        return ['span', mergeAttributes(this.options.HTMLAttributes, n), 0];
    },
    addCommands() {
        return {
            setNoTranslate:
                () =>
                ({ commands: n }) =>
                    n.setMark(this.name),
            toggleNoTranslate:
                () =>
                ({ commands: n }) =>
                    n.toggleMark(this.name),
            unsetNoTranslate:
                () =>
                ({ commands: n }) =>
                    n.unsetMark(this.name)
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
