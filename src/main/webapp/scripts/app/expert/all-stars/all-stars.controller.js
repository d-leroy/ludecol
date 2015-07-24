'use strict';

angular.module('ludecolApp')
    .controller('ExpertAllStarsController', function ($scope, $compile, $state, Principal, Image, ExpertGame, UserExpertGame, SubmitModalService) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;

            var imgWidth;
            var imgHeight;
            var imgCenter;

            var map;

            $scope.plantSpecies = ['Batis','Borrichia','Juncus','Limonium','Salicornia','Spartina'];
            $scope.animalSpecies = ['Snail','Mussel','Crab','Burrow'];
            $scope.radioModels = {};
            $scope.speciesStyles = {};
            for(var i=0, ii=$scope.plantSpecies.length;i<ii;i++) {
                $scope.radioModels[$scope.plantSpecies[i]] = 0;
                $scope.speciesStyles[$scope.plantSpecies[i]] = {
                    'position': 'absolute', 'top': '-11px', 'left': '-16px', 'width': '0', 'height': '50px', 'background-color': '#5CBB5C', 'z-index': '1', 'border-radius': '3px'
                };

            }
            for(var i=0, ii=$scope.animalSpecies.length;i<ii;i++) {
                $scope.radioModels[$scope.animalSpecies[i]] = 0;
                $scope.speciesStyles[$scope.animalSpecies[i]] = {
                    'position': 'absolute', 'top': '-11px', 'left': '-16px', 'width': '0', 'height': '50px', 'background-color': '#5CBB5C', 'z-index': '1', 'border-radius': '3px'
                };
            }

            UserExpertGame.query({login: $scope.account.login, completed: false, mode: 'ExpertAllStars'}, function(result) {
                if(result.length === 0) {
                    var game = {
                        usr: $scope.account.login,
                        game_mode: 'ExpertAllStars',
                        game_result: $scope.radioModels,
                        completed: false
                    };
                    ExpertGame.update(game, function(f,g) {
                        var header = g().location.split("/");
                        var nId = header[header.length - 1];
                        Image.get({id: f.img}, function(i) {loadGame(i, f)});
                    });
                }
                else {
                    Image.get({id: result[0].img}, function(i) {loadGame(i, result[0])});
                }
            });

            function loadGame(img, game) {

                var species_map = game.processed_result.species_map;

                for (var property in species_map) {
                    if (species_map.hasOwnProperty(property) && property !== '__proto__') {
                        var x = species_map[property].x;
                        var y = species_map[property].y;
                        if(x+y !== 0) {
                            var width = (x / (x+y)) * 165;
                            $scope.speciesStyles[property].width = width.toString() + "px";
                        }
                    }
                }

                $scope.img = img;

                imgWidth = $scope.img.width;
                imgHeight = $scope.img.height;
                imgCenter = [imgWidth / 2, - imgHeight / 2];

                var tileSource = new ol.source.Zoomify({
                    url: $scope.img.path,
                    size: [imgWidth, imgHeight],
                    crossOrigin: 'anonymous'
                });
                var tileLayer = new ol.layer.Tile({source: tileSource});

                map = new ol.Map({
                    controls: ol.control.defaults().extend([
                        new ol.control.FullScreen()
                    ]),
                    layers: [tileLayer],
                    target: 'map',
                    view: new ol.View({
                        projection: new ol.proj.Projection({
                            code: 'ZOOMIFY',
                            units: 'pixels',
                            extent: [0, 0, imgWidth, imgHeight]
                        }),
                        center: imgCenter,
                        zoom: 2,
                        extent: [0, -imgHeight, imgWidth, 0]
                    }),
                    logo: false
                });

                map.addControl(new ol.control.Control({element: controls}));

                $scope.submit = function() {
                    game.game_result = $scope.radioModels;
                    game.completed = true;
                    ExpertGame.update(game, function() {
                        var modalInstance = SubmitModalService();
                        modalInstance.result.then(
                            function (newGame) {
                                if(newGame) {$state.reload()}
                                else {$state.go('home')}
                            },
                            function () {

                            });
                    });
                }

                $scope.displayControls = true;
                $scope.toggleControls = function() {$scope.displayControls = !$scope.displayControls}
            }
        });
    });
