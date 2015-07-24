'use strict';

angular.module('ludecolApp')
    .factory('Register', function ($resource) {
        return $resource('api/register', {}, {
        });
    });


