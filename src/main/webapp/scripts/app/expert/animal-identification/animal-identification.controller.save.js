'use strict';

angular.module('ludecolApp')
    .controller('ExpertAnimalIdentificationController', function ($scope, $compile, $state, Principal, Image, UserExpertGame, ExpertGame, SubmitModalService) {
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

            var speciesCircleStyles = {
                Burrow: new ol.style.Style({fill: new ol.style.Fill({color: [255,0,255,0.5]})}),
                Crab: new ol.style.Style({fill: new ol.style.Fill({color: [255,255,0,0.5]})}),
                Mussel: new ol.style.Style({fill: new ol.style.Fill({color: [0,255,0,0.5]})}),
                Snail: new ol.style.Style({fill: new ol.style.Fill({color: [0,0,255,0.5]})})
            }

            var species = {
                Burrow: [],
                Crab: [],
                Mussel: [],
                Snail: []
            }

            var speciesCircles = {
                Burrow: [],
                Crab: [],
                Mussel: [],
                Snail: []
            }

            UserExpertGame.query({login: $scope.account.login, completed: false, mode: 'ExpertAnimalIdentification'}, function(result) {
                if(result.length === 0) {
                    var res = [];
                    var game = {
                        usr: $scope.account.login,
                        game_mode: 'ExpertAnimalIdentification',
                        game_result: {
                            Burrow: [],
                            Crab: [],
                            Mussel: [],
                            Snail: []
                        },
                        completed: false
                    };
                    ExpertGame.update(game, function(f,g) {
                        Image.get({id: f.img}, function(i) {loadGame(i, f)});
                    }, function(response) {
                        if(response.status === 400) {
                            console.log("No image are available");
                        }
                    });
                }
                else {
                    Image.get({id: result[0].img}, function(i) {loadGame(i, result[0])});
                }
            });

            function loadGame(img, game) {

                $scope.img = img;

                $scope.burrows = species['Burrow'];
                $scope.crabs = species['Crab'];
                $scope.mussels = species['Mussel'];
                $scope.snails = species['Snail'];

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
                    layers: [tileLayer, vectorLayer],
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
                map.addControl(new ol.control.Control({element: options}));

                $scope.displayControls = true;
                $scope.radioModel = null;
                $scope.displayOptions = true;
                $scope.showImage = true;
                $scope.showSnail = true;
                $scope.showMussel = true;
                $scope.showCrab = true;
                $scope.showBurrow = true;

                $scope.$watch('showImage', function(n,o) {tileLayer.setVisible(n)});

                var species_map = game.processed_result.species_map;

                for(var property in species_map) {
                    if (species_map.hasOwnProperty(property) && property !== '__proto__') {
                        var processedSpecies = species_map[property];
                        for(var i=0,ii=processedSpecies.length;i<ii;i++) {
                            var center = processedSpecies[i];
                            var circle = new ol.Feature({
                                geometry: new ol.geom.Circle([center[0],center[1]],64)
                            });
                            circle.setStyle(speciesCircleStyles[property]);
                            speciesCircles[property].push(circle);
                            vectorSource.addFeature(circle);
                        }
                    }
                }

                $scope.$watch("showSnail", function(n,o) {
                    if(n === true && o === false) {vectorSource.addFeatures(speciesCircles['Snail']);}
                    else if(o === true && n === false) {
                        for(var j=0, jj=speciesCircles['Snail'].length;j<jj;j++) {
                            vectorSource.removeFeature(speciesCircles['Snail'][j]);
                        }
                    }
                });
                $scope.$watch("showCrab", function(n,o) {
                    if(n === true && o === false) {vectorSource.addFeatures(speciesCircles['Crab']);}
                    else if(o === true && n === false) {
                        for(var j=0, jj=speciesCircles['Crab'].length;j<jj;j++) {
                            vectorSource.removeFeature(speciesCircles['Crab'][j]);
                        }
                    }
                });
                $scope.$watch("showMussel", function(n,o) {
                    if(n === true && o === false) {vectorSource.addFeatures(speciesCircles['Mussel']);}
                    else if(o === true && n === false) {
                        for(var j=0, jj=speciesCircles['Mussel'].length;j<jj;j++) {
                            vectorSource.removeFeature(speciesCircles['Mussel'][j]);
                        }
                    }
                });
                $scope.$watch("showBurrow", function(n,o) {
                    if(n === true && o === false) {vectorSource.addFeatures(speciesCircles['Burrow']);}
                    else if(o === true && n === false) {
                        for(var j=0, jj=speciesCircles['Burrow'].length;j<jj;j++) {
                            vectorSource.removeFeature(speciesCircles['Burrow'][j]);
                        }
                    }
                });

                map.on('singleclick', function(evt) {
                    if(isWithinBounds(evt.coordinate)) {
                        if($scope.radioModel !== null) {
                            var feat = new ol.Feature({geometry: new ol.geom.Point([evt.coordinate[0],evt.coordinate[1]])});
                            feat.setStyle(speciesStyles[$scope.radioModel]);
                            species[$scope.radioModel].push(feat);
                            $scope.$apply();
                            vectorSource.addFeature(feat);
                        } else {
                            console.log("radioModel is null");
                        }
                    }
                });

                $scope.submit = function() {
                    game.game_result = {
                        Burrow: toLocation(species.Burrow),
                        Crab: toLocation(species.Crab),
                        Mussel: toLocation(species.Mussel),
                        Snail: toLocation(species.Snail)
                    };
                    game.completed = true;
                    ExpertGame.update(game, function () {
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

                $scope.highlightFeature = function(property,idx,b) {
                    var feat = species[property][idx];
                    if(b) {feat.setStyle(highlightStyle)}
                    else {feat.setStyle(speciesStyles[property])}
                }

                $scope.removeFeature = function(property,idx) {
                    vectorSource.removeFeature(species[property][idx]);
                    species[property].splice(idx,1);
                }

                function panTo(coordinate) {
                    var pan = ol.animation.pan({
                        duration: 500,
                        source: /** @type {ol.Coordinate} */ (view.getCenter())
                    });
                    map.beforeRender(pan);
                    view.setCenter(coordinate);
                }

                $scope.panToFeature = function(property,idx) {panTo(species[property][idx].getGeometry().getCoordinates());}
            }
        });
    });
