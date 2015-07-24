'use strict';

angular.module('ludecolApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('image', {
                parent: 'entity',
                url: '/image',
                data: {
                    roles: ['ROLE_USER'],
                    pageTitle: 'ludecolApp.image.home.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/image/images.html',
                        controller: 'ImageController'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('image');
                        return $translate.refresh();
                    }]
                }
            })
            .state('imageDetail', {
                parent: 'entity',
                url: '/image/:id',
                data: {
                    roles: ['ROLE_USER'],
                    pageTitle: 'ludecolApp.image.detail.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/image/image-detail.html',
                        controller: 'ImageDetailController'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('image');
                        return $translate.refresh();
                    }]
                }
            });
    });
