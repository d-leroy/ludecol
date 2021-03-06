'use strict';

angular.module('ludecolApp')
    .factory('ExpertPlantGameService', function (RadioModel, ImageService, GameService, UserExpertGame, ExpertGame) {

        var _cols, _rows, _width, _height, _speciesMap;
        var _tagWidth, _rectWidth, _rectHeight;
        var _vectorSource, _tags, _hintTags, _displayedFeatures;
        var _successCallback, _submitGame;

        var _speciesStyles = {
            Batis: new ol.style.Style({fill: new ol.style.Fill({color: '#40B12F'})}),
            Borrichia: new ol.style.Style({fill: new ol.style.Fill({color: '#59B4C0'})}),
            Juncus: new ol.style.Style({fill: new ol.style.Fill({color: '#B958CA'})}),
            Limonium: new ol.style.Style({fill: new ol.style.Fill({color: '#6C7DDA'})}),
            Salicornia: new ol.style.Style({fill: new ol.style.Fill({color: '#CC6161'})}),
            Spartina: new ol.style.Style({fill: new ol.style.Fill({color: '#DAB11B'})})
        }

        var _speciesHintStrokes = {
            Batis: new ol.style.Stroke({color: '#40B12F', width: 5}),
            Borrichia: new ol.style.Stroke({color: '#59B4C0', width: 5}),
            Juncus: new ol.style.Stroke({color: '#B958CA', width: 5}),
            Limonium: new ol.style.Stroke({color: '#6C7DDA', width: 5}),
            Salicornia: new ol.style.Stroke({color: '#CC6161', width: 5}),
            Spartina: new ol.style.Stroke({color: '#DAB11B', width: 5})
        }

        function _getHintStyle(species,presence) {
            var text = new ol.style.Text({
                text: Math.floor(presence * 100).toString(),
                fill: _whiteFill
            });
            switch(species) {
                case 'Batis': return new ol.style.Style({text: text, stroke: _speciesHintStrokes.Batis});
                case 'Borrichia': return new ol.style.Style({text: text, stroke: _speciesHintStrokes.Borrichia});
                case 'Juncus': return new ol.style.Style({text: text, stroke: _speciesHintStrokes.Juncus});
                case 'Limonium': return new ol.style.Style({text: text, stroke: _speciesHintStrokes.Limonium});
                case 'Salicornia': return new ol.style.Style({text: text, stroke: _speciesHintStrokes.Salicornia});
                case 'Spartina': return new ol.style.Style({text: text, stroke: _speciesHintStrokes.Spartina});
            }
        }

        var _speciesHintStyles = {
            Batis: new ol.style.Style({stroke: new ol.style.Stroke({color: '#40B12F', width: 5})}),
            Borrichia: new ol.style.Style({stroke: new ol.style.Stroke({color: '#59B4C0', width: 5})}),
            Juncus: new ol.style.Style({stroke: new ol.style.Stroke({color: '#B958CA', width: 5})}),
            Limonium: new ol.style.Style({stroke: new ol.style.Stroke({color: '#6C7DDA', width: 5})}),
            Salicornia: new ol.style.Style({stroke: new ol.style.Stroke({color: '#CC6161', width: 5})}),
            Spartina: new ol.style.Style({stroke: new ol.style.Stroke({color: '#DAB11B', width: 5})})
        }

        var _whiteFill = new ol.style.Fill({color: '#fff'});

        function _isWithinBounds(coord) {
            return coord[0] >= 0 && coord[0] <= _width &&
             coord[1] <= 0 && coord[1] >= -_height;
        }

        function _getBaseCoordinates(i,j) {
            var x = i*_rectWidth + 5;
            var y = -(j+1)*_rectHeight + 5;
            return [[x,y],[x + _tagWidth,y],[x,y + _tagWidth]];
        }

        function _setupTags() {
            _tags = [];
            for(var i=0; i<_cols*_rows; i++) {
                _tags[i] = {};
            }
        }

        function _setupHintTags(species_map) {
            _speciesMap = species_map;
            _hintTags = [];
            angular.forEach(species_map,function(species,property) {
                angular.forEach(species,function(presence,index) {
                    if(presence > 0) {
                        var i = index % _cols;
                        var j = Math.floor(index / _cols);
                        var feat = new ol.Feature({geometry: new ol.geom.Polygon(_getBaseCoordinates(i,j))});
                        var style = _getHintStyle(property,presence);
                        feat.setStyle(style);
                        feat.hintStyle = style;

                        if(_hintTags[index] === undefined) {_hintTags[index] = {};}
                        _hintTags[index][property] = feat;
                    }
                });
            })

            for(var idx = 0; idx < _rows * _cols; idx++) {
                var i = idx % _cols;
                var j = Math.floor(idx / _cols);
                _setCellGeometries(idx,_getBaseCoordinates(i,j));
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

        function _setCellGeometries(idx, baseCoords) {
            var pos = []; var k = 0; var kmax = 0; var i = 0; var n = 0;

            function setPositions(collection) {
                angular.forEach(collection[idx],function() {
                    pos[i]=[kmax-k,k];
                    i++; k++;
                    if(k > kmax) {
                        k = 0; kmax++;
                    }
                });
            }

            function setFeatures(collection) {
                angular.forEach(collection[idx],function(feature) {
                    var xoffset = pos[n][0] * (_tagWidth + 5);
                    var yoffset = pos[n][1] * (_tagWidth + 5);
                    var coords =
                    [[
                        [baseCoords[0][0] + xoffset, baseCoords[0][1] + yoffset],
                        [baseCoords[1][0] + xoffset, baseCoords[1][1] + yoffset],
                        [baseCoords[2][0] + xoffset, baseCoords[2][1] + yoffset]
                    ]];
                    feature.getGeometry().setCoordinates(coords);
                    n++;
                });
            }
            if(_hintTags !== undefined) {
                setPositions(_hintTags);
                setFeatures(_hintTags);
            }
            setPositions(_tags);
            setFeatures(_tags);
        }

        function _initializePresenceGrid() {
            var res = {
                Batis: [], Borrichia: [], Juncus: [],
                Limonium: [], Salicornia: [], Spartina: []
            };
            for(var i=0; i<_cols*_rows; i++) {
                for (var property in res) {
                    if (res.hasOwnProperty(property)) {
                        res[property].push(false);
                    }
                }
            }
            return res;
        }

        function _getResult() {
            var result = _initializePresenceGrid();
            for(var i=0; i<_cols*_rows; i++) {
                var tmp = _tags[i];
                angular.forEach(tmp,function(value,key) {result[key][i] = true;});
                if(_hintTags !== undefined) {
                    tmp = _hintTags[i];
                    angular.forEach(tmp,function(value,key) {result[key][i] = true;});
                }
            }
            return result;
        }

        var _setupGame = function(img,game) {
            _displayedFeatures = {};
            _successCallback(img,game);

            //Setting up numerical values.
            _width = img.width; _height = img.height;
            _rectWidth = _width / _cols; _rectHeight = _height / _rows;
            _tagWidth = (Math.min(_rectHeight,_rectWidth) - 20) / 6;
            //Creating vector layer, to which features will be added.
            _vectorSource = new ol.source.Vector({});
            ImageService.addLayer(new ol.layer.Vector({source: _vectorSource}));
            //Setting up the grid and adding it to the vector layer.
            _vectorSource.addFeature(new ol.Feature({geometry: new ol.geom.MultiLineString(_setupLineGrid())}));
            //Initializing the array that will contain the 'tags'.
            _setupTags();
            if(game.reference_result !== null) {
                _setupHintTags(game.reference_result.species_map);
            }
            //Adding the click listener.
            ImageService.addListener('singleclick', function(evt) {
                if(_isWithinBounds(evt.coordinate)) {
                    var radioModel = RadioModel.data.selected;
                    if(radioModel !== null) {
                        var i = Math.floor(evt.coordinate[0] / _rectWidth);
                        var j = Math.floor(evt.coordinate[1] / -_rectHeight);
                        var idx = i + _cols * j;
                        var tags = _tags[idx];
                        var baseCoords = _getBaseCoordinates(i,j);

                        if(_hintTags !== undefined) {
                            var hintTags = _hintTags[idx];
                        }
                        var tags = _tags[idx];

                        if(hintTags !== undefined && hintTags[radioModel] !== undefined) {
                            if(hintTags[radioModel].getStyle() === hintTags[radioModel].hintStyle) {
                                hintTags[radioModel].setStyle(_speciesStyles[radioModel]);
                            }
                            else {
                                hintTags[radioModel].setStyle(hintTags[radioModel].hintStyle);
                            }
                        }
                        else {
                            if(tags[radioModel] !== undefined) {
                                if(_displayedFeatures[radioModel]) {
                                    _vectorSource.removeFeature(tags[radioModel]);
                                }
                                delete tags[radioModel];
                            }
                            else {
                                var feat = new ol.Feature({geometry: new ol.geom.Polygon(baseCoords)});
                                feat.setStyle(_speciesStyles[radioModel]);
                                //Should check if those features are currently displayed.
                                if(_displayedFeatures[radioModel]) {
                                    _vectorSource.addFeature(feat);
                                }
                                tags[radioModel] = feat;
                            }
                        }
                        _setCellGeometries(idx, baseCoords);
                    } else {
                        //popup saying that you should select a species first.
                    }
                }
            });
        }

        //-------------------API

        var initializeGame = function(login,cols,rows,successCallback,errorCallback) {
            ImageService.destroyMap();
            _successCallback = successCallback;
            _cols = cols; _rows = rows;
            var services = GameService.initializeGame(login,'PlantIdentification',_initializePresenceGrid,
                _getResult,_setupGame,errorCallback,UserExpertGame.query,ExpertGame.update);
            _submitGame = services.submitGame;
        };

        var initializeReferenceDefinition = function(login,cols,rows,successCallback,errorCallback,img,submitCallback) {
            ImageService.destroyMap();
            _successCallback = successCallback;
            _cols = cols; _rows = rows;
            _submitGame = GameService.initializeReferenceDefinition(login,'PlantIdentification',_initializePresenceGrid,
                _getResult,_setupGame,errorCallback,ExpertGame.update,img,submitCallback);
        }

        var submitGame = function(){
            _submitGame();
        };

        var toggleFeatures = function(property,show) {
            _displayedFeatures[property] = show;
            var features = [];
            angular.forEach(_tags,function(value){if(value[property] !== undefined) {features.push(value[property]);}});
            angular.forEach(_hintTags,function(value){if(value[property] !== undefined) {features.push(value[property]);}});
            if(show) {_vectorSource.addFeatures(features);}
            else {angular.forEach(features,function(value) {_vectorSource.removeFeature(value);});}
        }

        return {initializeGame: initializeGame, initializeReferenceDefinition: initializeReferenceDefinition,
            toggleFeatures: toggleFeatures, submitGame: submitGame};
    });
