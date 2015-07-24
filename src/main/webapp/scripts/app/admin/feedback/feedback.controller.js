'use strict';

angular.module('ludecolApp')
    .controller('FeedbackAdminController', function ($scope, $state, Principal, Feedback, TutorialModalService) {

        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;

            Feedback.query(function(res){$scope.feedbacks = res;})
        });
    });
