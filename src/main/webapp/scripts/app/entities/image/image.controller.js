'use strict';

angular.module('ludecolApp')
    .controller('ImageController', function ($scope, Image, ImageSet) {
        $scope.images = [];
        $scope.loadAll = function() {
            Image.query(function(result) {
               $scope.images = result;
            });
        };
        $scope.loadAll();

        $scope.create = function () {
            Image.update($scope.image,
                function () {
                    $scope.loadAll();
                    $('#saveImageModal').modal('hide');
                    $scope.clear();
                });
        };

        $scope.update = function (id) {
            Image.get({id: id}, function(result) {
                $scope.image = result;
                $('#saveImageModal').modal('show');
            });
        };

        $scope.delete = function (id) {
            Image.get({id: id}, function(result) {
                $scope.image = result;
                $('#deleteImageConfirmation').modal('show');
            });
        };

        $scope.confirmDelete = function (id) {
            Image.delete({id: id},
                function () {
                    $scope.loadAll();
                    $('#deleteImageConfirmation').modal('hide');
                    $scope.clear();
                });
        };

        $scope.clear = function () {
            $scope.image = {name: null, path: null, width: null, height: null, training: null, id: null};
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };
    });
