import { Injectable } from '@angular/core';

import { DotFormatDateService } from '@dotcms/app/api/services/dot-format-date-service';

@Injectable()
class DotFormatDateServiceMock {
    getRelative() {
        return '6 hours ago';
    }

    getUTC(time: Date = new Date()): Date {
        const utcTime = new Date(time.getTime() + time.getTimezoneOffset() * 60000);

        return utcTime;
    }

    differenceInCalendarDays(_dateLeft: Date, _dateRight: Date): number {
        return 1;
    }
}

import { DotRelativeDatePipe } from './dot-relative-date.pipe';

describe('DotRelativeDatePipe', () => {
    it('should set relative date', () => {
        const formatDateService = new DotFormatDateServiceMock();

        const pipe = new DotRelativeDatePipe(formatDateService as unknown as DotFormatDateService);
        expect(pipe.transform('2023-03-13 17:17:25.11')).toEqual('6 hours ago');
    });
});
