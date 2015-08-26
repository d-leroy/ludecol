'use strict';

angular.module('ludecolApp')
    .factory('ExpertAllStarsGameService', function (RadioModel, UserExpertGame, ExpertGame, MapService, GameService) {

        var _submitGame;

        function _getResult() {
            return RadioModel.data;
        }

        //-------------------API

        var initializeGame = function(login,successCallback,errorCallback) {
            MapService.destroyMap();
            _submitGame = GameService.initializeGame(login,'AllStars',function(){return {};},
                _getResult,successCallback,errorCallback,UserExpertGame.query,ExpertGame.update);
        };

        var submitGame = function(){
            _submitGame();
        };

        return {initializeGame: initializeGame, submitGame: submitGame};
    });
