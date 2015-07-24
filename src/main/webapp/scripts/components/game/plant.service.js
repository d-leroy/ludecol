'use strict';

angular.module('ludecolApp')
    .factory('PlantGameService', function (RadioModel) {

        var _cols, _rows, _width, _height;
        var _tagWidth, _rectWidth, _rectHeight;
        var _vectorSource, _cells, _displayedFeatures;
        var _initialized = false;

        var _speciesStyles = {
            Batis: new ol.style.Style({fill: new ol.style.Fill({color: '#40B12F'})}),
            Borrichia: new ol.style.Style({fill: new ol.style.Fill({color: '#59B4C0'})}),
            Juncus: new ol.style.Style({fill: new ol.style.Fill({color: '#B958CA'})}),
            Limonium: new ol.style.Style({fill: new ol.style.Fill({color: '#6C7DDA'})}),
            Salicornia: new ol.style.Style({fill: new ol.style.Fill({color: '#CC6161'})}),
            Spartina: new ol.style.Style({fill: new ol.style.Fill({color: '#DAB11B'})})
        }

        function _isWithinBounds(coord) {
            return coord[0] >= 0 && coord[0] <= _width &&
             coord[1] <= 0 && coord[1] >= -_height;
        }

        function _getBaseCoordinates(i,j) {
            var x = i*_rectWidth + 5;
            var y = -(j+1)*_rectHeight + 5;
            return [[x,y],[x + _tagWidth,y],[x,y + _tagWidth]];
        }

        function _setupCells() {
            _cells = [];
            for(var i=0; i<_cols*_rows; i++) {
                _cells[i] = {length: 0};
            }
        }

        function _setupLineGrid() {
            var lineGridCoords = [];
            for(var i=1;i<_cols;i++) {
                var startPoint = [_rectWidth * i,0];
                var endPoint = [_rectWidth * i,-_height];
                var line = [startPoint,endPoint];
                lineGridCoords.push(line);
            }
            for(var i=1;i<_rows;i++) {
                var startPoint = [0,-_rectHeight * i];
                var endPoint = [_width,-_rectHeight * i];
                var line = [startPoint,endPoint];
                lineGridCoords.push(line);
            }
            return lineGridCoords;
        }

        function _setCellGeometries(cell, baseCoords) {
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
                    var xoffset = pos[n][0] * (_tagWidth + 5);
                    var yoffset = pos[n][1] * (_tagWidth + 5);
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

        //-------------------API

        var initializeGame = function(width,height,cols,rows,map) {
            //Setting up numerical values.
            _cols = cols; _rows = rows; _width = width; _height = height;
            _rectWidth = width / cols; _rectHeight = height / rows;
            _tagWidth = (Math.min(_rectHeight,_rectWidth) - 20) / 6;
            //Creating vector layer, to which features will be added.
            _vectorSource = new ol.source.Vector({});
            map.addLayer(new ol.layer.Vector({source: _vectorSource}));
            //Setting up the grid and adding it to the vector layer.
            _vectorSource.addFeature(new ol.Feature({geometry: new ol.geom.MultiLineString(_setupLineGrid())}));
            //Initializing the array that will contain the 'tags'.
            _setupCells();
            _displayedFeatures = {};
            //Adding the click listener.
            if(!_initialized) {
                map.on('singleclick', function(evt) {
                    console.log("click");
                    if(_isWithinBounds(evt.coordinate)) {
                        var radioModel = RadioModel.data.selected;
                        if(radioModel !== null) {
                            var i = Math.floor(evt.coordinate[0] / _rectWidth);
                            var j = Math.floor(evt.coordinate[1] / -_rectHeight);
                            var idx = i + _cols * j;
                            var cell = _cells[idx];
                            var baseCoords = _getBaseCoordinates(i,j);

                            if(cell[radioModel] !== undefined) {
                                if(_displayedFeatures[radioModel]) {
                                    _vectorSource.removeFeature(cell[radioModel]);
                                }
                                delete cell[radioModel];
                                cell.length = cell.length - 1;
                            }
                            else {
                                var feat = new ol.Feature({geometry: new ol.geom.Polygon(baseCoords)});
                                feat.setStyle(_speciesStyles[radioModel]);
                                //Should check if those features are currently displayed.
                                if(_displayedFeatures[radioModel]) {
                                    _vectorSource.addFeature(feat);
                                }
                                cell[radioModel] = feat;
                                cell.length = cell.length + 1;
                            }
                            _setCellGeometries(cell, baseCoords);
                        } else {
                            //popup saying that you should select a species first.
                        }
                    }
                });
                _initialized = true;
            }
        }

        var toggleFeatures = function(property,show) {
            _displayedFeatures[property] = show;
            var features = [];
            angular.forEach(_cells,function(value){if(value[property] !== undefined) {features.push(value[property]);}});
            if(show) {_vectorSource.addFeatures(features);}
            else {angular.forEach(features,function(value) {_vectorSource.removeFeature(value);});}
        }

        var getResult = function(empty_result) {
            for(var i=0; i<_cols*_rows; i++) {
                var tmp = _cells[i];
                for (var property in tmp) {
                    if (tmp.hasOwnProperty(property) && property !== 'length') {
                        empty_result[property][i] = true;
                    }
                }
            }
            return empty_result;
        }

        return {initializeGame: initializeGame, getResult: getResult, toggleFeatures: toggleFeatures};
    });
