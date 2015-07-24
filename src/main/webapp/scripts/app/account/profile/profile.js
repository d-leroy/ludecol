'use strict';

angular.module('ludecolApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile', {
                parent: 'account',
                url: '/profile',
                data: {
                    roles: ['ROLE_USER']
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/account/profile/profile.html',
                        controller: 'ProfileController'
                    }
                }
            });
    });
