(function (angular, Highcharts, Math) {
    var module = angular.module('yabench-controllers', ['highcharts-ng']);

    module.controller('IndexCtrl', ['$scope',
        function ($scope) {
            $scope.chartRP = {
                title: {text: 'Precision/Recall'},
                options: {
                    tooltip: {
                        formatter: function() {
                            var tooltip = '';
                            var tempTooltip = '';
                            var prev;
                            angular.forEach(this.points, function(point) {
                                var from, to, fromNext, toNext;
                                if(point.point.x1 || point.point.x1 === 0) {
                                    //Line chart
                                    from = point.point.x1;
                                    to = point.point.x2;
                                    tooltip += prev === point.point.name ? '' 
                                            : '<b>Window ' + point.point.name + '</b>'
                                                + '<br/>\ttime: <b>[' + from + ':' + to + ']</b><br/>';
                                    tooltip += '\t' + point.series.name + ': <b>' + point.y + ' %</b><br/>';
                                    prev = point.point.name;
                                } else {
                                    //Area chart
                                    console.log(point);
                                    from = Math.min(point.point.x, point.point.x2);
                                    to = Math.max(point.point.x, point.point.x2);
                                    var i = point.point.index;
                                    //check if the point is a window end point
                                    if (to == point.point.x) {
                                        console.log('end');
                                        var nextPoint = point.series.data[i+2];
                                        //check if start point of next window equals end point of this window
                                        if ((typeof nextPoint !== "undefined") && (nextPoint.x == to)) {
                                            fromNext = nextPoint.x;
                                            toNext = nextPoint.x2;
                                            //create tooltip
                                            tempTooltip += '<b>Window ' + nextPoint.name + '</b>'
                                            + '<br/>\ttime: <b>[' + fromNext + ':' + toNext + ']</b><br/>';
                                        }
                                    }
                                    
                                    tooltip += '<b>Window ' + point.point.name + '</b>'
                                            + '<br/>\ttime: <b>[' + from + ':' + to + ']</b><br/>';
                                    tooltip += tempTooltip;
                                }
                            });
                            return tooltip; 
                        },
                        shared: true
                    },
                    plotOptions: {
                    area: {
                        fillOpacity: 0.5
                    }
                }
                },
                series: [
                    {
                        yAxis: 1,
                        type: 'area',
                        name: 'odd windows',
                        data: []
                    },
                    {
                        yAxis: 1,
                        type: 'area',
                        name: 'even windows',
                        data: []
                    },
                    {
                        yAxis: 0,
                        type: 'line',
                        name: 'precision',
                        dataLabels: {
                            enabled: true,
                            format: '{y} %'
                        },
                        data: []
                    },
                    {
                        yAxis: 0,
                        type: 'line',
                        name: 'recall',
                        dataLabels: {
                            enabled: true,
                            format: '{y} %'
                        },
                        data: []
                    }],
                yAxis: [
                    {
                        title: {text: 'Percentage (%)'}, 
                        labels: {
                            formatter: function() {
                                return this.value < 0? '' : this.value;
                            }
                        },
                        tickPositions: [-25, 0, 25, 50, 75,100],
                        max: 100, min: -25
                    },
                    {
                        title: {text: ''},
                        labels: {enabled: false},
                        opposite: true,
                        tickPositions: [0,1,2,3,4,5],
                        max: 3
                    }
                ],
                xAxis: {labels: {format: '{value} ms'}}
            };

            $scope.chartW = {
                options: {
                    chart: {type: 'line'},
                    plotOptions: {line: {dataLabels: {enabled: true}}},
                    tooltip: {shared: true, crosshairs: true}
                },
                title: {text: 'Window and Result size (# of triples) + Delay (ms)'},
                series: [],
                yAxis: [
                    {title: {text: '# of triples'}, min: 0},
                    {title: {text: '# of triples'}, min: 0},
                    {
                        title: {text: 'Delay'}, 
                        min: 0, 
                        opposite: true, 
                        labels: {format: "{value} ms"}
                    }
                ]
            };

            $scope.chartBP = {
                options: {
                    chart: {type: 'boxplot'}
                },
                
                title: {
                    text: 'Highcharts box plot styling'
                },
                
                legend: {
                    enabled: true
                },

                xAxis: {
                    categories: ['1', '2'],
                    title: {
                        text: 'Experiment No.'
                    }
                },

                yAxis: [{
                    title: {
                        text: 'Observations'
                    }},
                    {
                    title: {
                        text: 'Observations2'
                    }}],

                plotOptions: {
                    boxplot: {
                        fillColor: '#F0F0E0',
                        lineWidth: 2,
                        medianColor: '#0C5DA5',
                        medianWidth: 3,
                        stemColor: '#A63400',
                        stemDashStyle: 'dot',
                        stemWidth: 1,
                        whiskerColor: '#3D9200',
                        whiskerLength: '20%',
                        whiskerWidth: 3
                    }
                },

                series: [{
                    name: 'Observations',
                    data: [
                        [10, 20, 30, 40, 50],
                        [1, 2, 3, 4, 5]
                    ]
                },
                {
                    name: 'Observations2',
                    data: [
                        [360, 801, 848, 895, 965],
                        [733, 853, 939, 980, 1080]
                        ]
                }]
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
                yAxis: [{// Primary yAxis
                        min: 0,
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
                    }, {// Secondary yAxis
                        gridLineWidth: 0,
                        min: 0,
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

                    }, {// Tertiary yAxis
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
                    {// 4 Axis
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
                var seriesRP = [[], [], [], []];
                var seriesW = [
                    {yAxis: 0, name: 'Result size (actual)', data: []},
                    {yAxis: 0, name: 'Result size (expected)', data: []},
                    {yAxis: 1, name: 'Window size (expected)', data: []},
                    {yAxis: 2, name: 'Delay', data: []}
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
                        var windowCenter = (values[5] + (values[6] - values[5]) / 2) + 5;
                        var windowName = '#' + (index + 1);
                        //Recall and Precision
                        seriesRP[2].push({
                            x: windowCenter, y: (parseInt((values[0] * 100).toPrecision(3))), 
                            x1: values[5], x2: values[6], name: windowName
                        });
                        seriesRP[3].push({
                            x: windowCenter, y: (parseInt((values[1] * 100).toPrecision(3))),  
                            x1: values[5], x2: values[6], name: windowName
                        });
                        
                        //Windows
                        var windowSeries = index % 2;
                        seriesRP[windowSeries].push({
                            x: values[5], x2: values[6], y: 1, name: windowName
                        });
                        seriesRP[windowSeries].push({
                            x: values[6], x2: values[5], y: 1, name: windowName
                        });
                        seriesRP[windowSeries].push(null);

                        seriesW[0].data.push(values[2]);
                        seriesW[1].data.push(values[3]);
                        seriesW[2].data.push(values[4]);
                        seriesW[3].data.push(values[7]);
                    }
                });
                addToChart($scope.chartRP, seriesRP);

                $scope.chartW.series = seriesW;
                $scope.chartW.xAxis = xAxis;
            };
            
            $scope.loadBPData = function ($fileContent) {
            /*
                var xAxis = {
                    categories: [],
                    title: {
                        text: 'Windows',
                        format: 'Window #{value}'
                    }
                };
                var lines = $fileContent.split('\n');
                lines.pop();

                var seriesBP = [
                    {name: 'Precision', yAxis: 0, data: []},
                    {name: 'Recall', yAxis: 1, data: []},
                    {name: 'Delay', yAxis: 2, data: []}
                ];

                angular.forEach(lines, function (line, index) {
                    xAxis.categories.push('#' + (index + 1));
                    
                    var values = line.split(';');
                    
                    
                    var pr = values[0].split(',').map(function (item) {
                        return parseFloat(item);
                    });
                    
                    var re = values[1].split(',').map(function (item) {
                        return parseFloat(item);
                    });
                    
                    var del = values[2].split(',').map(function (item) {
                        return parseFloat(item);
                    });
                    
                    seriesBP[0].data.push(getBoxValues(pr));
                    seriesBP[1].data.push(getBoxValues(re));
                    //seriesBP[2].data.push(getBoxValues(del));
                });
                

                //$scope.chartBP.series = seriesBP;
                //$scope.chartBP.xAxis = xAxis;
                */
            };
            
            $scope.loadPData = function ($fileContent) {
                var lines = $fileContent.split('\n');
                lines = lines.slice(1, lines.length - 1);
                var seriesP = [
                    {name: 'Memory Usage', yAxis: 0, data: [],
                        dataLabels: {
                            enabled: true,
                            formatter: function () {
                                return this.y + ' MB';
                            }},
                        tooltip: {valueSuffix: ' MB'}},
                    {name: 'Memory Usage %', yAxis: 1, data: [],
                        dataLabels: {
                            enabled: true,
                            formatter: function () {
                                return this.y + ' %';
                            }},
                        tooltip: {valueSuffix: ' %'}},
                    {name: 'CPU %', yAxis: 2, data: [],
                        dataLabels: {
                            enabled: true,
                            formatter: function () {
                                return this.y + ' %';
                            }},
                        tooltip: {valueSuffix: ' %'}},
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
                    if (lines.length - 1 == index)
                        last = true;

                    var values = points.split(',').map(function (item) {
                        return parseFloat(item);
                    });
                    //xAxis.categories.push((values[0])+'s');
                    if (values.length > 4) {
                        if (old1 != values[1] && !first) {
                            seriesP[0].data.push([prevtime, old1]);
                            seriesP[0].data.push([values[0], values[1]]);
                        } else if ((old1 != values[1] && first) || last) {
                            seriesP[0].data.push([values[0], values[1]]);
                        }

                        if (old2 != values[3] && !first) {
                            seriesP[1].data.push([prevtime, old2]);
                            seriesP[1].data.push([values[0], values[3]]);
                        } else if ((old2 != values[3] && first) || last) {
                            seriesP[1].data.push([values[0], values[3]]);
                        }

                        if (old3 != values[2] && !first) {
                            seriesP[2].data.push([prevtime, old3]);
                            seriesP[2].data.push([values[0], values[2]]);
                        } else if ((old3 != values[2] && first) || last) {
                            seriesP[2].data.push([values[0], values[2]]);
                        }

                        if (old4 != values[4] && !first) {
                            seriesP[3].data.push([prevtime, old4]);
                            seriesP[3].data.push([values[0], values[4]]);
                        } else if ((old4 != values[4] && first) || last) {
                            seriesP[3].data.push([values[0], values[4]]);
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

    function addToChart(chart, array) {
        angular.forEach(array, function (data, index) {
            chart.series[index].data = data;
        });
    }
    
    function numSort(a,b) { 
        return a - b; 
    } 
    
    function getPercentile(data, percentile) {
        data.sort(numSort);
        var index = (percentile/100) * data.length;
        var result;
        if (Math.floor(index) == index) {
             result = (data[(index-1)] + data[index])/2;
        }
        else {
            result = data[Math.floor(index)];
        }
        return result;
    }
    
    function getBoxValues(data) {
        var boxValues = [];
        //min
        boxValues[0] = Math.min.apply(Math,data);
        //q1
        boxValues[1] = getPercentile(data, 25);
        //median
        boxValues[2] = getPercentile(data, 50);
        //q3
        boxValues[3] = getPercentile(data, 75);
        //max
        boxValues[4] = Math.max.apply(Math,data);
        return boxValues;
    }
    
    function isEven(n) {
        return n % 2 === 0;
    }

})(window.angular, window.Highcharts, Math);