'use strict';

angular.module('ludecolApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('game-history', {
                parent: 'account',
                url: '/game-history',
                data: {
                    roles: ['ROLE_USER']
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/game-history/game-history.html',
                        controller: 'GameHistoryController'
                    }
                }
            });
    });
