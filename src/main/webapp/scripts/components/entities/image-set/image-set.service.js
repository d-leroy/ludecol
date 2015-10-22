'use strict';

angular.module('ludecolApp')
    .factory('ImageSet', function ($resource) {
        return $resource('api/imagesets/:id', {}, {
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
    .factory('ImageSetDownload', function ($resource) {
        return $resource('api/imagesets/:id/download', {}, {
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            }
        });
    });
