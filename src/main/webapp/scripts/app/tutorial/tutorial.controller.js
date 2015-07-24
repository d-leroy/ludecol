'use strict';

angular.module('ludecolApp')
    .controller('TutorialController', function ($scope, Principal) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
            $scope.isPracticeCollapsed = true;
            $scope.isNormalCollapsed = true;
            $scope.isExpertCollapsed = true;
            $scope.isAllStarsCollapsed = true;
            $scope.isPlantsCollapsed = true;
            $scope.isAnimalsCollapsed = true;
        });
    });
