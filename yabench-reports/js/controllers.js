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
                title: {text: 'Precision/Recall'},
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
            
            $scope.chartP = {
                options: {
                    chart: {type: 'line'},
                    plotOptions: {line: {dataLabels: {enabled: true}}},
                    tooltip: {shared: true, crosshairs: true}
                },
                title: {text: 'Performance metrics'},
                series: [],
                yAxis: [{ // Primary yAxis
                    min : 0,
                    labels: {
                        format: '{value}MB',
                        style: {
                            color: Highcharts.getOptions().colors[0]
                        }
                    },
                    title: {
                        text: 'Memory Usage',
                        style: {
                            color: Highcharts.getOptions().colors[0]
                        }
                    }
                }, { // Secondary yAxis
                    gridLineWidth: 0,
                    min : 0,    
                    title: {
                        text: 'Memory Usage %',
                        style: {
                            color: Highcharts.getOptions().colors[1]
                        }
                    },
                    labels: {
                        format: '{value} %',
                        style: {
                            color: Highcharts.getOptions().colors[1]
                        }
                    }

                }, { // Tertiary yAxis
                    gridLineWidth: 0,
                    title: {
                        text: 'CPU Usage %',
                        style: {
                            color: Highcharts.getOptions().colors[2]
                        }
                    },
                    labels: {
                        format: '{value} %',
                        style: {
                            color: Highcharts.getOptions().colors[2]
                        }
                    },
                    opposite: true
                },
                { // 4 Axis
                    gridLineWidth: 0,
                    title: {
                        text: 'Threads',
                        style: {
                            color: Highcharts.getOptions().colors[3]
                        }
                    },
                    labels: {
                        format: '{value}',
                        style: {
                            color: Highcharts.getOptions().colors[3]
                        }
                    },
                    opposite: true
                }]
            };

            $scope.loadData = function ($fileContent) {
                var lines = $fileContent.split('\n');
                var seriesRP = [
                    {name: 'Precision', data: []},
                    {name: 'Recall', data: []}
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
            
            $scope.loadPData = function ($fileContent) {
                var lines = $fileContent.split('\n');
                lines = lines.slice(1,lines.length-1);
                var seriesP = [
                    {name: 'Memory Usage', yAxis: 0, data: [],
                        tooltip: { valueSuffix: ' MB'}},
                    {name: 'Memory Usage %', yAxis: 1, data: [],
                        tooltip: { valueSuffix: ' %'}},
                    {name: 'CPU %', yAxis: 2, data: [],
                        tooltip: { valueSuffix: ' %'}},
                    {name: 'Threads', yAxis: 3, data: []}
                ];
                var xAxis = {
                    categories: [],
                    title: {
                        text: 'Time'
                        //format: 'Window #{value}'
                    }
                };
                
                length = lines.length;
                for(var i=0;i<length;i++) {
                        var values = lines[i].split(',').map(function (item) {
                            return parseFloat(item);
                        });
                        xAxis.categories.push((values[0])+'s');
                        if (values.length > 4) {
                            seriesP[0].data.push(values[1]);
                            seriesP[1].data.push(values[3]);
                            seriesP[2].data.push(values[2]);
                            seriesP[3].data.push(values[4]);
                        }
                }
                $scope.chartP.series = seriesP;
                $scope.chartP.xAxis = xAxis;
            };
        }
    ]);
})(window.angular, window.Highcharts);