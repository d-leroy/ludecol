'use strict';

angular.module('ludecolApp')
    .controller('ExpertPlantIdentificationController', function ($scope, Principal, ImageService, RadioModel, ExpertPlantGameService) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
            $scope.errorMsg = null;

            var nbRows = 3;
            var nbCols = 3;

            function errorCallback() {$scope.errorMsg = true; ImageService.destroyMap();}

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
                    $scope.$watch('show'+value,function(n) {ExpertPlantGameService.toggleFeatures(value,n);});
                    $scope['show'+value] = true;
                });

                ImageService.initializeMap('map');
                ImageService.setView(img);
                ImageService.addControl(controls);
                ImageService.addControl(options);
                $scope.submit = function(){$scope.errorMsg = null; ExpertPlantGameService.submitGame();};

                $scope.jokerDisabled = true;
                $scope.displayControls = true;
                $scope.displayOptions = true;

            }

            ExpertPlantGameService.initializeGame($scope.account.login,nbCols,nbRows,loadGame,errorCallback);
        });
    });
