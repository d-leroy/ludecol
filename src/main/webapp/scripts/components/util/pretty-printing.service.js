'use strict';

angular.module('ludecolApp')
    .factory('PrettyPrinting', function () {

        var getTime = function(time) {
            var unit;
            time = time / 1000 | 0; //convert to seconds
            if(time / 60 | 0 >= 1) {
                time = time / 60 | 0; //convert to minutes
                if(time / 60 | 0 >= 1) {
                    time = time / 60 | 0; //convert to hours
                    if(time / 24 | 0 >= 1) {
                        time = time / 24 | 0; //convert to days
                        if(time / 30 | 0 >= 1) {
                            time = time / 30 | 0; //convert to months
                            if(time / 365 | 0 >= 1) {
                                time = time / 365 | 0; //convert to years
                                unit = time > 1 ? " years ago" : " year ago";
                            }
                            else {unit = time > 1 ? " months ago" : " month ago";}
                        }
                        else {unit = time > 1 ? " days ago" : " day ago";}
                    }
                    else {unit = time > 1 ? " hours ago" : " hour ago";}
                }
                else {unit = time > 1 ? " minutes ago" : " minute ago";}
            }
            else {unit = time > 1 ? " seconds ago" : " second ago";}
            return time.toString() + unit;
        }

        var getMode = function(mode,capitalized,short) {
            var res;
            switch(mode) {
                case 'PlantIdentification':
                    res = capitalized ? "Plants" : "plants";
                    res += short ? "" : " identification";
                    return res;
                case 'AnimalIdentification':
                    res = capitalized ? "Animals" : "animals";
                    res += short ? "" : " identification";
                    return res;
                case 'AllStars':
                    return capitalized ? "All stars" : "all stars";
                default:
                    return "";
            }
        }

        return {getTime: getTime, getMode: getMode};
    });
