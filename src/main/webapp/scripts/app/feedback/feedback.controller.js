'use strict';

angular.module('ludecolApp')

    .factory('Feedback', function ($resource) {
            return $resource('api/feedback', {}, {
                'query': { method: 'GET', isArray: true},
                'get': {
                    method: 'GET',
                    transformResponse: function (data) {
                        data = angular.fromJson(data);
                        return data;
                    }
                },
                'update': { method:'PUT' }
            });
        })

    .controller('FeedbackController', function ($scope, $state, Principal, Feedback, TutorialModalService) {

        $scope.questions = [
            {title:"Attractivité", rating: 0, answer: ""},
            {title:"Durée d'une partie", rating: 0, answer: ""},
            {title:"Ergonomie de l'interface", rating: 0, answer: ""},
            {title:"Qualité du tutorial", rating: 0, answer: ""},
            {title:"Logique", rating: 0, answer: ""}
        ];

        $scope.other = "";

        $scope.submit = function() {
            var res = [];
            angular.forEach($scope.questions, function(v) {
                res.push(v);
            });
            res.push({title:"Other", rating: 0, answer: $scope.other});
            Feedback.save(res);

            var modalInstance = TutorialModalService();
            modalInstance.result.then(function () {$state.go('home')},function () {});
        }

        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
        });
    });
