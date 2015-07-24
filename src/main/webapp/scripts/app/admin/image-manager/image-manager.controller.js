'use strict';

angular.module('ludecolApp')
    .controller('ImageManagerController', function ($scope, Principal, Image, ImageSet) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;

            $scope.folders = [];

            $scope.loadAll = function() {
                ImageSet.query(function(result) {
                    $scope.folders = result;
                });
            };

            $scope.loadAll();

            $scope.clear = function () {
                $scope.imageSet = {name: null, id: null};
                $scope.editForm.$setPristine();
                $scope.editForm.$setUntouched();
            };

            $scope.create = function () {
                ImageSet.update($scope.imageSet,
                    function () {
                        $scope.loadAll();
                        $('#saveImageSetModal').modal('hide');
                        $scope.clear();
                    });
            };
        });
    });
