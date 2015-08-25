'use strict';

angular.module('ludecolApp')
    .controller('PlantIdentificationController', function ($scope, Principal, MapService, RadioModel, PlantGameService) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
            $scope.errorMsg = null;

            var nbRows = 3;
            var nbCols = 3;

            function errorCallback() {$scope.errorMsg = true; MapService.destroyMap();}

            function loadGame(img,game) {
                $scope.errorMsg = false;

                $scope.radioModel = {selected: null};
                RadioModel.data = $scope.radioModel;

                $scope.Salicornia = undefined;
                $scope.Spartina = undefined;
                $scope.Batis = undefined;
                $scope.Borrichia = undefined;
                $scope.Juncus = undefined;
                $scope.Limonium= undefined;

                angular.forEach(img.flora_species,function(value) {
                    $scope[value] = true;
                    $scope.$watch('show'+value,function(n) {PlantGameService.toggleFeatures(value,n);});
                    $scope['show'+value] = true;
                });

                MapService.initializeMap('map');
                MapService.setView(img);
                MapService.addControl(controls);
                MapService.addControl(options);
                $scope.submit = function(){$scope.errorMsg = null; PlantGameService.submitGame();};

                $scope.jokerDisabled = true;
                $scope.displayControls = true;
                $scope.displayOptions = true;

            }

            PlantGameService.initializeGame($scope.account.login,nbCols,nbRows,loadGame,errorCallback);
        });
    });
