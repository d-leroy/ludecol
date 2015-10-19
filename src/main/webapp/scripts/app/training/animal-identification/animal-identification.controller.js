'use strict';

angular.module('ludecolApp')

    .factory('ScoreboardService', function () {
        return {data: {}};
    })

    .controller('TrainingAnimalIdentificationController', function ($scope, Principal, ScoreboardService, TrainingAnimalGameService, FeatureCollection, RadioModel, ImageService) {

        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
            $scope.errorMsgAnimals = null;

            function errorCallback() {$scope.errorMsgAnimals = true; ImageService.destroyMap();}

            function loadGame(img,game) {
                $scope.errorMsgAnimals = false;

                FeatureCollection.Burrow = undefined;
                FeatureCollection.Crab = undefined;
                FeatureCollection.Mussel = undefined;
                FeatureCollection.Snail = undefined;

                angular.forEach(img.fauna_species,function(value) {
                    FeatureCollection[value] = [];
                    $scope.$watch('show'+value,function(n) {TrainingAnimalGameService.toggleFeatures(value,n);});
                    $scope['show'+value] = true;
                });

                $scope.radioModel = {selected: null};
                RadioModel.data = $scope.radioModel;

                ImageService.initializeMap('map');
                ImageService.setView(img);
                ImageService.addControl(controls);
                ImageService.addControl(scoreboard);
                $scope.submit = TrainingAnimalGameService.submitGame;
                $scope.skip = function(){$scope.errorMsgAnimals = null; TrainingAnimalGameService.skipGame();};

                $scope.isCompleted = function(key) {
                    var entry = ScoreboardService.data.animals[key];
                    return entry !== undefined && entry.max > 0 && entry.max === entry.nbConfirmed;
                }

                $scope.burrows = FeatureCollection.Burrow;
                $scope.crabs = FeatureCollection.Crab;
                $scope.mussels = FeatureCollection.Mussel;
                $scope.snails = FeatureCollection.Snail;
                $scope.scoreboard = ScoreboardService.data;
                $scope.displayControls = true;
                $scope.displayScoreboard = true;

                $scope.highlightFeature = TrainingAnimalGameService.highlightFeature;
                $scope.removeFeature = TrainingAnimalGameService.removeFeature;
                $scope.panToFeature = function(property,idx) {ImageService.panTo(FeatureCollection[property][idx].getGeometry().getCoordinates());}
            }

            TrainingAnimalGameService.initializeGame($scope.account.login,loadGame,errorCallback);

            $scope.showModal = function() {
                $('#'+$scope.radioModel.selected+'Modal').modal('show');
            }

        });
    });
