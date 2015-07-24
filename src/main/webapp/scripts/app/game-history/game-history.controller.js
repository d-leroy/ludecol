'use strict';

angular.module('ludecolApp')
    .controller('GameHistoryController', function ($scope, $interval, Principal, Auth, User, UserPagedGame, PrettyPrinting) {
        $scope.loadPage = function() {
            UserPagedGame.query({login: $scope.settingAccount.login, completed: true, page: $scope.currentPage-1}, function(res) {
                $scope.totalItems = res.totalElements;
                $scope.gameList = res.content;
                $scope.itemsPerPage = res.size;
                $scope.now = new Date().getTime();
            })
        }

        $scope.$on('$destroy',function() {$interval.cancel(timer); timer = undefined;});

        $scope.getTime = function(t) {
            var elapsed = $scope.now - t;
            return PrettyPrinting.getTime(elapsed);
        }

        $scope.getMode = PrettyPrinting.getMode;

        var timer;

        Principal.identity().then(function(account) {
            $scope.settingAccount = account;
            $scope.loadPage();
            timer = $interval(function() {$scope.now = new Date().getTime();}, 1000);
        });
    });
