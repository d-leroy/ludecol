'use strict';

angular.module('ludecolApp')
    .controller('ExpertAnimalIdentificationController', function ($scope, Principal, MapService, GameService, RadioModel, FeatureCollection, ExpertAnimalGameService) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
            $scope.errorMsg = null;

            function errorCallback() {$scope.errorMsg = true; MapService.destroyMap();}

            function initializeFeatureCollection() {
                return {
                    Burrow: [],
                    Crab: [],
                    Mussel: [],
                    Snail: []
                }
            }

            function loadGame(img,game,force) {
                $scope.errorMsg = false;

                FeatureCollection.Burrow = undefined;
                FeatureCollection.Crab = undefined;
                FeatureCollection.Mussel = undefined;
                FeatureCollection.Snail = undefined;

                angular.forEach(['Burrow','Crab','Mussel','Snail'],function(value) {
                    FeatureCollection[value] = [];
                    $scope.$watch('show'+value,function(n) {ExpertAnimalGameService.toggleFeatures(value,n);});
                    $scope['show'+value] = true;
                });

                $scope.radioModel = {selected: null};
                RadioModel.data = $scope.radioModel;

                MapService.initializeMap('map',force);
                MapService.setView(img);
                MapService.addControl(controls);
                MapService.addControl(options);
                MapService.setupGame({species_map: game.processed_result.species_map});
                $scope.submit = function(){$scope.errorMsg = null; GameService.submitGame();};

                $scope.burrows = FeatureCollection.Burrow;
                $scope.crabs = FeatureCollection.Crab;
                $scope.mussels = FeatureCollection.Mussel;
                $scope.snails = FeatureCollection.Snail;
                $scope.displayControls = true;
                $scope.displayOptions = true;

                $scope.highlightFeature = ExpertAnimalGameService.highlightFeature;
                $scope.removeFeature = ExpertAnimalGameService.removeFeature;
                $scope.panToFeature = function(property,idx) {MapService.panTo(FeatureCollection[property][idx].getGeometry().getCoordinates());}
            }

            GameService.initializeGame($scope.account.login,'ExpertAnimalIdentification',initializeFeatureCollection,loadGame,errorCallback,true);
        });
    });
