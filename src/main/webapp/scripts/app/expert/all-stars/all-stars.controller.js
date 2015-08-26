'use strict';

angular.module('ludecolApp')
    .controller('ExpertAllStarsController', function ($scope, Principal, MapService, ExpertAllStarsGameService, RadioModel) {
        Principal.identity().then(function(account) {

            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
            $scope.errorMsg = null;
            $scope.radioModels = {};

            $scope.plantSpecies = ['Batis','Borrichia','Juncus','Limonium','Salicornia','Spartina'];
            $scope.animalSpecies = ['Snail','Mussel','Crab','Burrow'];

            $scope.speciesStyles = {};
            angular.forEach($scope.plantSpecies,function(v){
                $scope.radioModels[v] = 0;
                $scope.speciesStyles[v] = {
                    'position': 'absolute', 'top': '-11px', 'left': '-16px', 'width': '0', 'height': '50px', 'background-color': '#5CBB5C', 'z-index': '1', 'border-radius': '3px'
                };
            });
            angular.forEach($scope.animalSpecies,function(v){
                $scope.radioModels[v] = 0;
                $scope.speciesStyles[v] = {
                    'position': 'absolute', 'top': '-11px', 'left': '-16px', 'width': '0', 'height': '50px', 'background-color': '#5CBB5C', 'z-index': '1', 'border-radius': '3px'
                };
            });

            function errorCallback() {$scope.errorMsg = true; MapService.destroyMap();}

            function loadGame(img, game) {
                $scope.errorMsg = false;

                $scope.radioModels = {};
                angular.forEach($scope.plantSpecies,function(species) {$scope.radioModels[species] = 0;});
                angular.forEach($scope.animalSpecies,function(species) {$scope.radioModels[species] = 0;});
                RadioModel.data = $scope.radioModels;

                MapService.initializeMap('map');
                MapService.setView(img);
                MapService.addControl(controls);
                $scope.submit = function(){$scope.errorMsg = null; ExpertAllStarsGameService.submitGame();};
                $scope.displayControls = true;

                angular.forEach(game.processed_result.species_map,function(v,k){
                    var x = v.x;
                    var y = v.y;
                    if(x+y !== 0) {
                        var width = (x / (x+y)) * 165;
                        $scope.speciesStyles[k].width = width.toString() + "px";
                    }
                });
            }

            ExpertAllStarsGameService.initializeGame($scope.account.login,loadGame,errorCallback);

        });
    });
