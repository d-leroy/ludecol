'use strict';

angular.module('ludecolApp')
    .factory('Objective', function ($resource) {
        return $resource('api/objectives', {}, {
            'query': { method: 'GET', isArray: true}
        });
    });
