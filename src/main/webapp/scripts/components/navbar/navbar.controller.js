'use strict';

angular.module('ludecolApp')
    .controller('NavbarController', function ($scope, $location, $state, Auth, Principal, Notification, NotificationService, ObjectiveService, PrettyPrinting) {
        $scope.isAuthenticated = Principal.isAuthenticated;
        $scope.isInRole = Principal.isInRole;

        $scope.logout = function () {
            Auth.logout();
            $state.go('home');
        };

        $scope.markAsRead = function(idx,event) {
            event.stopPropagation();
            Notification.update($scope.notifications[idx], function() {
                $scope.notifications.splice(idx,1);
            })
        };

        $scope.notifications = [];

        NotificationService.setOnNotificationReceived(function(result) {$scope.notifications = $scope.notifications.concat(result);});
        NotificationService.setOnLogout(function() {$scope.notifications = [];});

        ObjectiveService.setOnObjectivesReceived(function(result) {
            $scope.ongoing_objectives = [];
            $scope.pending_objectives = [];
            for(var i=0,ii=result.length;i<ii;i++) {
                var completed = result[i].completed_games;
                var toComplete = result[i].games_to_complete;
                var toCorrect = toComplete - result[i].pending_games.length;
                if(completed < toComplete) {
                    //It's an ongoing objective
                    $scope.ongoing_objectives.push({
                        title: "Complete " + toComplete + " " + PrettyPrinting.getMode(result[i].game_mode,false,true) + " games",
                        state: "Completed : " + completed + "/" + toComplete
                    });
                }
                else {
                    //It's a pending objective
                    $scope.pending_objectives.push({
                        title: "Complete " + toComplete + " " + PrettyPrinting.getMode(result[i].game_mode,false,true) + " games",
                        state: "Corrected : " + toCorrect + "/" + toComplete
                    });
                }
            }
        });

        ObjectiveService.setOnLogout(function() {$scope.ongoing_objectives = []; $scope.pending_objectives = []});

        //Does not work when the user logs in, but works on page (re)loads.
        Principal.identity().then(function(account) {
            if(account !== null) {
                NotificationService.startLongPolling(true);
//                ObjectiveService.startLongPolling(true);
            }
        });
    });
