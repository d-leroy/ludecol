'use strict';

angular.module('ludecolApp')
    .controller('OptionsController', function ($scope, User, Principal, Image, Feature, ImageFeatures, ImageReferenceFeatures) {


        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;

            var wrt = new ol.format.WKT();

            var imgWidth;
            var imgHeight;
            var url;

            var map;

            var maxN = 0;

            var n = 0;

            var imgs = [];

            $scope.first = function() {return n<=0}
            $scope.last = function() {return n>=maxN-1}

            $scope.previous = function() {n--; $scope.img = imgs[n]}
            $scope.next = function() {n++; $scope.img = imgs[n]}

            $scope.features = [];

            Image.query(function(res) {

                imgs = res;
                maxN = imgs.length;

                $scope.img = imgs[n];

                $scope.$watch('img', function() {

                    imgWidth = $scope.img.width;
                    imgHeight = $scope.img.height;
                    url = $scope.img.path;

                    var crossOrigin = 'anonymous';

                    var imgCenter = [imgWidth / 2, - imgHeight / 2];

                    var proj = new ol.proj.Projection({
                      code: 'ZOOMIFY',
                      units: 'pixels',
                      extent: [0, 0, imgWidth, imgHeight]
                    });

                    var tileSource = new ol.source.Zoomify({
                      url: url,
                      size: [imgWidth, imgHeight],
                      crossOrigin: crossOrigin
                    });

                    var tile = new ol.layer.Tile({source: tileSource});

                    map.setLayerGroup(new ol.layer.Group({layers: [tile]}));
                    map.setView(new ol.View({
                        projection: proj,
                        center: imgCenter,
                        zoom: 2,
                        extent: [0, -imgHeight, imgWidth, 0]
                    }))

                    featureOverlay.getFeatures().clear();
                    $scope.features = [];
                    populateFeatures();

                });

                imgWidth = $scope.img.width;
                imgHeight = $scope.img.height;
                url = $scope.img.path;

                var crossOrigin = 'anonymous';

                var imgCenter = [imgWidth / 2, - imgHeight / 2];

                var proj = new ol.proj.Projection({
                  code: 'ZOOMIFY',
                  units: 'pixels',
                  extent: [0, 0, imgWidth, imgHeight]
                });

                var tileSource = new ol.source.Zoomify({
                  url: url,
                  size: [imgWidth, imgHeight],
                  crossOrigin: crossOrigin
                });

                var tile = new ol.layer.Tile({source: tileSource});

                map = new ol.Map({
                    controls: ol.control.defaults().extend([
                        new ol.control.FullScreen()
                    ]),
                    layers: [tile],
                    target: 'map',
                    view: new ol.View({
                        projection: proj,
                        center: imgCenter,
                        zoom: 2,
                        extent: [0, -imgHeight, imgWidth, 0]
                    }),
                    logo: false
                });

                var myControl = new ol.control.Control({element: controls});
                map.addControl(myControl);

                var featureOverlay = new ol.FeatureOverlay({
                    style: new ol.style.Style({
                        fill: new ol.style.Fill({
                            color: 'rgba(255, 255, 255, 0.2)'
                        }),
                        stroke: new ol.style.Stroke({
                            color: '#ffcc33',
                            width: 2
                        }),
                        image: new ol.style.Circle({
                            radius: 7,
                            fill: new ol.style.Fill({
                                color: '#ffcc33'
                            })
                        })
                    })
                });
                featureOverlay.setMap(map);

                var highlightOverlay = new ol.FeatureOverlay({
                    style: new ol.style.Style({
                        fill: new ol.style.Fill({
                            color: 'rgba(125, 0, 255, 0.2)'
                        }),
                        stroke: new ol.style.Stroke({
                            color: '#33ccff',
                            width: 3
                        }),
                        image: new ol.style.Circle({
                            radius: 7,
                            fill: new ol.style.Fill({
                                color: '#33ccff'
                            })
                        })
                    })
                });
                highlightOverlay.setMap(map);

                function populateFeatures() {
                    ImageReferenceFeatures.query({id: $scope.img.id}, function(result) {
                        for (i = 0; i < result.length; i++) {
                            var feat = wrt.readFeature(result[i].wkt);
                            feat.id = result[i].id;
                            $scope.features.push(feat);
                            featureOverlay.addFeature(feat);
                        }
                    });
                }

    //            var modify = new ol.interaction.Modify({
    //                features: featureOverlay.getFeatures(),
    //                deleteCondition: function(event) {
    //                    return ol.events.condition.shiftKeyOnly(event) && ol.events.condition.singleClick(event);
    //                }
    //            });
    //            map.addInteraction(modify);

                $scope.drawType = 'LineString';

                var draw;
                function addInteraction() {
                    draw = new ol.interaction.Draw({
                        features: featureOverlay.getFeatures(),
                        type: $scope.drawType
                    });
                    // Saving the original event handler to call it after our own event processing
                    var originalHandler = draw.handleEvent;
                    // Overriding the event handler with our own. This one checks if the event occured within the image
                    // before allowing it to be processed any further.
                    draw.handleEvent = function(mapBrowserEvent) {
                        var pass = true;
                        if(mapBrowserEvent.coordinate[0] < 0
                          || mapBrowserEvent.coordinate[0] > $scope.img.width
                          || mapBrowserEvent.coordinate[1] > 0
                          || mapBrowserEvent.coordinate[1] < -$scope.img.height) {
                            return false
                        }
                        // If everything looks fine, resume the original processing of the event.
                        return originalHandler.call(draw,mapBrowserEvent);
                    }

                    draw.on('drawend', function(event) {
                        User.get({login: $scope.account.login}, function(usr) {
                            Feature.update({wkt: wrt.writeFeature(event.feature), reference: true, id: null, user: usr, image: $scope.img}, function(f,g) {
                                var header = g().location.split("/");
                                var nId = header[header.length - 1];
                                var nFeat = event.feature;
                                nFeat.id = nId;
                                $scope.features.push(nFeat);
                                nFeat.on('change', function() {console.log("Feature "+nFeat.id+" changed.")})
                            });
                        });
                    });
                    map.addInteraction(draw);
                }

                $scope.$watch('drawType', function() {
                    map.removeInteraction(draw);
                    addInteraction();
                });

                $scope.displayControls = true;
                $scope.toggleControls = function() {$scope.displayControls = !$scope.displayControls}

                $scope.highlightFeature = function(idx, b) {
                    var feat = $scope.features[idx];
                    if(b) {highlightOverlay.addFeature(feat)}
                    else {highlightOverlay.removeFeature(feat)}
                }

                $scope.removeFeature = function(idx) {
                    var feat = $scope.features[idx];
                    Feature.delete({id: feat.id}, function() {
                        $scope.features.splice(idx,1);
                        featureOverlay.removeFeature(feat);
                        highlightOverlay.removeFeature(feat);
                    });
                }

            });

        });
    });
