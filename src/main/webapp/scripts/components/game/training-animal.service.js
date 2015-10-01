'use strict';

angular.module('ludecolApp')
    .factory('TrainingAnimalGameService', function ($rootScope, FeatureCollection, RadioModel, ScoreboardService, ImageService, GameService, UserTrainingGame, TrainingGame) {

        var _width, _height, _successCallback, _submitGame, _skipGame;
        var _vectorSource, _displayedFeatures;
        var _rectWidth, _rectHeight;
        var _cols = 3; var _rows = 3;

        var _speciesStyles = {
            Burrow: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-purple.png'}))}),
            Crab: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-yellow.png'}))}),
            Mussel: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-green.png'}))}),
            Snail: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-blue.png'}))})
        }

        var _validatedStyles = {
            Burrow: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-purple.png'}))}),
            Crab: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-yellow.png'}))}),
            Mussel: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-green.png'}))}),
            Snail: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-blue-checked.png'}))})
        }

        var _highlightStyle = new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-red.png',}))});

        function _isWithinBounds(coord) {
            return coord[0] >= 0 && coord[0] <= _width &&
             coord[1] <= 0 && coord[1] >= -_height;
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

        function _toLocation(features) {
            var res = [];
            angular.forEach(features,function(value,key){res[key] = value.getGeometry().getCoordinates();});
            return res;
        }

        function _populateFeatures(coordinates,style) {
            var result = [];
            for(var i=0, ii=coordinates.length;i<ii;i++) {
                var feat = new ol.Feature({
                    geometry: new ol.geom.Point(coordinates[i])
                });
                feat.validated = true;
                feat.setStyle(style);
                _vectorSource.addFeature(feat);
                result.push(feat);
            }
            return result;
        }


        function _initializeFeatureCollection() {
            return {
                Burrow: [],
                Crab: [],
                Mussel: [],
                Snail: []
            }
        };

        function _getResult() {
            var result = {};
            angular.forEach(FeatureCollection,function(value,key) {result[key] = _toLocation(value)});
            return result;
        };

        function _isReadyToSubmit(key) {
            var entry = ScoreboardService.data.animals[key];
            return entry.nbToSubmit + entry.nbConfirmed === entry.max;
        }

        function _setupGame(img,game) {
            _displayedFeatures = {};
            _successCallback(img,game);

            _width = img.width; _height = img.height;
            _rectWidth = _width / _cols; _rectHeight = _height / _rows;
            //Creating vector layer, to which features will be added.
            _vectorSource = new ol.source.Vector({});
            ImageService.addLayer(new ol.layer.Vector({source: _vectorSource}));
            //Setting up the grid and adding it to the vector layer.
            _vectorSource.addFeature(new ol.Feature({geometry: new ol.geom.MultiLineString(_setupLineGrid())}));
            //Adding the click listener.
            ImageService.addListener('singleclick', function(evt) {
                if(_isWithinBounds(evt.coordinate)) {
                    var radioModel = RadioModel.data.selected;
                    if(radioModel !== null) {
                        if(!_isReadyToSubmit(radioModel)) {
                            var feat = new ol.Feature({geometry: new ol.geom.Point([evt.coordinate[0],evt.coordinate[1]])});
                            feat.setStyle(_speciesStyles[radioModel]);
                            FeatureCollection[radioModel].push(feat);
                            ScoreboardService.data.animals[radioModel].nbToSubmit++;
                            $rootScope.$apply();
                            if(_displayedFeatures[radioModel]) {
                                _vectorSource.addFeature(feat);
                            }
                        }
                        else {
                                //popup saying that you cannot add more occurences of the selected species.
                        }
                    } else {

                    }
                }
            });

            _loadState(game);
        }

        function _loadState(data) {
            ScoreboardService.data.score = data.score;
            ScoreboardService.data.animals = {};
            _vectorSource.clear();
            angular.forEach(FeatureCollection,function(value,key){if (value !== undefined) while(value.length){value.pop()}});

            angular.forEach(data.partialResult, function(value,key) {
                if(data.maxSpecies[key] > 0) {
                    var features = _populateFeatures(value,_validatedStyles[key]);
                    angular.forEach(features,function(feature){FeatureCollection[key].push(feature);});
                    ScoreboardService.data.animals[key] = {
                        name: ""+key,
                        nbConfirmed: data.maxSpecies[key] - data.missingSpecies[key],
                        nbToSubmit: 0,
                        max: data.maxSpecies[key]
                    };
                }
            });
        };

        //-------------------API

        var initializeGame = function(login,successCallback,errorCallback) {
            ImageService.destroyMap();
            _successCallback = successCallback;
            var services = GameService.initializeGame(login,'AnimalIdentification',_initializeFeatureCollection,
                _getResult,_setupGame,errorCallback,UserTrainingGame.query,TrainingGame.update,TrainingGame.delete);
            _submitGame = services.submitGame;
            _skipGame = services.skipGame;
        };

        var submitGame = function(){
            _submitGame(function(cb, res) {
                TrainingGame.get({id: res.id}, function(updatedWrapper) {
                    _loadState(updatedWrapper);
                    if(updatedWrapper.completed) {
                        cb();
                    }
                });
            });
        };

        var skipGame = function(){
            _skipGame();
        };

        var highlightFeature = function(property,idx,b) {
            var feat = FeatureCollection[property][idx];
            if(b) {feat.setStyle(_highlightStyle)}
            else {feat.setStyle(_speciesStyles[property])}
        }

        var removeFeature = function(property,idx) {
            if(_displayedFeatures[property]) {
                _vectorSource.removeFeature(FeatureCollection[property][idx]);
            }
            FeatureCollection[property].splice(idx,1);
            ScoreboardService.data.animals[property].nbToSubmit--;
        }

        var toggleFeatures = function(property,show) {
            _displayedFeatures[property] = show;
            if(show) {_vectorSource.addFeatures(FeatureCollection[property]);}
            else {angular.forEach(FeatureCollection[property],function(value) {_vectorSource.removeFeature(value);});}
        }

        return {
            initializeGame: initializeGame,
            submitGame: submitGame,
            skipGame: skipGame,
            toggleFeatures: toggleFeatures,
            highlightFeature: highlightFeature,
            removeFeature: removeFeature
        }
    });
