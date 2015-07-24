'use strict';

angular.module('ludecolApp')
    .factory('NotificationService', function (Notification) {

        var _polling = false;
        var _onNotificationReceived = function() {};
        var _onLongPollingStopped = function() {};
        var _onLongPollingStarted = function() {};
        var _onLogin = function() {};
        var _onLogout = function() {};

        function _getLatestNotifications(force) {
            Notification.query({force: force}, function(result) {
                if(_polling) {
                    _onNotificationReceived(result);
                    _getLatestNotifications(false);
                }
            });
        }

        return {
            startLongPolling: function(force) {
                if(_polling === false) {
                    _polling = true;
                    _onLongPollingStarted();
                    _getLatestNotifications(force);
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
            setOnNotificationReceived: function(f) {_onNotificationReceived = f;},
            setOnLongPollingStopped: function(f) {_onLongPollingStopped = f;},
            setOnLongPollingStarted: function(f) {_onLongPollingStarted = f;},
            setOnLogin: function(f) {_onLogin = f;},
            setOnLogout: function(f) {_onLogout = f;}
        };
    });
