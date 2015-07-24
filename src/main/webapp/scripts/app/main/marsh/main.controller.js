'use strict';

angular.module('ludecolApp')
    .controller('MarshMainController', function ($scope, Principal) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
            $scope.isTrainingCollapsed = true;
            $scope.isExpertCollapsed = true;
            $scope.isPlayCollapsed = true;
            $scope.isTtaCollapsed = true;
            $scope.moreDetails = false;
        });
    });
