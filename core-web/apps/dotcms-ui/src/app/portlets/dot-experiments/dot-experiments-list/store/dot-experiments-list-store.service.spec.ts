import { of } from 'rxjs';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';

import { MessageService } from 'primeng/api';

import { DotMessageService } from '@dotcms/data-access';
import {
    ComponentStatus,
    DotExperiment,
    DotExperimentStatusList,
    GroupedExperimentByStatus,
    TrafficProportionTypes
} from '@dotcms/dotcms-models';
import { MockDotMessageService } from '@dotcms/utils-testing';
import { DotMessagePipeModule } from '@pipes/dot-message/dot-message-pipe.module';
import { DotExperimentsService } from '@portlets/dot-experiments/shared/services/dot-experiments.service';
import { ExperimentMocks, GoalsMock } from '@portlets/dot-experiments/test/mocks';

import { DotExperimentsListStore, DotExperimentsState } from './dot-experiments-list-store.service';

const routerParamsPageId = '1111-1111-111';
const ActivatedRouteMock = {
    snapshot: {
        params: {
            pageId: routerParamsPageId
        },
        parent: { parent: { parent: { parent: { data: { content: { page: { title: '' } } } } } } }
    }
};

const messageServiceMock = new MockDotMessageService({
    'experimentspage.add.new.experiment': 'Add a new experiment'
});

describe('DotExperimentsListStore', () => {
    let store: DotExperimentsListStore;
    let dotExperimentsService: jasmine.SpyObj<DotExperimentsService>;

    beforeEach(() => {
        const dotExperimentsServiceSpy = {
            add: jest.fn(),
            getAll: jest.fn(),
            getById: jest.fn(),
            archive: jest.fn(),
            delete: jest.fn()
        };

        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule, DotMessagePipeModule, RouterTestingModule],
            providers: [
                DotExperimentsListStore,
                MessageService,
                { provide: ActivatedRoute, useValue: ActivatedRouteMock },
                { provide: DotExperimentsService, useValue: dotExperimentsServiceSpy },
                { provide: DotMessageService, useValue: messageServiceMock }
            ]
        });

        store = TestBed.inject(DotExperimentsListStore);

        dotExperimentsService = TestBed.inject(
            DotExperimentsService
        ) as jasmine.SpyObj<DotExperimentsService>;
    });

    it('should set initial data', (done) => {
        const expectedInitialState: DotExperimentsState = {
            page: {
                pageId: routerParamsPageId,
                pageTitle: ''
            },
            experiments: [],
            filterStatus: [
                DotExperimentStatusList.DRAFT,
                DotExperimentStatusList.ENDED,
                DotExperimentStatusList.RUNNING,
                DotExperimentStatusList.SCHEDULED,
                DotExperimentStatusList.ARCHIVED
            ],
            status: ComponentStatus.INIT
        };

        store.state$.subscribe((state) => {
            expect(state).toEqual(expectedInitialState);
            done();
        });
    });

    it('should have getState$ from the store', () => {
        store.getStatus$.subscribe((data) => {
            expect(data).toEqual(ComponentStatus.INIT);
        });
    });

    it('should update status to the store', () => {
        store.setComponentStatus(ComponentStatus.LOADED);
        store.getStatus$.subscribe((status) => {
            expect(status).toEqual(ComponentStatus.LOADED);
        });
    });
    it('should update experiments to the store', () => {
        store.setExperiments(ExperimentMocks);
        store.getExperiments$.subscribe((experiments) => {
            expect(experiments).toEqual(ExperimentMocks);
        });
    });
    it('should update status filtered to the store', () => {
        const statusSelectedMock = [DotExperimentStatusList.DRAFT, DotExperimentStatusList.ENDED];
        store.setFilterStatus(statusSelectedMock);
        store.getFilterStatusList$.subscribe((experimentsFilterStatus) => {
            expect(experimentsFilterStatus).toEqual(statusSelectedMock);
        });
    });

    it('should delete experiment by id of the store', () => {
        const ID_TO_DELETE = '222';
        const expected: DotExperiment[] = [ExperimentMocks[0], ExperimentMocks[2]];

        store.setExperiments(ExperimentMocks);
        store.deleteExperimentById(ID_TO_DELETE);
        store.getExperiments$.subscribe((experiments) => {
            expect(experiments).toEqual(expected);
        });
    });

    it('should change status to archived status by experiment id of the store', () => {
        const ID_TO_ARCHIVE = '222';
        const expected: DotExperiment[] = [...ExperimentMocks];
        expected[1].status = DotExperimentStatusList.ARCHIVED;

        store.setExperiments(ExperimentMocks);
        store.archiveExperimentById(ID_TO_ARCHIVE);
        store.getExperiments$.subscribe((exp) => {
            expect(exp).toEqual(expected);
        });
    });

    it('should get ordered experiment by status', () => {
        const endedExperiments: DotExperiment[] = [
            {
                id: '111',
                identifier: '1111-1111-1111-1111',
                pageId: '456',
                status: DotExperimentStatusList.ENDED,
                archived: false,
                readyToStart: false,
                description: 'Praesent at molestie mauris, quis vulputate augue.',
                name: 'Praesent at molestie mauris',
                trafficAllocation: 100,
                scheduling: null,
                trafficProportion: {
                    type: TrafficProportionTypes.SPLIT_EVENLY,
                    variants: [{ id: '111', name: 'DEFAULT', weight: 100.0 }]
                },
                creationDate: new Date('2022-08-21 14:50:03'),
                modDate: new Date('2022-08-21 18:50:03'),
                goals: { ...GoalsMock }
            }
        ];
        const archivedExperiments: DotExperiment[] = [
            {
                id: '222',
                identifier: '2222-2222-2222-2222',
                pageId: '456',
                status: DotExperimentStatusList.ARCHIVED,
                archived: false,
                readyToStart: false,
                description: 'Praesent at molestie mauris, quis vulputate augue.',
                name: 'Praesent at molestie mauris',
                trafficAllocation: 100,
                scheduling: null,
                trafficProportion: {
                    type: TrafficProportionTypes.SPLIT_EVENLY,
                    variants: [{ id: '222', name: 'DEFAULT', weight: 100.0 }]
                },
                creationDate: new Date('2022-08-21 14:50:03'),
                modDate: new Date('2022-08-21 18:50:03'),
                goals: { ...GoalsMock }
            }
        ];

        const expected: GroupedExperimentByStatus = {
            [DotExperimentStatusList.ENDED]: [...endedExperiments],
            [DotExperimentStatusList.ARCHIVED]: [...archivedExperiments]
        };

        store.setExperiments([...endedExperiments, ...archivedExperiments]);

        store.getExperimentsFilteredAndGroupedByStatus$.subscribe((exp) => {
            expect(exp).toEqual(expected);
        });
    });

    describe('Effects', () => {
        beforeEach(() => {
            dotExperimentsService.getAll.mockReturnValue(of(ExperimentMocks));

            store.initStore();
            store.loadExperiments(routerParamsPageId);
        });
        it('should load experiments to store', (done) => {
            expect(dotExperimentsService.getAll).toHaveBeenCalledWith(routerParamsPageId);
            store.getExperiments$.subscribe((exp) => {
                expect(exp).toEqual(ExperimentMocks);
                done();
            });
        });

        it('should delete experiment from the store', (done) => {
            dotExperimentsService.delete.mockReturnValue(of('deleted'));

            const expectedExperimentsInStore = [ExperimentMocks[1], ExperimentMocks[2]];
            const experimentToDelete = ExperimentMocks[0];

            store.deleteExperiment(experimentToDelete);

            expect(dotExperimentsService.delete).toHaveBeenCalled();
            expect(dotExperimentsService.delete).toHaveBeenCalledWith(ExperimentMocks[0].id);
            store.getExperiments$.subscribe((experiments) => {
                expect(experiments).toEqual(expectedExperimentsInStore);
                done();
            });
        });

        it('should change experiment status to archive in the store', (done) => {
            const expectedExperimentsInStore = [...ExperimentMocks];

            expectedExperimentsInStore[0].status = DotExperimentStatusList.ARCHIVED;

            dotExperimentsService.archive.mockReturnValue(of(expectedExperimentsInStore[0]));

            const experimentToArchive = ExperimentMocks[0];

            store.archiveExperiment(experimentToArchive);

            expect(dotExperimentsService.archive).toHaveBeenCalled();
            expect(dotExperimentsService.archive).toHaveBeenCalledWith(experimentToArchive.id);
            store.getExperiments$.subscribe((experiment) => {
                expect(experiment).toEqual(expectedExperimentsInStore);
                done();
            });
        });
    });
});
