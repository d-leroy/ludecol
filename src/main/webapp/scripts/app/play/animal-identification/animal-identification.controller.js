'use strict';

angular.module('ludecolApp')

    .factory('RadioModel', function () {
        return {data: null};
    })

    .factory('FeatureCollection', function () {
        return {Burrow: null, Crab: null, Mussel: null, Snail: null};
    })

    .controller('AnimalIdentificationController', function ($scope, Principal, MapService, GameService, RadioModel, FeatureCollection, AnimalGameService) {

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

                angular.forEach(img.fauna_species,function(value) {
                    FeatureCollection[value] = [];
                    $scope.$watch('show'+value,function(n) {AnimalGameService.toggleFeatures(value,n);});
                    $scope['show'+value] = true;
                });

                $scope.radioModel = {selected: null};
                RadioModel.data = $scope.radioModel;

                MapService.initializeMap('map',force);
                MapService.setView(img);
                MapService.addControl(controls);
                MapService.addControl(options);
                MapService.setupGame();
                $scope.submit = function(){$scope.errorMsg = null; GameService.submitGame();};

                $scope.burrows = FeatureCollection.Burrow;
                $scope.crabs = FeatureCollection.Crab;
                $scope.mussels = FeatureCollection.Mussel;
                $scope.snails = FeatureCollection.Snail;
                $scope.displayControls = true;
                $scope.displayOptions = true;

                $scope.highlightFeature = AnimalGameService.highlightFeature;
                $scope.removeFeature = AnimalGameService.removeFeature;
                $scope.panToFeature = function(property,idx) {MapService.panTo(FeatureCollection[property][idx].getGeometry().getCoordinates());}
            }

            GameService.initializeGame($scope.account.login,'AnimalIdentification',initializeFeatureCollection,loadGame,errorCallback,true);
        });
    });
