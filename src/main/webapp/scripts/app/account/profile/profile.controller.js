'use strict';

angular.module('ludecolApp')
    .controller('ProfileController', function ($scope, $interval, Principal, Auth, User, UserLastCompletedGame, PrettyPrinting) {
        $scope.success = null;
        $scope.error = null;
        $scope.rank = 50;
        $scope.score = 0;

        $scope.$watch('rank',function(n,o) {$scope.rankStyle = computeStyle((50 - n) * 2);});
        $scope.$watch('score',function(n,o) {$scope.scoreStyle = computeStyle(n);});
        $scope.$on('$destroy',function() {$interval.cancel(timer); timer = undefined;});

        function computeStyle(percent) {
            var increment = 180 / 100;
            var deg = Math.floor(-90 + (increment * (100 - percent)));
            var degString = deg.toString() + "deg";
            return {
                'width': '200px',
                'height': '200px',
                'border-top-left-radius': '50%',
                'border-bottom-left-radius': '50%',
                'background-color': '#9DB439',
                'background-image': "linear-gradient("+degString+", #A8CFFA 50%, rgba(0, 0, 0, 0) 50%, rgba(0, 0, 0, 0)), linear-gradient(270deg, #A8CFFA 50%, #9DB439 50%, #9DB439)"
            }
        }

        $scope.getTime = function(t) {
            var elapsed = $scope.now - t;
            return PrettyPrinting.getTime(elapsed);
        }

        $scope.getMode = PrettyPrinting.getMode;

        var timer;

        Principal.identity().then(function(account) {
            $scope.settingAccount = account;
            User.get({login: $scope.settingAccount.login}, function(result) {
                $scope.rank = result.rank;
                $scope.score = result.score;
            })
            UserLastCompletedGame.query({},function(result) {
                $scope.gameList = result;
                $scope.now = new Date().getTime();
                timer = $interval(function() {$scope.now = new Date().getTime();}, 1000);
            })
        });
    });
