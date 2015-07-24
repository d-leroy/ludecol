'use strict';

angular.module('ludecolApp')
    .factory('SubmitModalService', function($modal) {
        return function() {
            return $modal.open({
                templateUrl: 'scripts/app/modal/submit-modal.html',
                controller: 'SubmitModalInstanceCtrl',
                backdrop: 'static',
                keyboard: false
            });
        }
    })
    .factory('TrainingSubmitModalService', function($modal) {
        return function() {
            return $modal.open({
                templateUrl: 'scripts/app/modal/training-submit-modal.html',
                controller: 'TrainingSubmitModalInstanceCtrl',
                backdrop: 'static',
                keyboard: false
            });
        }
    })
    .factory('TutorialModalService', function($modal) {
        return function() {
            return $modal.open({
                templateUrl: 'scripts/app/modal/tutorial-modal.html',
                controller: 'TutorialModalInstanceCtrl',
                backdrop: 'static',
                keyboard: false
            });
        }
    });
