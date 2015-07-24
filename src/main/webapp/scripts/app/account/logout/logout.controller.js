'use strict';

angular.module('ludecolApp')
    .controller('LogoutController', function (Auth) {
        Auth.logout();
    });
