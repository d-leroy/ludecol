'use strict';

angular.module('ludecolApp')
.controller('TrainingSubmitModalInstanceCtrl', function ($scope, $modalInstance) {

  $scope.ok = function () {
    $modalInstance.close(true);
  };

  $scope.cancel = function () {
    $modalInstance.close(false);
  };

});
