'use strict';

angular.module('ludecolApp')
    .factory('AnimalGameService', function ($rootScope, FeatureCollection, RadioModel, ImageService, GameService, UserGame, Game) {

        var _width, _height, _successCallback, _submitGame;
        var _vectorSource, _displayedFeatures;
        var _rectWidth, _rectHeight;
        var _cols = 3; var _rows = 3;

        var _speciesStyles = {
            Burrow: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-purple.png'}))}),
            Crab: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-yellow.png'}))}),
            Mussel: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-green.png'}))}),
            Snail: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-blue.png'}))})
        };

        var _highlightStyle = new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-red.png',}))});

        function _isWithinBounds(coord) {
            return coord[0] >= 0 && coord[0] <= _width &&
             coord[1] <= 0 && coord[1] >= -_height;
        };

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
        };

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
                        var feat = new ol.Feature({geometry: new ol.geom.Point([evt.coordinate[0],evt.coordinate[1]])});
                        feat.setStyle(_speciesStyles[radioModel]);
                        FeatureCollection[radioModel].push(feat);
                        $rootScope.$apply();
                        if(_displayedFeatures[radioModel]) {
                            _vectorSource.addFeature(feat);
                        }
                    } else {

                    }
                }
            });
        }

        //-------------------API

        var initializeGame = function(login,successCallback,errorCallback) {
            ImageService.destroyMap();
            _successCallback = successCallback;
            _submitGame = GameService.initializeGame(login,'AnimalIdentification',_initializeFeatureCollection,
                _getResult,_setupGame,errorCallback,UserGame.query,Game.update);
        };

        var submitGame = function(){
            _submitGame();
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
        }

        var toggleFeatures = function(property,show) {
            _displayedFeatures[property] = show;
            if(show) {_vectorSource.addFeatures(FeatureCollection[property]);}
            else {angular.forEach(FeatureCollection[property],function(value) {_vectorSource.removeFeature(value);});}
        }

        return {
            initializeGame: initializeGame,
            submitGame: submitGame,
            toggleFeatures: toggleFeatures,
            highlightFeature: highlightFeature,
            removeFeature: removeFeature
        }
    });
