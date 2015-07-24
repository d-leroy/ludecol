'use strict';

angular.module('ludecolApp')
    .controller('TrainingPlantsController', function ($scope, $compile, $state, User, Principal, Image, UserTrainingGame, TrainingGame, TrainingSubmitModalService) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;

            var imgWidth;
            var imgHeight;
            var imgCenter;

            var rectWidth;
            var rectHeight;

            var tagWidth;

            var nbRows = 3;
            var nbCols = 3;

            var vectorSource = new ol.source.Vector({});
            var vectorLayer = new ol.layer.Vector({source: vectorSource});
            var map;

            var cells;

            var speciesStyles = {
                Batis: new ol.style.Style({fill: new ol.style.Fill({color: '#40B12F'})}),
                Borrichia: new ol.style.Style({fill: new ol.style.Fill({color: '#59B4C0'})}),
                Juncus: new ol.style.Style({fill: new ol.style.Fill({color: '#B958CA'})}),
                Limonium: new ol.style.Style({fill: new ol.style.Fill({color: '#6C7DDA'})}),
                Salicornia: new ol.style.Style({fill: new ol.style.Fill({color: '#CC6161'})}),
                Spartina: new ol.style.Style({fill: new ol.style.Fill({color: '#DAB11B'})})
            }

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

            UserTrainingGame.query({login: $scope.account.login, completed: false, mode: 'TrainingPlantIdentification'}, function(result) {
                if(result.length === 0) {
                    var game = {
                        usr: $scope.account.login,
                        game_mode: 'TrainingPlantIdentification',
                        game_result: initializePresenceGrid(),
                        completed: false
                    };
                    TrainingGame.update(game, function(f,g) {Image.get({id: f.img}, function(i) {loadGame(i, f)});});
                }
                else {Image.get({id: result[0].img}, function(i) {loadGame(i, result[0])});}
            });

            function isWithinBounds(coord) {
                return coord[0] >= 0 && coord[0] <= $scope.img.width &&
                    coord[1] <= 0 && coord[1] >= -$scope.img.height;
            }

            //TODO: EXTERNALIZE
            function populateFeatures(property,presenceGrid,style) {
                for(var i=0, ii=presenceGrid.length;i<ii;i++) {
                    if(presenceGrid[i]) {
                        var feat = new ol.Feature({geometry: new ol.geom.Polygon([[0,0],[0,0],[0,0]])});
                        feat.setStyle(style);
                        feat.validated = true;
                        cells[i][property] = feat;
                        cells[i].length++;
                    }
                }
            }

            function isReadyToSubmit(property) {
                return !($scope.plants[property].nbToSubmit + $scope.plants[property].nbConfirmed < $scope.plants[property].max);
            }

            function getBaseCoordinates(i,j) {
                var x = i*rectWidth + 5;
                var y = -(j+1)*rectHeight + 5;
                var baseCoords = [[x,y],[x + tagWidth,y],[x,y + tagWidth]];
                return baseCoords;
            }

            function setCellGeometries(cell, baseCoords) {
                var pos = [];
                var n=0;

                for(var kmax=0, i=0; i<cell.length; kmax++) {
                    for(var k=0; k<=kmax; k++) {
                        pos[i]=[kmax-k,k];
                        i++;
                    }
                }

                for (var property in cell) {
                    if (cell.hasOwnProperty(property) && property !== 'length' && property !== '__proto__') {
                        var xoffset = pos[n][0] * (tagWidth + 5);
                        var yoffset = pos[n][1] * (tagWidth + 5);
                        var coords =
                        [[
                            [baseCoords[0][0] + xoffset, baseCoords[0][1] + yoffset],
                            [baseCoords[1][0] + xoffset, baseCoords[1][1] + yoffset],
                            [baseCoords[2][0] + xoffset, baseCoords[2][1] + yoffset]
                        ]];
                        cell[property].getGeometry().setCoordinates(coords);
                        n++;
                    }
                }
            }

            //TODO: EXTERNALIZE
            function loadState(data) {
                $scope.radioModel = null;
                $scope.score = data.score;
                $scope.plants = {};
                cells = [];
                for(var i=0; i<nbCols*nbRows; i++) {
                    cells[i] = {length: 0};
                }

                for(var property in data.partialResult) {
                    if (data.partialResult.hasOwnProperty(property) && property !== '__proto__') {
                        if(data.maxSpecies[property] > 0) {
                            populateFeatures(property,data.partialResult[property],speciesStyles[property]);
                            $scope.plants[property] = {
                                name: ""+property,
                                nbConfirmed: data.maxSpecies[property] - data.missingSpecies[property],
                                nbToSubmit: 0,
                                max: data.maxSpecies[property],
                            };
                        }
                    }
                }

                for(var i=0; i<nbCols*nbRows; i++) {
                    var x = i % nbCols;
                    var y = Math.floor(i / nbCols);
                    var baseCoords = getBaseCoordinates(x,y);
                    setCellGeometries(cells[i],baseCoords);
                    for (var property in cells[i]) {
                        if (cells[i].hasOwnProperty(property) && property !== 'length' && property !== '__proto__') {
                            vectorSource.addFeature(cells[i][property]);
                        }
                    }
                }

                var lineGridCoords = []
                for(var i=1;i<nbCols;i++) {
                    var startPoint = [rectWidth * i,0];
                    var endPoint = [rectWidth * i,-imgHeight];
                    var line = [startPoint,endPoint];
                    lineGridCoords.push(line);
                }
                for(var i=1;i<nbRows;i++) {
                    var startPoint = [0,-rectHeight * i];
                    var endPoint = [imgWidth,-rectHeight * i];
                    var line = [startPoint,endPoint];
                    lineGridCoords.push(line);
                }
                var lineGridFeature = new ol.Feature({geometry: new ol.geom.MultiLineString(lineGridCoords)});
                vectorSource.addFeature(lineGridFeature);
            }

            function loadGame(img, wrapper) {

                $scope.img = img;

                imgWidth = $scope.img.width;
                imgHeight = $scope.img.height;
                imgCenter = [imgWidth / 2, - imgHeight / 2];
                rectWidth = imgWidth / nbCols;
                rectHeight = imgHeight / nbRows;
                tagWidth = (Math.min(rectHeight,rectWidth) - 20) / 6;

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

                //TODO EXTERNALIZE
                map.addControl(new ol.control.Control({element: controls}));
                map.addControl(new ol.control.Control({element: scoreboard}));

                map.on('singleclick', function(evt) {
                    if(isWithinBounds(evt.coordinate)) {
                        if($scope.radioModel !== null) {
                            var i = Math.floor(evt.coordinate[0] / rectWidth);
                            var j = Math.floor(evt.coordinate[1] / -rectHeight);
                            var idx = i + nbCols * j;
                            var cell = cells[idx];
                            var baseCoords = getBaseCoordinates(i,j);

                            if(cell[$scope.radioModel] !== undefined) {
                                if(!cell[$scope.radioModel].validated) {
                                    vectorSource.removeFeature(cell[$scope.radioModel]);
                                    delete cell[$scope.radioModel];
                                    cell.length = cell.length - 1;
                                    $scope.plants[$scope.radioModel].nbToSubmit--;
                                }
                            }
                            else {
                                if(!isReadyToSubmit($scope.radioModel)) {
                                    var feat = new ol.Feature({geometry: new ol.geom.Polygon(baseCoords)});
                                    feat.setStyle(speciesStyles[$scope.radioModel]);
                                    feat.validated = false;
                                    vectorSource.addFeature(feat);
                                    cell[$scope.radioModel] = feat;
                                    cell.length = cell.length + 1;
                                    $scope.plants[$scope.radioModel].nbToSubmit++;
                                }
                                else {
                                    //popup saying that you cannot add more occurences of the selected species.
                                }
                            }
                            $scope.$apply();
                            setCellGeometries(cell, baseCoords);
                        } else {
                            //popup saying that you should select a species first.
                        }
                    }
                });

                $scope.submit = function() {
                    var res = initializePresenceGrid();
                    for(var i=0; i<nbCols*nbRows; i++) {
                        var tmp = cells[i];
                        for (var property in tmp) {
                            if (tmp.hasOwnProperty(property) && property !== 'length') {
                                res[property][i] = true;
                            }
                        }
                    }
                    var game = {
                        usr: $scope.account.login,
                        img: wrapper.img,
                        id: wrapper.id,
                        game_mode: 'TrainingPlantIdentification',
                        game_result: res
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

                loadState(wrapper);

                //Joker reveals missing occurrences before skipping to the next picture.
                //The player gets a score of 50 * percentage of found occurrences.
                $scope.joker = function(){console.log("Joker!")};

                $scope.isDisabled = function(p) {return !$scope.plants.hasOwnProperty(p)};
                $scope.isCompleted = function(p) {return $scope.plants.hasOwnProperty(p) && $scope.plants[p].max == $scope.plants[p].nbConfirmed};
                $scope.displayControls = true;
                $scope.toggleControls = function() {$scope.displayControls = !$scope.displayControls}
                $scope.displayScoreboard = true;
                $scope.toggleScoreboard = function() {$scope.displayScoreboard = !$scope.displayScoreboard}
                $scope.radioModel = null;

            }
        });
    });
