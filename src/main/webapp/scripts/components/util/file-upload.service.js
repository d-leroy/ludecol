'use strict';

angular.module('ludecolApp')
    .service('FileUpload', function ($http) {
        var uploadFileToUrl = function(data, uploadUrl, successCallback){
            var fd = new FormData();
            angular.forEach(data,function(value,key){fd.append(key,value);});
            $http.post(uploadUrl, fd, {
                transformRequest: angular.identity,
                headers: {'Content-Type': undefined}
            })
            .success(function(){
                successCallback();
            })
            .error(function(status){
                console.dir(status);
            });
        }

        return {uploadFileToUrl: uploadFileToUrl};
    });
