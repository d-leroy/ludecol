'use strict';

angular.module('ludecolApp')

    .factory('ScoreboardService', function () {
        return {data: {}};
    })

    .controller('TrainingAnimalIdentificationController', function ($scope, Principal, ScoreboardService, TrainingAnimalGameService, FeatureCollection, RadioModel, MapService) {

        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
            $scope.errorMsg = null;

            function errorCallback() {$scope.errorMsg = true; MapService.destroyMap();}

            function loadGame(img,game) {
                $scope.errorMsg = false;

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

                MapService.initializeMap('map');
                MapService.setView(img);
                MapService.addControl(controls);
                MapService.addControl(scoreboard);
                $scope.submit = TrainingAnimalGameService.submitGame;

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
                $scope.panToFeature = function(property,idx) {MapService.panTo(FeatureCollection[property][idx].getGeometry().getCoordinates());}
            }

            TrainingAnimalGameService.initializeGame($scope.account.login,loadGame,errorCallback);

        });
    });
