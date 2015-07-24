'use strict';

angular.module('ludecolApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('image-manager', {
                parent: 'site',
                url: '/image-manager',
                data: {
                    roles: []
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/admin/image-manager/image-manager.html',
                        controller: 'ImageManagerController'
                    }
                },
                resolve: {
                    mainTranslatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate,$translatePartialLoader) {
                        $translatePartialLoader.addPart('main');
                        return $translate.refresh();
                    }]
                }
            });
    })
    .config(function ($stateProvider) {
        $stateProvider
            .state('image-manager-detail', {
                parent: 'site',
                url: '/image-manager/:set',
                data: {
                    roles: []
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/admin/image-manager/image-manager-detail.html',
                        controller: 'ImageManagerDetailController'
                    }
                }
            });
    });
