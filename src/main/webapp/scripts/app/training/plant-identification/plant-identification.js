'use strict';

angular.module('ludecolApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('training-plant-identification', {
                parent: 'site',
                url: '/',
                data: {
                    roles: []
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/training/plant-identification/plant-identification.html',
                        controller: 'TrainingPlantIdentificationController'
                    }
                },
                resolve: {
                    mainTranslatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate,$translatePartialLoader) {
                        $translatePartialLoader.addPart('training');
                        return $translate.refresh();
                    }]
                }
            });
    });
