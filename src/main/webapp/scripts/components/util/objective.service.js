'use strict';

angular.module('ludecolApp')
    .factory('ObjectiveService', function (Objective) {

        var _polling = false;
        var _onObjectivesReceived = function() {};
        var _onLongPollingStopped = function() {};
        var _onLongPollingStarted = function() {};
        var _onLogin = function() {};
        var _onLogout = function() {};

        function _getUpdatedObjectives(force) {
            Objective.query({force:force}, function(result) {
                if(_polling) {
                    _onObjectivesReceived(result);
                    _getUpdatedObjectives(false);
                }
            });
        }

        return {
            startLongPolling: function(force) {
                if(_polling === false) {
                    _polling = true;
                    _onLongPollingStarted();
                    _getUpdatedObjectives(force);
                }
            },
            stopLongPolling: function() {
                if(_polling === true) {
                    _polling = false;
                    _onLongPollingStopped();
                }
            },
            login: function() {_onLogin();},
            logout: function() {_onLogout();},
            setOnObjectivesReceived: function(f) {_onObjectivesReceived = f;},
            setOnLongPollingStopped: function(f) {_onLongPollingStopped = f;},
            setOnLongPollingStarted: function(f) {_onLongPollingStarted = f;},
            setOnLogin: function(f) {_onLogin = f;},
            setOnLogout: function(f) {_onLogout = f;}
        };
    });
