'use strict';

angular.module('ludecolApp')
    .factory('AllStarsGameService', function (RadioModel, UserGame, Game, ImageService, GameService) {

        var _submitGame, _skipGame;

        function _getResult() {
            return RadioModel.data;
        }

        //-------------------API

        var initializeGame = function(login,successCallback,errorCallback) {
            ImageService.destroyMap();
            var services = GameService.initializeGame(login,'AllStars',function(){return {};},
                _getResult,successCallback,errorCallback,UserGame.query,Game.update,Game.delete);
            _submitGame = services.submitGame;
            _skipGame = services.skipGame;
        };

        var submitGame = function(){
            _submitGame();
        };

        var skipGame = function(){
            _skipGame();
        };

        return {initializeGame: initializeGame, submitGame: submitGame, skipGame: skipGame};
    });
