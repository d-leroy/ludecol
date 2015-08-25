'use strict';

angular.module('ludecolApp')

    .factory('RadioModel', function () {
        return {data: null};
    })

    .factory('FeatureCollection', function () {
        return {Burrow: [], Crab: [], Mussel: [], Snail: []};
    })

    .controller('AnimalIdentificationController', function ($scope, Principal, MapService, RadioModel, FeatureCollection, AnimalGameService) {

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
                    $scope.$watch('show'+value,function(n) {AnimalGameService.toggleFeatures(value,n);});
                    $scope['show'+value] = true;
                });

                $scope.radioModel = {selected: null};
                RadioModel.data = $scope.radioModel;

                MapService.initializeMap('map');
                MapService.setView(img);
                MapService.addControl(controls);
                MapService.addControl(options);
                $scope.submit = function(){$scope.errorMsg = null; AnimalGameService.submitGame();};

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

            AnimalGameService.initializeGame($scope.account.login,loadGame,errorCallback);
        });
    });
