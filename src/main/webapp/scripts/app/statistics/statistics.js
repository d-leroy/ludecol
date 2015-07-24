'use strict';

angular.module('ludecolApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('statistics', {
                parent: 'account',
                url: '/statistics',
                data: {
                    roles: ['ROLE_USER']
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/statistics/statistics.html',
                        controller: 'StatisticsController'
                    }
                }
            });
    });
