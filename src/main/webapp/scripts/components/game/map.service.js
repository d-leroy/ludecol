'use strict';

angular.module('ludecolApp')
    .factory('ImageService', function () {

        var _imgWidth, _imgHeight, _imgCenter, _image, _view;
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

                _image.getLayers().clear();
                _image.addLayer(tileLayer);

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

                _image.setView(_view);
            }
        }

        var initializeMap = function(target) {
            angular.forEach(_listenerKeys,function(key) {_image.unByKey(key);});
            if(!_initialized) {
                _image = new ol.Map({
                    controls: ol.control.defaults().extend([
                        new ol.control.FullScreen()
                    ]),
                    target: target,
                    logo: false
                });
                _initialized = true;
            }
            return _image;
        }

        var destroyMap = function() {
            if(_initialized) {
                angular.forEach(_listenerKeys,function(key) {_image.unByKey(key);});
                _listenerKeys = [];
                _image.setTarget(null);
                _imgWidth = 0;
                _imgHeight = 0;
                _imgCenter = null;
                _image = null;
                _view = null;
                _initialized = false;
            }
        }

        var addControl = function(element) {
            if(_initialized) {
                _image.addControl(new ol.control.Control({element: element}));
            }
        }

        var addLayer = function(layer) {
            if(_initialized) {
                _image.addLayer(layer);
            }
        }

        var addListener = function(event,listener) {
            if(_initialized) {
                _listenerKeys.push(_image.on(event,listener));
            }
        }

        var panTo = function(coordinate) {
            var pan = ol.animation.pan({
                duration: 500,
                source: (_view.getCenter())
            });
            _image.beforeRender(pan);
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
