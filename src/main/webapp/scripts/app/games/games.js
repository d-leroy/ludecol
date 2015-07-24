'use strict';

angular.module('ludecolApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('games', {
                parent: 'account',
                url: '/games/:id',
                data: {
                    roles: ['ROLE_USER']
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/games/games.html',
                        controller: 'GamesController'
                    }
                }
            });
    });
