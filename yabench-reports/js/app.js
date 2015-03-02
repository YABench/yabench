'use strict';

(function (angular) {
    var yabenchReports = angular.module('yabench-reports', [
        'ngRoute',
        'highcharts-ng',
        'yabench-controllers',
        'yabench-directives'
    ]);

    yabenchReports.config(['$routeProvider',
        function ($routeProvider) {
            $routeProvider
                .when('/', {
                    templateUrl: 'partials/index.html',
                    controller: 'IndexCtrl'
                })
                .otherwise({
                    redirectTo: '/'
                });
        }
    ]);
})(window.angular);