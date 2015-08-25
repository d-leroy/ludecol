'use strict';

angular.module('ludecolApp')
    .factory('GameService', function ($state, Image, SubmitModalService, MapService) {

        var _mode, _login, _successCallback, _errorCallback, _game, _queryFunction, _updateFunction;

        function _emptyResultSupplier() {return {};}

        function _resultSupplier() {return _emptyResultSupplier();}

        function _startNewGame() {
            var modalInstance = SubmitModalService();
            modalInstance.result.then(
                function (newGame) {
                    if(newGame) {initializeGame(_login,_mode,_emptyResultSupplier,_resultSupplier,_successCallback,_errorCallback,_queryFunction,_updateFunction)}
                    else {$state.go('home');}
                },
                function () {

                });
        }

        var _submitGame = function(inputSubmitCallback) {
            var res = _resultSupplier();
            _game.game_result = res;
            _game.completed = true;
            console.dir(_game);
            _updateFunction(_game, function(updatedResult) {
                if(inputSubmitCallback !== undefined) {
                    inputSubmitCallback(_startNewGame,updatedResult);
                }
                else {
                    _startNewGame();
                }
            });
        }

        //-------------------API

        var initializeGame = function(login,mode,emptyResultSupplier,resultSupplier,successCallback,errorCallback,queryFunction,updateFunction) {
            _login = login;
            _mode = mode;
            _emptyResultSupplier = emptyResultSupplier;
            _resultSupplier = resultSupplier;
            _errorCallback = errorCallback;
            _successCallback = successCallback;
            _queryFunction = queryFunction;
            _updateFunction = updateFunction;

            _queryFunction({login: _login, completed: false, mode: _mode}, function(result) {
                if(result.length === 0) {
                    _game = {
                        usr: _login,
                        game_mode: _mode,
                        game_result: _emptyResultSupplier(),
                        completed: false
                    };
                    _updateFunction(_game, function(updatedGame) {
                        Image.get({id: updatedGame.img}, function(image) {
                            _game = updatedGame;
                            _game.usr = _login;
                            _game.game_mode = _mode;
                            _successCallback(image,_game);
                        });
                    }, _errorCallback);
                }
                else {
                    _game = result[0];
                    _game.usr = _login;
                    _game.game_mode = _mode;
                    Image.get({id: _game.img}, function(image) {successCallback(image,_game);});
                }
            });

            return _submitGame;
        }

        return {initializeGame: initializeGame};
    });
