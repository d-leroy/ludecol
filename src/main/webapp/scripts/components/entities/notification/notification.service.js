'use strict';

angular.module('ludecolApp')
    .factory('Notification', function ($resource) {
        return $resource('api/notifications', {}, {
            'query': { method: 'GET', isArray: true},
            'update': { method:'PUT' }
        });
    });
