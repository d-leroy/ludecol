'use strict';

angular.module('ludecolApp')
    .controller('AllStarsController', function ($scope, Principal, ImageService, AllStarsGameService, RadioModel) {

        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
            $scope.errorMsg = null;

            $scope.plantSpecies = ['Batis','Borrichia','Juncus','Limonium','Salicornia','Spartina'];
            $scope.animalSpecies = ['Snail','Mussel','Crab','Burrow'];

            function errorCallback() {$scope.errorMsg = true; ImageService.destroyMap();}

            function loadGame(img, game) {
                $scope.errorMsg = false;

                $scope.radioModels = {};
                angular.forEach($scope.plantSpecies,function(species) {$scope.radioModels[species] = 0;});
                angular.forEach($scope.animalSpecies,function(species) {$scope.radioModels[species] = 0;});
                RadioModel.data = $scope.radioModels;

                ImageService.initializeMap('map');
                ImageService.setView(img);
                ImageService.addControl(controls);
                $scope.submit = function(){$scope.errorMsg = null; AllStarsGameService.submitGame();};
                $scope.displayControls = true;
            }

            AllStarsGameService.initializeGame($scope.account.login,loadGame,errorCallback);
        });
    });
