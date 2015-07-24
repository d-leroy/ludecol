'use strict';

angular.module('ludecolApp')
    .controller('GamesController', function ($scope, $stateParams, Principal, Auth, Game, Image) {
        Principal.identity().then(function(account) {

            $scope.settingAccount = account;

            var imgWidth;
            var imgHeight;
            var imgCenter;

            var vectorSource = new ol.source.Vector();
            var vectorLayer = new ol.layer.Vector({source: vectorSource});

            Game.get({id: $stateParams.id}, function(res) {
                Image.get({id: res.img}, function(i) {loadGame(i,res)});
            })

            function setupPlantIdentification(submitted_map,reference_map) {

                var plantSpeciesStyles = {
                    Batis: new ol.style.Style({fill: new ol.style.Fill({color: '#40B12F'})}),
                    Borrichia: new ol.style.Style({fill: new ol.style.Fill({color: '#59B4C0'})}),
                    Juncus: new ol.style.Style({fill: new ol.style.Fill({color: '#B958CA'})}),
                    Limonium: new ol.style.Style({fill: new ol.style.Fill({color: '#6C7DDA'})}),
                    Salicornia: new ol.style.Style({fill: new ol.style.Fill({color: '#CC6161'})}),
                    Spartina: new ol.style.Style({fill: new ol.style.Fill({color: '#DAB11B'})})
                }

                var nbCols = 6;
                var nbRows = 4;

                var rectWidth = imgWidth / nbCols;
                var rectHeight = imgHeight / nbRows;
                var tagWidth = (Math.min(rectHeight,rectWidth) - 20) / 6;

                var submitted_cells = [];
                var reference_cells = [];

                for(var i=0; i<nbCols*nbRows; i++) {
                    submitted_cells[i] = {length: 0};
                    reference_cells[i] = {length: 0};
                }

                function setCellGeometries(cell,baseCoords,reference) {
                    var pos = [];
                    var n=0;

                    for(var kmax=0, i=0; i<cell.length; kmax++) {
                        for(var k=0; k<=kmax; k++) {
                            pos[i]=[kmax-k,k];
                            i++;
                        }
                    }

                    for (var property in cell) {
                        if (cell.hasOwnProperty(property) && property !== 'length') {
                            var xoffset = pos[n][0] * (reference ? -1 : 1) * (tagWidth + 5);
                            var yoffset = pos[n][1] * (reference ? -1 : 1) * (tagWidth + 5);
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

                function getSubmittedBaseCoordinates(i,j) {
                    var x = i*rectWidth + 5;
                    var y = -(j+1)*rectHeight + 5;
                    return [[x,y],[x + tagWidth,y],[x,y + tagWidth]];
                }

                function getReferenceBaseCoordinates(i,j) {
                    var x = (i+1) * rectWidth - 5;
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

                function populateFeatures(property,presenceGrid,features) {
                    for(var i=0, ii=presenceGrid.length;i<ii;i++) {
                        if(presenceGrid[i]) {
                            var feat = new ol.Feature({geometry: new ol.geom.Polygon([[0,0],[0,0],[0,0]])});
                            feat.setStyle(plantSpeciesStyles[property]);
                            features[i][property] = feat;
                            features[i].length++;
                        }
                    }
                }

                for(var property in submitted_map) {
                    if (submitted_map.hasOwnProperty(property) && property !== '__proto__') {
                        if(submitted_map[property].length > 0) {
                            populateFeatures(property,submitted_map[property],submitted_cells);
                        }
                    }
                }

                for(var property in reference_map) {
                    if (reference_map.hasOwnProperty(property) && property !== '__proto__') {
                        if(reference_map[property].length > 0) {
                            populateFeatures(property,reference_map[property],reference_cells);
                        }
                    }
                }

                for(var i=0; i<nbCols*nbRows; i++) {
                    var x = i % nbCols;
                    var y = Math.floor(i / nbCols);
                    setCellGeometries(submitted_cells[i],getSubmittedBaseCoordinates(x,y),false);
                    setCellGeometries(reference_cells[i],getReferenceBaseCoordinates(x,y),true);
                    for (var property in submitted_cells[i]) {
                        if (submitted_cells[i].hasOwnProperty(property) && property !== 'length' && property !== '__proto__') {
                            vectorSource.addFeature(submitted_cells[i][property]);
                        }
                    }
                    for (var property in reference_cells[i]) {
                        if (reference_cells[i].hasOwnProperty(property) && property !== 'length' && property !== '__proto__') {
                            vectorSource.addFeature(reference_cells[i][property]);
                        }
                    }
                }

                var lineGrid = generateLineGrid();
                vectorSource.addFeature(lineGrid);
            }

            function setupAnimalIdentification(submitted_map,reference_map) {

                var animalSpeciesStyles = {
                    Burrow: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-purple.png'}))}),
                    Crab: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-yellow.png'}))}),
                    Mussel: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-green.png'}))}),
                    Snail: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-blue.png'}))})
                }

                var animalSpeciesCircleStyles = {
                    Burrow: new ol.style.Style({fill: new ol.style.Fill({color: [255,0,255,0.5]})}),
                    Crab: new ol.style.Style({fill: new ol.style.Fill({color: [255,255,0,0.5]})}),
                    Mussel: new ol.style.Style({fill: new ol.style.Fill({color: [0,255,0,0.5]})}),
                    Snail: new ol.style.Style({fill: new ol.style.Fill({color: [0,0,255,0.5]})})
                }

                for(var property in submitted_map) {
                    if (submitted_map.hasOwnProperty(property) && property !== '__proto__') {
                        var submitted_list = submitted_map[property];
                        for(var i=0,ii=submitted_list.length;i<ii;i++) {
                            var center = submitted_list[i];
                            var circle = new ol.Feature({geometry: new ol.geom.Circle([center[0],center[1]],64)});
                            circle.setStyle(animalSpeciesCircleStyles[property]);
                            vectorSource.addFeature(circle);
                        }
                    }
                }

                for(var property in reference_map) {
                    if (reference_map.hasOwnProperty(property) && property !== '__proto__') {
                        var reference_list = reference_map[property];
                        for(var i=0,ii=reference_list.length;i<ii;i++) {
                            var center = reference_list[i];
                            var feature = new ol.Feature({geometry: new ol.geom.Point([center[0],center[1]])});
                            feature.setStyle(animalSpeciesStyles[property]);
                            vectorSource.addFeature(feature);
                        }
                    }
                }
            }

            function loadGame(img,game) {

                var submitted_map = game.game_result.species_map;
                var reference_map = game.corrected_game_result.species_map;

                imgWidth = img.width;
                imgHeight = img.height;
                imgCenter = [imgWidth / 2, - imgHeight / 2];

                switch(game.game_mode) {
                    case 'AllStarsIdentification':

                        break;
                    case 'PlantIdentification':
                        setupPlantIdentification(submitted_map,reference_map);
                        break;
                    case 'AnimalIdentification':
                        setupAnimalIdentification(submitted_map,reference_map);
                        break;

                }

                var tileSource = new ol.source.Zoomify({
                    url: img.path,
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
                })

                var map = new ol.Map({
                    controls: ol.control.defaults().extend([
                        new ol.control.FullScreen()
                    ]),
                    layers: [tileLayer, vectorLayer],
                    target: 'map',
                    view: view,
                    logo: false
                });
            }
        });
    });
