'use strict';

angular.module('ludecolApp')
    .filter('slice', function() {
      return function(arr, start, end) {
        return arr.slice(start, end);
      };
    })
    .directive('fileModel', function ($parse) {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {
                var model = $parse(attrs.fileModel);
                var modelSetter = model.assign;

                element.bind('change', function(){
                    scope.$apply(function(){
                        modelSetter(scope, element[0].files);
                    });
                });
            }
        };
    })
    .controller('ImageManagerDetailController', function ($scope, $stateParams, Principal, Image, FileUpload, MapService) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
            $scope.currentPage = 1;
            $scope.modes = [{name: 'AllStars', state: null}, {name:'AnimalIdentification', state: null}, {name:'PlantIdentification', state: null}];

            $scope.$watchCollection('files',function(n,o) {
                    angular.forEach(n,function(file){
                            $scope.filesToUpload.push(file);
                        });
                    });

            $scope.loadPage = function() {
                Image.get({set: $stateParams.set, page: $scope.currentPage-1}, function(res) {
                    $scope.totalItems = res.totalElements;
                    $scope.images = res.content;
                    $scope.itemsPerPage = res.size;
                });
            }

            var successCallback = function() {
                $scope.uploadedFiles.push($scope.currentFile);
                $scope.currentFile = null;
                if($scope.filesToUpload.length > 0) {
                    $scope.add();
                }
            }

            $scope.add = function() {
                $scope.currentFile = $scope.filesToUpload[0];
                $scope.filesToUpload.splice(0,1);
                var data = {
                    cols: $scope.cols,
                    rows: $scope.rows,
                    set: $stateParams.set,
                    name: $scope.currentFile.name,
                    file: $scope.currentFile
                }
                FileUpload.uploadFileToUrl(data,"/api/images",successCallback);
            }

            $scope.clear = function() {
                MapService.destroyMap();
                $scope.previewing = false;
                $scope.filesToUpload = [];
                $scope.currentFile = null;
                $scope.uploadedFiles = [];
                $scope.files = [];
                $scope.cols = 3;
                $scope.rows = 2;
                $scope.floraModel = {Batis: false, Borrichia: false, Juncus: false, Limonium: false, Salicornia: false, Spartina: false};
                $scope.faunaModel = {Burrow: false, Crab: false, Mussel: false, Snail: false};

                $scope.loadPage();
            }

            $scope.clearUploaded = function() {
                $scope.uploadedFiles = {};
            }

            $scope.setCurrentImage = function(i) {
                $scope.clear();
                $scope.currentImage = $scope.images[i];

                angular.forEach($scope.modes,function(value){
                    value.state = $scope.currentImage.mode_status[value.name].status;
                });

                angular.forEach($scope.floraModel,function(value,key){
                    $scope.floraModel[key] = $scope.currentImage.flora_species.indexOf(key) !== -1;
                });

                angular.forEach($scope.faunaModel,function(value,key){
                    $scope.faunaModel[key] = $scope.currentImage.fauna_species.indexOf(key) !== -1;
                });

                $scope.$watchCollection('floraModel', function () {
                    $scope.currentImage.flora_species = [];
                    angular.forEach($scope.floraModel, function (value, key) {
                        if (value) {
                            $scope.currentImage.flora_species.push(key);
                        }
                    });
                });

                $scope.$watchCollection('faunaModel', function () {
                    $scope.currentImage.fauna_species = [];
                    angular.forEach($scope.faunaModel, function (value, key) {
                        if (value) {
                            $scope.currentImage.fauna_species.push(key);
                        }
                    });
                });
            }

            $scope.showPreview = function(show) {
                $scope.previewing = show;
                if(show) {
                    MapService.initializeMap('preview');
                    MapService.setView($scope.currentImage);
                }
                else {
                    MapService.destroyMap();
                }
            }

            $scope.delete = function(i) {
                Image.delete({id: $scope.images[i].id},function(){$scope.clear();});
            }

            $scope.submit = function() {
                $scope.currentImage.game_modes = $scope.availableModes;
                angular.forEach($scope.modes,function(v) {$scope.currentImage.mode_status[v.name].status = v.state;});
                Image.update($scope.currentImage,function(i) {
                    $scope.clear();
                    $('#editImageModal').modal('hide');
                })
            }

            $scope.clear();

        });
    });
