'use strict';

angular.module('ludecolApp')
    .factory('AnimalGameService', function ($rootScope, FeatureCollection, RadioModel) {

        var _width, _height;
        var _vectorSource, _displayedFeatures;
        var _initialized = false;

        var _speciesStyles = {
            Burrow: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-purple.png'}))}),
            Crab: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-yellow.png'}))}),
            Mussel: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-green.png'}))}),
            Snail: new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-blue.png'}))})
        }

        var _highlightStyle = new ol.style.Style({image: new ol.style.Icon(({anchor: [0.5, 1],src: 'images/icon-red.png',}))});

        function _isWithinBounds(coord) {
            return coord[0] >= 0 && coord[0] <= _width &&
             coord[1] <= 0 && coord[1] >= -_height;
        }

        function _toLocation(features) {
            var res = [];
            angular.forEach(features,function(value,key){res[key] = value.getGeometry().getCoordinates();});
            return res;
        }

        //-------------------API

        var getResult = function(result) {
            angular.forEach(FeatureCollection,function(value,key) {result[key] = _toLocation(value)})
            return result;
        }

        var initializeGame = function(width,height,map) {
            //Setting up numerical values.
            _width = width; _height = height;
            //Creating vector layer, to which features will be added.
            _vectorSource = new ol.source.Vector({});
            map.addLayer(new ol.layer.Vector({source: _vectorSource}));
            _displayedFeatures = {};
            //Adding the click listener.
            map.on('singleclick', function(evt) {
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
                        console.log("radioModel is null");
                    }
                }
            });
        }

        var highlightFeature = function(property,idx,b) {
            var feat = FeatureCollection[property][idx];
            if(b) {feat.setStyle(_highlightStyle)}
            else {feat.setStyle(_speciesStyles[property])}
        }

        var removeFeature = function(property,idx) {
            if(_displayedFeatures[radioModel]) {
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
            toggleFeatures: toggleFeatures,
            highlightFeature: highlightFeature,
            removeFeature: removeFeature,
            getResult: getResult
        }
    });
