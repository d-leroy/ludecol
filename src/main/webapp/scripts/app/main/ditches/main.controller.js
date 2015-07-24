'use strict';

angular.module('ludecolApp')
    .controller('DitchesMainController', function ($scope, Principal) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
            $scope.isTrainingCollapsed = true;
            $scope.isExpertCollapsed = true;
            $scope.isPlayCollapsed = true;
        });
    });
