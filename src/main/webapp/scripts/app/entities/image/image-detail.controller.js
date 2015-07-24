'use strict';

angular.module('ludecolApp')
    .controller('ImageDetailController', function ($scope, $stateParams, Image, Feature) {
        $scope.image = {};
        $scope.load = function (id) {
            Image.get({id: id}, function(result) {
              $scope.image = result;
            });
        };
        $scope.load($stateParams.id);
    });
