'use strict';

angular.module('ludecolApp')
    .factory('GameService', function ($state, UserGame, UserExpertGame, Game, ExpertGame, Image, PlantGameService,
        AnimalGameService, AllStarsGameService, ExpertAnimalGameService, SubmitModalService) {

        var _mode, _login, _success_callback, _error_callback, _game, _queryFunction, _updateFunction;

        function _empty_result_provider() {return {};}

        function _getResult() {
            var result = _empty_result_provider();
            switch(_mode) {
                case 'AllStars': return AllStarsGameService.getResult(result);
                case 'PlantIdentification': return PlantGameService.getResult(result);
                case 'AnimalIdentification': return AnimalGameService.getResult(result);
                case 'ExpertAnimalIdentification': return ExpertAnimalGameService.getResult(result);
                default: return result;
            }
        }

        //-------------------API

        var initializeGame = function(login,mode,empty_result_provider,success_callback,error_callback,force) {
            _login = login;
            _mode = mode;
            _empty_result_provider = empty_result_provider;
            _error_callback = error_callback;
            _success_callback = success_callback;

            switch(_mode) {
                case 'AllStars':
                case 'PlantIdentification':
                case 'AnimalIdentification':
                    _queryFunction = UserGame.query;
                    _updateFunction = Game.update;
                    break;
                case 'ExpertAnimalIdentification':
                    _queryFunction = UserExpertGame.query;
                    _updateFunction = ExpertGame.update;
                    break;
            }

            _queryFunction({login: _login, completed: false, mode: _mode}, function(result) {
                if(result.length === 0) {
                    _game = {
                        usr: _login,
                        game_mode: _mode,
                        game_result: _empty_result_provider(),
                        completed: false
                    };
                    _updateFunction(_game, function(f,g) {
                        Image.get({id: f.img}, function(i) {
                            _game = f;
                            _success_callback(i,_game,force);
                        });
                    }, _error_callback);
                }
                else {
                    _game = result[0];
                    Image.get({id: _game.img}, function(i) {success_callback(i,_game,force);});
                }
            });
        }

        var submitGame = function() {
            var res = _getResult();
            _game.game_result = res;
            _game.completed = true;
            _updateFunction(_game, function() {
                var modalInstance = SubmitModalService();
                modalInstance.result.then(
                    function (newGame) {
                        if(newGame) {initializeGame(_login,_mode,_empty_result_provider,_success_callback,_error_callback,false)}
                        else {$state.go('home')}
                    },
                    function () {

                    });
            });
        }

        var getMode = function() {return _mode;}

        return {initializeGame: initializeGame, submitGame: submitGame, getMode: getMode};
    });
