(function (angular, Highcharts) {
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
                    text: 'Delay/Window size'
                },
                series: [],
                yAxis: [
                    {
                        labels: {
                            format: '{value}ms',
                            style: {
                                color: Highcharts.getOptions().colors[0]
                            }
                        },
                        title: {
                            text: 'Delay (ms)',
                            style: {
                                color: Highcharts.getOptions().colors[0]
                            }
                        }
                    },
                    {
                        labels: {
                            style: {
                                color: Highcharts.getOptions().colors[1]
                            }
                        },
                        title: {
                            text: 'Number of triples',
                            style: {
                                color: Highcharts.getOptions().colors[1]
                            }
                        },
                        opposite: true
                    }
                ]
            };

            $scope.loadData = function ($fileContent) {
                var lines = $fileContent.split('\n');
                var seriesRP = [
                    {name: 'Recall', data: []}, 
                    {name: 'Precision', data: []}
                ];
                var seriesD = [
                    {name: 'Delay', data: [], yAxis: 0},
                    {name: 'Window size (actual)', data: [], yAxis: 1},
                    {name: 'Window size (expected)', data: [], yAxis: 1}
                ];
                var xAxis = {
                    categories: [],
                    title: {
                        text: 'Windows'
                    }
                };
                angular.forEach(lines, function (points, index) {
                    xAxis.categories.push(index + 1);
                    var values = points.split(',').map(function (item) {
                        return parseFloat(item);
                    });
                    if (values.length > 4) {
                        seriesRP[0].data.push(values[0] * 100);
                        seriesRP[1].data.push(values[1] * 100);

                        seriesD[0].data.push(values[2]);
                        seriesD[1].data.push(values[3]);
                        seriesD[2].data.push(values[4]);
                    }
                });
                $scope.chartRP.series = seriesRP;
                $scope.chartRP.xAxis = xAxis;
                $scope.chartD.series = seriesD;
                $scope.chartD.xAxis = xAxis;
            };
        }
    ]);
})(window.angular, window.Highcharts);