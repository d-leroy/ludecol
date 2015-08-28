'use strict';

angular.module('ludecolApp')
    .controller('ReferenceDefinitionAnimalIdentificationController', function ($scope, $state, $stateParams, Principal, ImageService, RadioModel, FeatureCollection, ExpertAnimalGameService) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
            $scope.errorMsg = null;

            function errorCallback() {$scope.errorMsg = true; ImageService.destroyMap();}

            function initializeFeatureCollection() {
                return {
                    Burrow: [],
                    Crab: [],
                    Mussel: [],
                    Snail: []
                }
            }

            function loadGame(img,game) {
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

                ImageService.initializeMap('map');
                ImageService.setView(img);
                ImageService.addControl(controls);
                ImageService.addControl(options);

                $scope.submit = function(){$scope.errorMsg = null; ExpertAnimalGameService.submitGame();};

                $scope.burrows = FeatureCollection.Burrow;
                $scope.crabs = FeatureCollection.Crab;
                $scope.mussels = FeatureCollection.Mussel;
                $scope.snails = FeatureCollection.Snail;
                $scope.displayControls = true;
                $scope.displayOptions = true;

                $scope.highlightFeature = ExpertAnimalGameService.highlightFeature;
                $scope.removeFeature = ExpertAnimalGameService.removeFeature;
                $scope.panToFeature = function(property,idx) {ImageService.panTo(FeatureCollection[property][idx].getGeometry().getCoordinates());}
            }

            ExpertAnimalGameService.initializeReferenceDefinition($scope.account.login,loadGame,errorCallback,$stateParams.img,function(){$state.go('image-manager',{set:$stateParams.set})});
        });
    });
