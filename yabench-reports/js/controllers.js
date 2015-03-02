(function (angular) {
    var module = angular.module('yabench-controllers', ['highcharts-ng']);

    module.controller('IndexCtrl', ['$scope',
        function ($scope) {
            $scope.chartRP = {
                options: {
                    chart: {
                        type: 'line'
                    },
                    plotOptions: {
                        line: {
                            dataLabels: {
                                enabled: true
                            },
                            enableMouseTracking: false
                        }
                    }
                },
                title: {
                    text: 'Recall/Precision'
                },
                series: [],
                yAxis: {
                    title: {
                        text: 'Percentage (%)'
                    }
                }
            };
            $scope.chartD = {
                options: {
                    chart: {
                        type: 'line'
                    },
                    plotOptions: {
                        line: {
                            dataLabels: {
                                enabled: true
                            },
                            enableMouseTracking: false
                        }
                    }
                },
                title: {
                    text: 'Delay'
                },
                series: [],
                yAxis: {
                    title: {
                        text: 'Milliseconds (ms)'
                    }
                }
            };

            $scope.loadData = function ($fileContent) {
                var lines = $fileContent.split('\n');
                var seriesRP = [{name: 'Recall', data: []}, {name: 'Precision', data: []}];
                var seriesD = [{name: 'Delay', data: []}];
                angular.forEach(lines, function (points) {
                    var values = points.split('\t').map(function (item) {
                        return parseInt(item);
                    });
                    if (values.length > 2) {
                        seriesRP[0].data.push(values[0]);
                        seriesRP[1].data.push(values[1]);

                        seriesD[0].data.push(values[2]);
                    }
                });
                $scope.chartRP.series = seriesRP;
                $scope.chartD.series = seriesD;
            };
        }
    ]);
})(window.angular);