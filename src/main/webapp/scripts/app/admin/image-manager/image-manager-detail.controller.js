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
                $scope.flora_species = [{name: 'Batis', state: null}, {name: 'Borrichia', state: null},
                                        {name: 'Juncus', state: null}, {name: 'Limonium', state: null},
                                        {name: 'Salicornia', state: null}, {name: 'Spartina', state: null}];
                $scope.fauna_species = [{name: 'Burrow', state: null}, {name: 'Crab', state: null},
                                      {name: 'Mussel', state: null}, {name: 'Snail', state: null}];
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

                angular.forEach($scope.flora_species,function(value){
                    value.state = $scope.currentImage.flora_species.indexOf(value.name) !== -1;
                });

                angular.forEach($scope.fauna_species,function(value){
                    value.state = $scope.currentImage.fauna_species.indexOf(value.name) !== -1;
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

            $scope.clear();

            $scope.submit = function() {
                $scope.currentImage.game_modes = $scope.availableModes;
                angular.forEach($scope.modes,function(v) {$scope.currentImage.mode_status[v.name].status = v.state;});
                $scope.currentImage.flora_species = $scope.presentFlora;
                $scope.currentImage.fauna_species = $scope.presentFauna;
                Image.update($scope.currentImage,function(i) {
                    $('#editImageModal').modal('hide');
                })
            }

        });
    });
