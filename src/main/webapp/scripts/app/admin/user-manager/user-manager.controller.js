'use strict';

angular.module('ludecolApp')
    .controller('UserManagerController', function ($scope, Principal, UserPromotion) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
            $scope.username = "";

            $scope.promote = function() {
                UserPromotion.update({login: $scope.username},function() {
                    console.log("ok!");
                    $scope.username = "";
                });
            }

        });
    });
