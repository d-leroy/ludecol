'use strict';

angular.module('ludecolApp')
    .controller('TrainingPlantIdentificationController', function ($scope, Principal, ScoreboardService, TrainingPlantGameService, RadioModel, ImageService) {
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
                    $scope.$watch('show'+value,function(n) {TrainingPlantGameService.toggleFeatures(value,n);});
                    $scope['show'+value] = true;
                });

                ImageService.initializeMap('map');
                ImageService.setView(img);
                ImageService.addControl(controls);
                ImageService.addControl(options);
                ImageService.addControl(scoreboard);
                $scope.submit = TrainingPlantGameService.submitGame;
                $scope.skip = function(){$scope.errorMsg = null; TrainingPlantGameService.skipGame();};

                $scope.isCompleted = function(key) {
                    var entry = ScoreboardService.data.plants[key];
                    return entry !== undefined && entry.max > 0 && entry.max === entry.nbConfirmed;
                }

                $scope.scoreboard = ScoreboardService.data;
                $scope.displayOptions = true;
                $scope.displayControls = true;
                $scope.displayScoreboard = true;
            }

            TrainingPlantGameService.initializeGame($scope.account.login,nbCols,nbRows,loadGame,errorCallback);

            $scope.showModal = function() {
                $('#'+$scope.radioModel.selected+'Modal').modal('show');
            }
        });
    });
