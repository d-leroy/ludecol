'use strict';

angular.module('ludecolApp')
    .controller('AllStarsController', function ($scope, Principal, MapService, GameService, RadioModel) {

        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
            $scope.errorMsg = null;

            $scope.plantSpecies = ['Batis','Borrichia','Juncus','Limonium','Salicornia','Spartina'];
            $scope.animalSpecies = ['Snail','Mussel','Crab','Burrow'];

            function errorCallback() {$scope.errorMsg = true; MapService.destroyMap();}

            function loadGame(img, game, force) {
                $scope.errorMsg = false;

                $scope.radioModels = {};
                angular.forEach($scope.plantSpecies,function(species) {$scope.radioModels[species] = 0;});
                angular.forEach($scope.animalSpecies,function(species) {$scope.radioModels[species] = 0;});
                RadioModel.data = $scope.radioModels;

                MapService.initializeMap('map',force);
                MapService.setView(img);
                MapService.addControl(controls);
                $scope.submit = function(){$scope.errorMsg = null; GameService.submitGame();};
                $scope.displayControls = true;
            }

            GameService.initializeGame($scope.account.login,'AllStars',function(){return {};},loadGame,errorCallback,true);
        });
    });
