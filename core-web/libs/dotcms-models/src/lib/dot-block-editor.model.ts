export type EditorAssetTypes = 'image' | 'video';

export type Actions = {
    command: string;
    menuLabel: string;
    icon: string;
    name: string;
};

export type Block = {
    url: string;
    actions: Array<Actions>;
};

export type CustomBlock = {
    extensions: Block[];
};

export interface EDITOR_DOTMARKETING_CONFIG {
    SHOW_VIDEO_THUMBNAIL: boolean;
}

export enum EDITOR_MARKETING_KEYS {
    SHOW_VIDEO_THUMBNAIL = 'SHOW_VIDEO_THUMBNAIL'
}
