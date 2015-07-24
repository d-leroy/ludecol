'use strict';

angular.module('ludecolApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('training-animal-identification', {
                parent: 'site',
                url: '/',
                data: {
                    roles: []
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/training/animal-identification/animal-identification.html',
                        controller: 'TrainingAnimalIdentificationController'
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
