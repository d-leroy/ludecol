'use strict';

angular.module('ludecolApp')
    .factory('MapService', function () {

        var _imgWidth, _imgHeight, _imgCenter, _map, _view;
        var _listenerKeys = [];
        var _initialized = false;

        //-------------------API

        var setView = function(image) {
            if(_initialized) {
                _imgWidth = image.width;
                _imgHeight = image.height;
                _imgCenter = [_imgWidth / 2, - _imgHeight / 2];

                var tileSource = new ol.source.Zoomify({
                    url: image.path,
                    size: [_imgWidth, _imgHeight],
                    crossOrigin: 'anonymous'
                });
                var tileLayer = new ol.layer.Tile({source: tileSource});

                _map.getLayers().clear();
                _map.addLayer(tileLayer);

                _view = new ol.View({
                    projection: new ol.proj.Projection({
                        code: 'ZOOMIFY',
                        units: 'pixels',
                        extent: [0, 0, _imgWidth, _imgHeight]
                    }),
                    center: _imgCenter,
                    zoom: 2,
                    maxZoom: 4,
                    minZoom: 0,
                    extent: [0, -_imgHeight, _imgWidth, 0]
                });

                _map.setView(_view);
            }
        }

        var initializeMap = function(target) {
            angular.forEach(_listenerKeys,function(key) {_map.unByKey(key);});
            if(!_initialized) {
                _map = new ol.Map({
                    controls: ol.control.defaults().extend([
                        new ol.control.FullScreen()
                    ]),
                    target: target,
                    logo: false
                });
                _initialized = true;
            }
            return _map;
        }

        var destroyMap = function() {
            if(_initialized) {
                angular.forEach(_listenerKeys,function(key) {_map.unByKey(key);});
                _listenerKeys = [];
                _map.setTarget(null);
                _imgWidth = 0;
                _imgHeight = 0;
                _imgCenter = null;
                _map = null;
                _view = null;
                _initialized = false;
            }
        }

        var addControl = function(element) {
            if(_initialized) {
                _map.addControl(new ol.control.Control({element: element}));
            }
        }

        var addLayer = function(layer) {
            if(_initialized) {
                _map.addLayer(layer);
            }
        }

        var addListener = function(event,listener) {
            if(_initialized) {
                _listenerKeys.push(_map.on(event,listener));
            }
        }

        var panTo = function(coordinate) {
            var pan = ol.animation.pan({
                duration: 500,
                source: (_view.getCenter())
            });
            _map.beforeRender(pan);
            _view.setCenter(coordinate);
        }

        return {
            initializeMap: initializeMap,
            destroyMap: destroyMap,
            setView: setView,
            addControl: addControl,
            addLayer: addLayer,
            addListener: addListener,
//            setupGame: setupGame,
            panTo: panTo
        };
    });
