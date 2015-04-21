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

            $scope.chartW = {
                options: {
                    chart: {type: 'line'},
                    plotOptions: {line: {dataLabels: {enabled: true}}},
                    tooltip: {shared: true, crosshairs: true}
                },
                title: {text: 'Window and Result size (num of triples)'},
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
                xAxis: {
                    labels: {
                        format: '{value} s'
                    }
                },
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
                var seriesW = [
                    {name: 'Result size (actual)', data: []},
                    {name: 'Result size (expected)', data: []},
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
                    if (values.length > 6) {
                        seriesRP[0].data.push(values[0] * 100);
                        seriesRP[1].data.push(values[1] * 100);

                        seriesW[0].data.push(values[2]);
                        seriesW[1].data.push(values[3]);
                        seriesW[2].data.push(values[4]);
                    }
                });
                $scope.chartRP.series = seriesRP;
                $scope.chartRP.xAxis = xAxis;

                $scope.chartW.series = seriesW;
                $scope.chartW.xAxis = xAxis;
            };
            
            $scope.loadPData = function ($fileContent) {
                var lines = $fileContent.split('\n');
                lines = lines.slice(1,lines.length-1);
                var seriesP = [
                    {name: 'Memory Usage', yAxis: 0, data: [],
                        dataLabels: {
                            enabled: true,                                         
                            formatter: function() {
                                    return this.y + ' MB';
                                }},                    
                        tooltip: { valueSuffix: ' MB'}},
                    {name: 'Memory Usage %', yAxis: 1, data: [],
                        dataLabels: {
                            enabled: true,                                         
                            formatter: function() {
                                    return this.y + ' %';
                                }},   
                        tooltip: { valueSuffix: ' %'}},
                    {name: 'CPU %', yAxis: 2, data: [],
                        dataLabels: {
                            enabled: true,                                         
                            formatter: function() {
                                    return this.y + ' %';
                                }},   
                        tooltip: { valueSuffix: ' %'}},
                    {name: 'Threads', yAxis: 3, data: []}
                ];
                var xAxis = {
                    //categories: [],
                    title: {
                        text: 'Time'
                        //format: 'Window #{value}'
                    }
                };
                
                var old1 = -1;
                var old2 = -1;
                var old3 = -1;
                var old4 = -1;
                var first = true;
                var last = false;
                angular.forEach(lines, function (points, index) {
                    if (lines.length-1 == index)
                        last = true;

                    var values = points.split(',').map(function (item) {
                        return parseFloat(item);
                    });
                    //xAxis.categories.push((values[0])+'s');
                    if (values.length > 4) {                        
                        if (old1 != values[1] && !first) {
                            seriesP[0].data.push([prevtime,old1]);
                            seriesP[0].data.push([values[0],values[1]]);
                        } else if ((old1 != values[1] && first) || last) {
                            seriesP[0].data.push([values[0],values[1]]);
                        }
                        
                        if (old2 != values[3] && !first) {
                            seriesP[1].data.push([prevtime,old2]);
                            seriesP[1].data.push([values[0],values[3]]);
                        } else if ((old2 != values[3] && first) || last) {
                            seriesP[1].data.push([values[0],values[3]]);
                        }
                        
                        if (old3 != values[2]  && !first) {
                            seriesP[2].data.push([prevtime,old3]);
                            seriesP[2].data.push([values[0],values[2]]);
                        } else if ((old3 != values[2] && first) || last) {
                            seriesP[2].data.push([values[0],values[2]]);
                        }
                        
                        if (old4 != values[4] && !first) {
                            seriesP[3].data.push([prevtime,old4]);
                            seriesP[3].data.push([values[0],values[4]]);
                        } else if ((old4 != values[4] && first) || last) {
                            seriesP[3].data.push([values[0],values[4]]);
                        }
                        
                        old1 = values[1];
                        old2 = values[3];
                        old3 = values[2];
                        old4 = values[4];
                        
                        prevtime = values[0];
                        first = false;
                        
                    }
                });

                $scope.chartP.series = seriesP;
                $scope.chartP.xAxis = xAxis;
            };
        }
    ]);
})(window.angular, window.Highcharts);