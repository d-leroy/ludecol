'use strict';

angular.module('ludecolApp')
    .factory('Image', function ($resource) {
        return $resource('api/images/:id', {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            'update': { method:'PUT' },
            'upload': {
                method:'POST',
                transformRequest: angular.identity,
                headers: {'Content-Type': undefined}
            },
            'delete': {method: 'DELETE'}
        });
    });
