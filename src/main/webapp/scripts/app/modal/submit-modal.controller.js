'use strict';

angular.module('ludecolApp')
.controller('SubmitModalInstanceCtrl', function ($scope, $modalInstance, Fact) {

    Fact.get(function(res) {
        var el = document.createElement("div");
        el.innerText = el.textContent = res.response;
        $scope.fact = el.innerHTML;
    });

    $scope.ok = function () {
        $modalInstance.close(true);
    };

    $scope.cancel = function () {
        $modalInstance.close(false);
    };

});
