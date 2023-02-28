import { DotPageRender, DotPageRenderState } from '@dotcms/dotcms-models';
import { mockDotRenderedPage, mockUser } from '@dotcms/utils-testing';
import * as dotUtils from '@dotcms/utils/lib/dot-utils';

describe('Dot Utils', () => {
    it('should return anchor with the correct values', () => {
        const blobMock = new Blob(['']);
        const fileName = 'doc.txt';
        jest.spyOn(window.URL, 'createObjectURL').mockImplementation(() => {});
        const anchor = dotUtils.getDownloadLink(blobMock, fileName);

        expect(anchor.download).toEqual(fileName);
        expect(window.URL.createObjectURL).toHaveBeenCalledWith(blobMock);
    });

    it('should return unique URL with host, language and device Ids', () => {
        const mockRenderedPageState = new DotPageRenderState(
            mockUser(),
            new DotPageRender({
                ...mockDotRenderedPage(),
                viewAs: {
                    ...mockDotRenderedPage().viewAs,
                    device: {
                        identifier: 'abc123',
                        cssHeight: '800',
                        cssWidth: '1200',
                        name: 'custom',
                        inode: '123zxc'
                    }
                }
            })
        );

        const url = dotUtils.generateDotFavoritePageUrl(mockRenderedPageState);

        expect(url).toEqual('/an/url/test?&language_id=1&device_inode=123zxc');
    });
});
