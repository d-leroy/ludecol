'use strict';

angular.module('ludecolApp')
    .controller('ExpertPlantIdentificationController', function ($scope, $compile, $state, Principal, Image, ExpertGame, UserExpertGame, SubmitModalService) {
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
            var hintCells;

            var speciesStyles = {
                Batis: new ol.style.Style({fill: new ol.style.Fill({color: '#40B12F'})}),
                Borrichia: new ol.style.Style({fill: new ol.style.Fill({color: '#59B4C0'})}),
                Juncus: new ol.style.Style({fill: new ol.style.Fill({color: '#B958CA'})}),
                Limonium: new ol.style.Style({fill: new ol.style.Fill({color: '#6C7DDA'})}),
                Salicornia: new ol.style.Style({fill: new ol.style.Fill({color: '#CC6161'})}),
                Spartina: new ol.style.Style({fill: new ol.style.Fill({color: '#DAB11B'})})
            }

            var speciesHintFillStyles = {
                Batis: new ol.style.Fill({color: '#40B12F'}),
                Borrichia: new ol.style.Fill({color: '#59B4C0'}),
                Juncus: new ol.style.Fill({color: '#B958CA'}),
                Limonium: new ol.style.Fill({color: '#6C7DDA'}),
                Salicornia: new ol.style.Fill({color: '#CC6161'}),
                Spartina: new ol.style.Fill({color: '#DAB11B'})
            }

            var whiteFill = new ol.style.Fill({color: '#fff'});

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

            function isWithinBounds(coord) {
                return coord[0] >= 0 && coord[0] <= $scope.img.width &&
                 coord[1] <= 0 && coord[1] >= -$scope.img.height;
            }

            function setCellGeometries(cell, baseCoords) {
                var pos = [];
                var n=0;

                /*
                    We compute the position of the tags so they fill the cell in the following pattern :

                    ...
                    6 ...
                    3 5 ...
                    1 2 4 ...
                */
                for(var kmax=0, i=0; i<cell.length; kmax++) {
                    for(var k=0; k<=kmax; k++) {
                        pos[i]=[kmax-k,k];
                        i++;
                    }
                }

                /*
                    We then apply an offset computed off the position of each tag to the features they are associated to
                */
                for (var property in cell) {
                    if (cell.hasOwnProperty(property) && property !== 'length') {
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

            function setCellHintGeometries(cell, baseCoords) {

                var pos = [];
                var n=0;

                /*
                    We compute the position of the tags so they fill the cell in the following pattern :

                    ...
                    6 ...
                    3 5 ...
                    1 2 4 ...
                */
                for(var kmax=0, i=0; i<cell.length; kmax++) {
                    for(var k=0; k<=kmax; k++) {
                        pos[i]=[kmax-k,k];
                        i++;
                    }
                }

                /*
                    We then apply an offset computed off the position of each tag to the features they are associated to
                */
                for (var property in cell) {
                    if (cell.hasOwnProperty(property) && property !== 'length') {
                        var xoffset = pos[n][0] * -(tagWidth + 5);
                        var yoffset = pos[n][1] * -(tagWidth + 5);
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

            function getBaseCoordinates(i,j) {
                var x = i*rectWidth + 5;
                var y = -(j+1)*rectHeight + 5;
                return [[x,y],[x + tagWidth,y],[x,y + tagWidth]];
            }

            function getHintBaseCoordinates(i,j) {
                var x = ((i % nbCols) + 1) * rectWidth - 5;
                var y = -j * rectHeight - 5;
                return [[x,y],[x - tagWidth,y],[x,y - tagWidth]];
            }

            function generateLineGrid() {
                var lineGridCoords = [];
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
                return new ol.Feature({geometry: new ol.geom.MultiLineString(lineGridCoords)});
            }

            UserExpertGame.query({login: $scope.account.login, completed: false, mode: 'ExpertPlantIdentification'}, function(result) {
                if(result.length === 0) {
                    var game = {
                        usr: $scope.account.login,
                        game_mode: 'ExpertPlantIdentification',
                        game_result: initializePresenceGrid(),
                        completed: false
                    };
                    ExpertGame.update(game, function(f,g) {
                        Image.get({id: f.img}, function(i) {loadGame(i, f)});
                    });
                }
                else {
                    Image.get({id: result[0].img}, function(i) {loadGame(i, result[0])});
                }
            });

            //TODO: EXTERNALIZE
            function populateHintFeatures(property,presenceGrid) {
                for(var i=0, ii=presenceGrid.length;i<ii;i++) {
                    if(presenceGrid[i]) {
                        var feat = new ol.Feature({geometry: new ol.geom.Polygon([[0,0],[0,0],[0,0]])});
                        feat.setStyle(new ol.style.Style({
                            fill: speciesHintFillStyles[property],
                            text: new ol.style.Text({
                                text: Math.floor(presenceGrid[i] * 100).toString(),
                                fill: whiteFill
                            })
                        }));
                        hintCells[i][property] = feat;
                        hintCells[i].length++;
                    }
                }
            }

            function loadState(data) {
                cells = [];
                hintCells = [];

                for(var i=0; i<nbCols*nbRows; i++) {
                    cells[i] = {length: 0};
                    hintCells[i] = {length: 0};
                }

                for(var property in data.processed_result.species_map) {
                    if (data.processed_result.species_map.hasOwnProperty(property) && property !== '__proto__') {
                        if(data.processed_result.species_map[property].length > 0) {
                            populateHintFeatures(property,data.processed_result.species_map[property]);
                        }
                    }
                }

                for(var i=0; i<nbCols*nbRows; i++) {
                    var x = i % nbCols;
                    var y = Math.floor(i / nbCols);
                    var baseCoords = getHintBaseCoordinates(x,y);
                    setCellHintGeometries(hintCells[i],baseCoords);
                    for (var property in hintCells[i]) {
                        if (hintCells[i].hasOwnProperty(property) && property !== 'length' && property !== '__proto__') {
                            vectorSource.addFeature(hintCells[i][property]);
                        }
                    }
                }
            }

            function loadGame(img, game) {

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

                vectorSource.addFeature(generateLineGrid());

                map.on('singleclick', function(evt) {
                    if(isWithinBounds(evt.coordinate)) {
                        if($scope.radioModel !== null) {
                            var i = Math.floor(evt.coordinate[0] / rectWidth);
                            var j = Math.floor(evt.coordinate[1] / -rectHeight);
                            var idx = i + nbCols * j;
                            var cell = cells[idx];
                            var baseCoords = getBaseCoordinates(i,j);

                            if(cell[$scope.radioModel] !== undefined) {
                                vectorSource.removeFeature(cell[$scope.radioModel]);
                                delete cell[$scope.radioModel];
                                cell.length = cell.length - 1;
                            }
                            else {
                                var feat = new ol.Feature({geometry: new ol.geom.Polygon(baseCoords)});
                                feat.setStyle(speciesStyles[$scope.radioModel]);
                                vectorSource.addFeature(feat);
                                cell[$scope.radioModel] = feat;
                                cell.length = cell.length + 1;
                            }
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
                    game.game_result = res;
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

                loadState(game);

                $scope.displayControls = true;
                $scope.toggleControls = function() {$scope.displayControls = !$scope.displayControls}
                $scope.radioModel = null;

            }
        });
    });
