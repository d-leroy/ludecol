'use strict';

angular.module('ludecolApp')
    .factory('AllStarsGameService', function (RadioModel, UserGame, Game, MapService, GameService) {

        var _submitGame;

        function _getResult() {
            return RadioModel.data;
        }

        //-------------------API

        var initializeGame = function(login,successCallback,errorCallback) {
            MapService.destroyMap();
            _submitGame = GameService.initializeGame(login,'AllStars',function(){return {};},
                _getResult,successCallback,errorCallback,UserGame.query,Game.update);
        };

        var submitGame = function(){
            _submitGame();
        };

        return {initializeGame: initializeGame, submitGame: submitGame};
    });
