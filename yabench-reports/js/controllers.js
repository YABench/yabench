(function (angular, Highcharts) {
    var module = angular.module('yabench-controllers', ['highcharts-ng']);

    module.controller('IndexCtrl', ['$scope',
        function ($scope) {
            $scope.chartRP = {
                options: {
                    chart: {type: 'line'},
                    plotOptions: {
                        line: {
                            dataLabels: {
                                enabled: true,
                                formatter: function() {
                                    return this.y.toPrecision(3) + '%';
                                }
                            }
                        }
                    },
                    tooltip: {
                        shared: true,
                        crosshairs: true, 
                        valueSuffix: '%',
                        valueDecimals: 2
                    }
                },
                title: {text: 'Recall/Precision'},
                series: [],
                yAxis: {title: {text: 'Percentage (%)'}, max: 100}
            };
            $scope.chartD = {
                options: {
                    chart: {type: 'line'},
                    plotOptions: {line: {dataLabels: {enabled: true}}},
                    tooltip: {
                        shared: true, crosshairs: true, valueSuffix: 'ms'
                    }
                },
                title: {text: 'Delay'},
                series: [],
                yAxis: [
                    {
                        labels: {format: '{value}ms'},
                        title: {text: 'Delay (ms)'}
                    }
                ]
            };
            $scope.chartW = {
                options: {
                    chart: {type: 'line'},
                    plotOptions: {line: {dataLabels: {enabled: true}}},
                    tooltip: {shared: true, crosshairs: true}
                },
                title: {text: 'Window size (num of triples)'},
                series: [],
                yAxis: [{title: {text: 'Number of triples'}}]
            };

            $scope.loadData = function ($fileContent) {
                var lines = $fileContent.split('\n');
                var seriesRP = [
                    {name: 'Recall', data: []},
                    {name: 'Precision', data: []}
                ];
                var seriesD = [
                    {name: 'Delay', data: []}
                ];
                var seriesW = [
                    {name: 'Window size (actual)', data: []},
                    {name: 'Window size (expected)', data: []}
                ];
                var xAxis = {
                    categories: [],
                    title: {
                        text: 'Windows',
                        format: 'Window #{value}'
                    }
                };
                angular.forEach(lines, function (points, index) {
                    xAxis.categories.push('#' + (index + 1));
                    var values = points.split(',').map(function (item) {
                        return parseFloat(item);
                    });
                    if (values.length > 4) {
                        seriesRP[0].data.push(values[0] * 100);
                        seriesRP[1].data.push(values[1] * 100);

                        seriesD[0].data.push(values[2]);

                        seriesW[0].data.push(values[3]);
                        seriesW[1].data.push(values[4]);
                    }
                });
                $scope.chartRP.series = seriesRP;
                $scope.chartRP.xAxis = xAxis;
                $scope.chartD.series = seriesD;
                $scope.chartD.xAxis = xAxis;
                $scope.chartW.series = seriesW;
                $scope.chartW.xAxis = xAxis;
            };
        }
    ]);
})(window.angular, window.Highcharts);