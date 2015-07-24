'use strict';

angular.module('ludecolApp')
    .factory('MapService', function (PlantGameService, AnimalGameService, ExpertAnimalGameService, GameService) {

        var _imgWidth, _imgHeight, _imgCenter, _map, _view;
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
                })

                _map.setView(_view);
            }
        }

        var initializeMap = function(target,force) {
            if(!_initialized || force) {
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

        var setupGame = function(options) {
            if(_initialized) {
                switch(GameService.getMode()) {
                    case 'AnimalIdentification': AnimalGameService.initializeGame(_imgWidth,_imgHeight,_map); break;
                    case 'PlantIdentification': PlantGameService.initializeGame(_imgWidth,_imgHeight,options.cols,options.rows,_map); break;
                    case 'ExpertAnimalIdentification': ExpertAnimalGameService.initializeGame(_imgWidth,_imgHeight,_map,options.species_map); break;
                    default: break;
                }

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
            setupGame: setupGame,
            panTo: panTo
        };
    });
