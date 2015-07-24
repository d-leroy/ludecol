'use strict';

angular.module('ludecolApp')
    .controller('TrainingAnimalIdentificationController', function ($scope, $compile, $state, User, Principal, Image, TrainingGame, UserTrainingGame, TrainingSubmitModalService) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;

            var imgWidth;
            var imgHeight;
            var imgCenter;

            var map;

            var vectorSource = new ol.source.Vector();
            var vectorLayer = new ol.layer.Vector({source: vectorSource});

            function isWithinBounds(coord) {
                return coord[0] >= 0 && coord[0] <= $scope.img.width &&
                 coord[1] <= 0 && coord[1] >= -$scope.img.height;
            }

            function toLocation(features) {
                var res = [];
                for(var i=0, ii = features.length; i<ii; i++) {
                    res[i] = features[i].getGeometry().getCoordinates();
                }
                return res;
            }

            var speciesStyles = {
                Burrow: new ol.style.Style({image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({anchor: [0.5, 1],src: 'images/icon-purple.png'}))}),
                Crab: new ol.style.Style({image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({anchor: [0.5, 1],src: 'images/icon-yellow.png'}))}),
                Mussel: new ol.style.Style({image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({anchor: [0.5, 1],src: 'images/icon-green.png'}))}),
                Snail: new ol.style.Style({image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({anchor: [0.5, 1],src: 'images/icon-blue.png'}))})
            }

            var highlightStyle = new ol.style.Style({image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({anchor: [0.5, 1],src: 'images/icon-red.png',}))});

            function populateFeatures(property,coordinates,style) {
                var result = [];
                for(var i=0, ii=coordinates.length;i<ii;i++) {
                    var feat = new ol.Feature({
                        geometry: new ol.geom.Point(coordinates[i])
                    });
                    feat.validated = true;
                    feat.setStyle(style);
                    vectorSource.addFeature(feat);
                    result.push(feat);
                }
                return result;
            }

            function loadState(data) {
                $scope.radioModel = null;
                $scope.score = data.score;
                $scope.animals = {};

                $scope.species = {Burrow: [],Crab: [],Mussel: [],Snail: []};

                for(var property in data.partialResult) {
                    if (data.partialResult.hasOwnProperty(property) && property !== '__proto__') {
                        if(data.maxSpecies[property] > 0) {
                            $scope.species[property] = populateFeatures(property,data.partialResult[property],speciesStyles[property]);
                            $scope.animals[property] = {
                                name: ""+property,
                                nbConfirmed: data.maxSpecies[property] - data.missingSpecies[property],
                                nbToSubmit: 0,
                                max: data.maxSpecies[property],
                            };
                        }
                    }
                }
            }

            function loadGame(img, wrapper) {

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

                var view = new ol.View({
                    projection: new ol.proj.Projection({
                        code: 'ZOOMIFY',
                        units: 'pixels',
                        extent: [0, 0, imgWidth, imgHeight]
                    }),
                    center: imgCenter,
                    zoom: 2,
                    extent: [0, -imgHeight, imgWidth, 0]
                });

                map = new ol.Map({
                    controls: ol.control.defaults().extend([
                        new ol.control.FullScreen()
                    ]),
                    layers: [tileLayer, vectorLayer],
                    target: 'map',
                    view: view,
                    logo: false
                });

                map.addControl(new ol.control.Control({element: controls}));
                map.addControl(new ol.control.Control({element: scoreboard}));

                map.on('singleclick', function(evt) {
                    if(isWithinBounds(evt.coordinate)) {
                        if($scope.radioModel !== null) {
                            var feat = new ol.Feature({
                                geometry: new ol.geom.Point([evt.coordinate[0],evt.coordinate[1]])
                            });
                            feat.validated = false;
                            feat.setStyle(speciesStyles[$scope.radioModel]);
                            $scope.species[$scope.radioModel].push(feat);
                            $scope.animals[$scope.radioModel].nbToSubmit++;
                            $scope.$apply();
                            vectorSource.addFeature(feat);
                        } else {
                            console.log("radioModel is null");
                        }
                    }
                });

                $scope.submit = function() {
                    var game = {
                        usr: $scope.account.login,
                        img: wrapper.img,
                        id: wrapper.id,
                        game_mode: 'TrainingAnimalIdentification',
                        game_result: {
                            Snail: toLocation($scope.species.Snail),
                            Mussel: toLocation($scope.species.Mussel),
                            Crab: toLocation($scope.species.Crab),
                            Burrow: toLocation($scope.species.Burrow)
                        }
                    };
                    TrainingGame.update(game, function(res) {
                        TrainingGame.get({id: res.id}, function(updatedWrapper) {
                            if(updatedWrapper.completed) {
                                loadState(updatedWrapper);
                                var modalInstance = TrainingSubmitModalService();
                                modalInstance.result.then(
                                    function (newGame) {
                                        if(newGame) {$state.reload()}
                                        else {$state.go('home')}
                                    },
                                    function () {

                                    });
                            }
                            else {
                                vectorSource.clear(true);
                                loadState(updatedWrapper);
                            }
                        });
                    });
                }

                $scope.isDisabled = function(p) {return !$scope.animals.hasOwnProperty(p)};
                $scope.isCompleted = function(p) {return $scope.animals.hasOwnProperty(p) && $scope.animals[p].max == $scope.animals[p].nbConfirmed};
                $scope.displayControls = true;
                $scope.displayScoreboard = true;
                $scope.radioModel = null;

                loadState(wrapper);

                $scope.highlightFeature = function(property,idx,b) {
                    var feat = $scope.species[property][idx];
                    if(b) {feat.setStyle(highlightStyle)}
                    else {feat.setStyle(speciesStyles[property])}
                }

                $scope.removeFeature = function(property,idx) {
                    vectorSource.removeFeature($scope.species[property][idx]);
                    $scope.species[property].splice(idx,1);
                }

                function panTo(coordinate) {
                    var pan = ol.animation.pan({
                        duration: 500,
                        source: /** @type {ol.Coordinate} */ (view.getCenter())
                    });
                    map.beforeRender(pan);
                    view.setCenter(coordinate);
                }

                $scope.panToFeature = function(property,idx) {panTo($scope.species[property][idx].getGeometry().getCoordinates());}
            }

            UserTrainingGame.query({login: $scope.account.login, completed: false, mode: 'TrainingAnimalIdentification'}, function(result) {
                if(result.length === 0) {
                    var game = {
                        usr: $scope.account.login,
                        game_mode: 'TrainingAnimalIdentification',
                        game_result: {
                            Snail: [],
                            Mussel: [],
                            Crab: [],
                            Burrow: []
                        },
                        completed: false
                    };
                    TrainingGame.update(game, function(f,g) {
                        Image.get({id: f.img}, function(i) {loadGame(i, f)});
                    });
                }
                else {
                    Image.get({id: result[0].img}, function(i) {loadGame(i, result[0])});
                }
            });
        });
    });
