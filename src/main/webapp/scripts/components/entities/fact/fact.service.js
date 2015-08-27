'use strict';

angular.module('ludecolApp')
    .factory('Fact', function ($resource) {
        return $resource('api/fact', {}, {
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            }
        });
    });
