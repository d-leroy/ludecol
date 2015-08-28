'use strict';

angular.module('ludecolApp')
    .factory('ExpertAllStarsGameService', function (RadioModel, UserExpertGame, ExpertGame, ImageService, GameService) {

        var _submitGame;

        function _getResult() {
            return RadioModel.data;
        }

        //-------------------API

        var initializeGame = function(login,successCallback,errorCallback) {
            ImageService.destroyMap();
            _submitGame = GameService.initializeGame(login,'AllStars',function(){return {};},
                _getResult,successCallback,errorCallback,UserExpertGame.query,ExpertGame.update);
        };

        var initializeReferenceDefinition = function(login,successCallback,errorCallback,img,submitCallback) {
            ImageService.destroyMap();
            _submitGame = GameService.initializeReferenceDefinition(login,'AllStars',function(){return {};},
                _getResult,successCallback,errorCallback,ExpertGame.update,img,submitCallback);
        };

        var submitGame = function(){
            _submitGame();
        };

        return {
            initializeGame: initializeGame,
            initializeReferenceDefinition: initializeReferenceDefinition,
            submitGame: submitGame
        };
    });
