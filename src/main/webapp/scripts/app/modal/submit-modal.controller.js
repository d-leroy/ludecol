'use strict';

angular.module('ludecolApp')
.controller('SubmitModalInstanceCtrl', function ($scope, $modalInstance, Fact) {

    $scope.fact = Fact.get();

    $scope.ok = function () {
        $modalInstance.close(true);
    };

    $scope.cancel = function () {
        $modalInstance.close(false);
    };

});
