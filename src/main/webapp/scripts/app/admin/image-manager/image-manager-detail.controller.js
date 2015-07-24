'use strict';

angular.module('ludecolApp')
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
                $scope.unavailableModes = [
                    'AllStars', 'ExpertAllStars', 'AnimalIdentification', 'ExpertAnimalIdentification', 'TrainingAnimalIdentification',
                    'PlantIdentification', 'ExpertPlantIdentification', 'TrainingPlantIdentification'
                ];
                $scope.availableModes = [];
                $scope.absentFlora = ['Batis', 'Borrichia', 'Juncus', 'Limonium', 'Salicornia', 'Spartina'];
                $scope.presentFlora = [];
                $scope.absentFauna = ['Burrow', 'Crab', 'Mussel', 'Snail'];
                $scope.presentFauna = [];
                $scope.loadPage();
            }

            $scope.clearUploaded = function() {
                $scope.uploadedFiles = [];
            }

            $scope.setCurrentImage = function(i) {
                $scope.clear();
                $scope.currentImage = $scope.images[i];
                angular.forEach($scope.currentImage.game_modes,function(value){
                    var i = $scope.unavailableModes.indexOf(value);
                    if(i !== -1) {
                        $scope.unavailableModes.splice(i,1);
                        $scope.availableModes.push(value);
                    }
                });
                angular.forEach($scope.currentImage.flora_species,function(value){
                    var i = $scope.absentFlora.indexOf(value);
                    if(i !== -1) {
                        $scope.absentFlora.splice(i,1);
                        $scope.presentFlora.push(value);
                    }
                });
                angular.forEach($scope.currentImage.fauna_species,function(value){
                    var i = $scope.absentFauna.indexOf(value);
                    if(i !== -1) {
                        $scope.absentFauna.splice(i,1);
                        $scope.presentFauna.push(value);
                    }
                });
            }

            $scope.addToAvailable = function(i) {
                var value = $scope.unavailableModes[i];
                $scope.unavailableModes.splice(i,1);
                $scope.availableModes.push(value);
            }

            $scope.addToUnavailable = function(i) {
                var value = $scope.availableModes[i];
                $scope.availableModes.splice(i,1);
                $scope.unavailableModes.push(value);
            }

            $scope.addToPresentFlora = function(i) {
                var value = $scope.absentFlora[i];
                $scope.absentFlora.splice(i,1);
                $scope.presentFlora.push(value);
            }

            $scope.addToAbsentFlora = function(i) {
                var value = $scope.presentFlora[i];
                $scope.presentFlora.splice(i,1);
                $scope.absentFlora.push(value);
            }

            $scope.addToPresentFauna = function(i) {
                var value = $scope.absentFauna[i];
                $scope.absentFauna.splice(i,1);
                $scope.presentFauna.push(value);
            }

            $scope.addToAbsentFauna = function(i) {
                var value = $scope.presentFauna[i];
                $scope.presentFauna.splice(i,1);
                $scope.absentFauna.push(value);
            }

            $scope.showPreview = function(show) {
                $scope.previewing = show;
                if(show) {
                    MapService.initializeMap('preview',true);
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
                $scope.currentImage.flora_species = $scope.presentFlora;
                $scope.currentImage.fauna_species = $scope.presentFauna;
                Image.update($scope.currentImage,function() {
                    $('#editImageModal').modal('hide');
                })
            }

        });
    });
