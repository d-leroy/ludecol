'use strict';

angular.module('ludecolApp')
    .factory('AllStarsGameService', function (RadioModel) {

        //-------------------API

        var getResult = function() {
            return RadioModel.data;
        }

        return {getResult: getResult};
    });
