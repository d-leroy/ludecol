'use strict';

angular.module('ludecolApp')
    .controller('StatisticsController', function ($scope, Principal, Auth, User, UserStatistics) {
        Principal.identity().then(function(account) {
            $scope.settingAccount = account;
            $scope.statistics = [];
            $scope.bonusPoints = 0;
            $scope.totalEarnedPoints = 0;

            UserStatistics.get({login:account.login}, function(res) {
                $scope.statistics = res.gameModeStatisticsDTOs;
                $scope.bonusPoints = res.bonusPoints;
                $scope.totalEarnedPoints = res.totalEarnedPoints;
            })

        });

        $scope.getMode = function(mode) {
            switch(mode) {
                case 'PlantIdentification': return "Plant Identification";
                case 'AnimalIdentification': return "Animal Identification";
                case 'AllStars': return "All Stars";
                default: return "";
            }
        }
    });
