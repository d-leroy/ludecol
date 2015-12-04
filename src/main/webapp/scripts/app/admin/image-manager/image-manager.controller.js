'use strict';

angular.module('ludecolApp')
    .controller('ImageManagerController', function ($scope, Principal, Image, ImageSet, ImageDownload) {
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

            $scope.encode = function(text) {
                return encodeURIComponent(text);
            }

            $scope.download = function() {
                ImageDownload.get({}, function(res){
                    $scope.downloadData = res.result;
                });
            }

            $scope.clear = function () {
                $scope.imageSet = {name: null, id: null, priority: 0, required_submissions: 3};
                $scope.editForm.$setPristine();
                $scope.editForm.$setUntouched();
                $scope.downloadData = null;
                $scope.loadAll();
            };

            $scope.create = function () {
                ImageSet.update($scope.imageSet,
                    function() {
                        $('#saveImageSetModal').modal('hide');
                        $scope.clear();
                    });
            };

            $scope.setImageSet = function(i) {
                $scope.imageSet = $scope.folders[i];
            }

            $scope.delete = function() {
                ImageSet.delete({id: $scope.imageSet.id},
                    function() {
                        $('#deleteImageSetModal').modal('hide');
                        $scope.clear();
                    });
            }
        });
    });
