'use strict';

angular.module('ludecolApp')

    .factory('RadioModel', function () {
        return {data: null};
    })

    .factory('FeatureCollection', function () {
        return {Burrow: [], Crab: [], Mussel: [], Snail: []};
    })

    .controller('AnimalIdentificationController', function ($scope, Principal, ImageService, RadioModel, FeatureCollection, AnimalGameService) {

        $scope.allTabs = ['Uca','Sesarma','Armases','Eurytium','Callinectes'];
        $scope.selectedTab = 'Uca';
        $scope.selectTab = function(tab) {$scope.selectedTab = tab; console.log(tab);}

        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
            $scope.errorMsg = null;

            function errorCallback() {$scope.errorMsg = true; ImageService.destroyMap();}

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

                ImageService.initializeMap('map');
                ImageService.setView(img);
                ImageService.addControl(controls);
                ImageService.addControl(options);
                $scope.submit = function(){$scope.errorMsg = null; AnimalGameService.submitGame();};
                $scope.skip = function(){$scope.errorMsg = null; AnimalGameService.skipGame();};

                $scope.burrows = FeatureCollection.Burrow;
                $scope.crabs = FeatureCollection.Crab;
                $scope.mussels = FeatureCollection.Mussel;
                $scope.snails = FeatureCollection.Snail;
                $scope.displayControls = true;
                $scope.displayOptions = true;

                $scope.highlightFeature = AnimalGameService.highlightFeature;
                $scope.removeFeature = AnimalGameService.removeFeature;
                $scope.panToFeature = function(property,idx) {ImageService.panTo(FeatureCollection[property][idx].getGeometry().getCoordinates());}
            }

            AnimalGameService.initializeGame($scope.account.login,loadGame,errorCallback);

            $scope.showModal = function() {
                $('#'+$scope.radioModel.selected+'Modal').modal('show');
            }
        });
    });
