'use strict';

angular.module('ludecolApp')
    .controller('PlantIdentificationController', function ($scope, Principal, MapService, GameService, RadioModel, PlantGameService) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
            $scope.errorMsg = null;

            var nbRows = 3;
            var nbCols = 3;

            function initializePresenceGrid() {
                var res = {
                    Batis: [], Borrichia: [], Juncus: [],
                    Limonium: [], Salicornia: [], Spartina: []
                };
                for(var i=0; i<nbCols*nbRows; i++) {
                    for (var property in res) {
                        if (res.hasOwnProperty(property)) {
                            res[property].push(false);
                        }
                    }
                }
                return res;
            }

            function errorCallback() {$scope.errorMsg = true; MapService.destroyMap();}

            function loadGame(img,game,force) {
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

                MapService.initializeMap('map',force);
                MapService.setView(img);
                MapService.addControl(controls);
                MapService.addControl(options);
                MapService.setupGame({cols:nbCols,rows:nbRows});
                $scope.submit = function(){$scope.errorMsg = null; GameService.submitGame();};

                $scope.jokerDisabled = true;
                $scope.displayControls = true;
                $scope.displayOptions = true;

            }

            GameService.initializeGame($scope.account.login,'PlantIdentification',initializePresenceGrid,loadGame,errorCallback,true);
        });
    });
