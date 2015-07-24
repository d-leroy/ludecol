'use strict';

angular.module('ludecolApp')
.controller('TutorialModalInstanceCtrl', function ($scope, $modalInstance) {

  $scope.ok = function () {
    $modalInstance.close(true);
  };

  $scope.cancel = function () {
    $modalInstance.close(false);
  };

});
