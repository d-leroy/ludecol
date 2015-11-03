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
    .factory('FeedbackModalService', function($modal) {
        return function() {
            return $modal.open({
                templateUrl: 'scripts/app/modal/feedback-modal.html',
                controller: 'DefaultModalInstanceCtrl',
                backdrop: 'static',
                keyboard: false
            });
        }
    })
    .factory('SkipModalService', function($modal) {
        return function() {
            return $modal.open({
                templateUrl: 'scripts/app/modal/skip-modal.html',
                controller: 'DefaultModalInstanceCtrl',
                backdrop: 'static',
                keyboard: false
            });
        }
    });
