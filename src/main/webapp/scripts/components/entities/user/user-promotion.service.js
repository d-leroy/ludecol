'use strict';

angular.module('ludecolApp')
    .factory('UserPromotion', function ($resource) {
        return $resource('api/promote/:login', {}, {
            'update': { method:'PUT' }
        });
    });
