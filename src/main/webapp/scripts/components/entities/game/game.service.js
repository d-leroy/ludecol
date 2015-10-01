'use strict';

angular.module('ludecolApp')
    .factory('Game', function ($resource) {
        return $resource('api/games/:id', {}, {
            'delete': { method: 'DELETE' },
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    })
    .factory('UserTrainingGame', function ($resource) {
        return $resource('api/users/traingames', {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            }
        });
    })
    .factory('UserGame', function ($resource) {
        return $resource('api/users/games', {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            }
        });
    })
    .factory('UserPagedGame', function ($resource) {
        return $resource('api/users/pagedgames', {}, {
            'query': { method: 'GET', isArray: false},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            }
        });
    })
    .factory('UserExpertGame', function ($resource) {
        return $resource('api/users/expertgames', {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            }
        });
    })
    .factory('TrainingGame', function ($resource) {
        return $resource('api/traingames/:id', {}, {
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    })
    .factory('ExpertGame', function ($resource) {
        return $resource('api/expertgames', {}, {
            'update': { method:'PUT' }
        });
    })
    .factory('UserLastCompletedGame', function ($resource) {
        return $resource('api/games/last', {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            }
        });
    });
