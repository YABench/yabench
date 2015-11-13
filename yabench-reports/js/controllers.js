(function (angular, Highcharts, Math) {
	var module = angular.module('yabench-controllers', ['highcharts-ng']);

	module.controller('IndexCtrl', ['$scope', '$document', '$http',
			function ($scope, $document, $http) {
				$scope.chartRP = {
					title : {
						text : 'Precision/Recall'
					},
					options : {
						tooltip : {
							formatter : function () {
								var tooltip = '';
								var tempTooltip = '';
								var prev;
								angular.forEach(this.points, function (point) {
									var from,
									to,
									fromNext,
									toNext;
									if (point.point.x1 || point.point.x1 === 0) {
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
											var nextPoint = point.series.data[i + 2];
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
							shared : true
						},
						plotOptions : {
							area : {
								fillOpacity : 0.5
							}
						}
					},
					series : [{
							yAxis : 1,
							type : 'area',
							name : 'odd windows',
							data : []
						}, {
							yAxis : 1,
							type : 'area',
							name : 'even windows',
							data : []
						}, {
							yAxis : 0,
							type : 'line',
							name : 'precision',
							dataLabels : {
								enabled : true,
								format : '{y} %'
							},
							data : []
						}, {
							yAxis : 0,
							type : 'line',
							name : 'recall',
							dataLabels : {
								enabled : true,
								format : '{y} %'
							},
							data : []
						}
					],
					yAxis : [{
							title : {
								text : 'Percentage (%)'
							},
							labels : {
								formatter : function () {
									return this.value < 0 ? '' : this.value;
								}
							},
							tickPositions : [-25, 0, 25, 50, 75, 100],
							max : 100,
							min : -25
						}, {
							title : {
								text : ''
							},
							labels : {
								enabled : false
							},
							opposite : true,
							tickPositions : [0, 1, 2, 3, 4, 5],
							max : 3
						}
					],
					xAxis : {
						labels : {
							format : '{value} ms'
						}
					}
				};

				$scope.chartW = {
					options : {
						chart : {
							type : 'line'
						},
						exporting : {
							sourceWidth : 650
						},
						plotOptions : {
							line : {
								dataLabels : {
									enabled : true
								}
							}
						},
						tooltip : {
							shared : true,
							crosshairs : true
						}
					},
					title : {
						text : 'Window and Result size (# of triples) + Delay (ms)'
					},
					series : [],
					yAxis : [{
							title : {
								text : '# of triples'
							},
							min : 0,
							showEmpty : false
						}, {
							title : {
								text : '# of triples'
							},
							min : 0,
							showEmpty : false
						}, {
							title : {
								text : 'Delay (ms)'
							},
							min : 0,
							opposite : true,
							showEmpty : false,
							labels : {
								format : "{value}"
							}
						}
					]
				};

				$scope.chartBPPR = {
					options : {
						chart : {
							type : 'boxplot',
							zoomType : 'y'
						},
						exporting : {
							sourceWidth : 700,
							//sourceHeight: 5000,
							//scale: 2
						},
						plotOptions : {
							boxplot : {
								//fillColor: '#F0F0E0', //is done later for individual coloring
								lineWidth : 2,
								//medianColor: '#0C5DA5',
								medianWidth : 1,
								whiskerLength : '20%',
								whiskerWidth : 1
							}
						},
						tooltip : {
							crosshairs : true
						}
					},

					title : {
						text : 'Boxplots of Precision and Recall'
					},

					legend : {
						enabled : true
					},

					yAxis : [{
							title : {
								text : 'Percentage (%)'
							},
							labels : {
								formatter : function () {
									return this.value * 100;
								}
							},
							min : 0,
							max : 1,
							showEmpty : false,
						}
					],

					series : []
				};

				$scope.chartBPD = {
					options : {
						chart : {
							type : 'boxplot',
							zoomType : 'y'
						},
						plotOptions : {
							boxplot : {
								//fillColor: '#F0F0E0',
								lineWidth : 1,
								//medianColor: '#0C5DA5',
								medianWidth : 1,
								whiskerLength : '20%',
								whiskerWidth : 1
							}
						},
						tooltip : {
							crosshairs : true
						}
					},

					title : {
						text : 'Boxplots of Delay'
					},

					legend : {
						enabled : true
					},

					yAxis : [{
							title : {
								text : 'Delay (ms)'
							},
							labels : {
								formatter : function () {
									return this.value;
								}
							},
							showEmpty : false,
						}
					],

					series : []
				};

				$scope.chartP = {
					options : {
						chart : {
							type : 'line'
						},
						plotOptions : {
							line : {
								dataLabels : {
									enabled : true
								}
							}
						},
						tooltip : {
							shared : true,
							crosshairs : true
						}
					},
					title : {
						text : 'Performance metrics'
					},
					series : [],
					xAxis : {
						labels : {
							format : '{value} s'
						}
					},
					yAxis : [{ // Primary yAxis
							min : 0,
							labels : {
								format : '{value}',
								style : {
									color : Highcharts.getOptions().colors[0]
								}
							},
							title : {
								text : 'Memory Usage (MB)',
								style : {
									color : Highcharts.getOptions().colors[0]
								}
							}
						}
						/*, {// Secondary yAxis
						gridLineWidth: 0,
						min: 0,
						title: {
						text: 'Memory Usage (%)',
						style: {
						color: Highcharts.getOptions().colors[1]
						}
						},
						labels: {
						format: '{value}',
						style: {
						color: Highcharts.getOptions().colors[1]
						}
						}

						}, {// Tertiary yAxis
						gridLineWidth: 0,
						title: {
						text: 'CPU Usage (%)',
						style: {
						color: Highcharts.getOptions().colors[2]
						}
						},
						labels: {
						format: '{value}',
						style: {
						color: Highcharts.getOptions().colors[2]
						}
						},
						opposite: true
						},{// 4 Axis
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
						}*/
					]
				};

				$scope.loadData = function ($fileContent) {
					var lines = $fileContent.split('\n');
					var seriesRP = [[], [], [], []];
					var seriesW = [{
							yAxis : 0,
							name : 'Result size (actual)',
							data : []
						}, {
							yAxis : 0,
							name : 'Result size (expected)',
							data : []
						}, {
							yAxis : 1,
							name : 'Window size (expected)',
							data : []
						}, {
							yAxis : 2,
							name : 'Delay',
							data : [],
							tooltip : {
								valueSuffix : ' ms'
							}
						}
					];

					var xAxis = {
						categories : [],
						title : {
							text : 'Windows',
							format : 'Window #{value}'
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
								x : windowCenter,
								y : (parseInt((values[0] * 100).toPrecision(3))),
								x1 : values[5],
								x2 : values[6],
								name : windowName
							});
							seriesRP[3].push({
								x : windowCenter,
								y : (parseInt((values[1] * 100).toPrecision(3))),
								x1 : values[5],
								x2 : values[6],
								name : windowName
							});

							//Windows
							var windowSeries = index % 2;
							seriesRP[windowSeries].push({
								x : values[5],
								x2 : values[6],
								y : 1,
								name : windowName
							});
							seriesRP[windowSeries].push({
								x : values[6],
								x2 : values[5],
								y : 1,
								name : windowName
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

				$scope.loadBPE2Data = function ($fileContent) {
					var xAxis = xAxisPR;
					var lines = $fileContent.split('\n');
					lines.pop();

					//get engineName which is in the first line of the file
					var engineName = lines.shift();

					//series for engine 2
					var seriesBPPR2 = [{
							name : 'Precision' + getEngineNameForSeriesLabel(engineName),
							yAxis : 0,
							data : [],
							pointPlacement : 0.1,
						}, {
							name : 'Recall' + getEngineNameForSeriesLabel(engineName),
							yAxis : 0,
							data : [],
							pointPlacement : 0.1,
						},
					];

					var seriesBPD2 = [{
							name : 'Delay' + getEngineNameForSeriesLabel(engineName),
							yAxis : 0,
							data : [],
							tooltip : {
								valueSuffix : ' ms'
							}
						}
					];

					//coloring
					seriesBPPR2[0].fillColor = '#90ed7d';
					seriesBPPR2[0].medianColor = '#73bd64';
					seriesBPPR2[0].color = '#90ed7d';
					seriesBPPR2[1].fillColor = '#893BFF';
					seriesBPPR2[1].medianColor = '#6d2fcc';
					seriesBPPR2[1].color = '#893BFF';
					seriesBPD2[0].fillColor = '#90ed7d';
					seriesBPD2[0].medianColor = '#73bd64';
					seriesBPD2[0].color = '#90ed7d';

					angular.forEach(lines, function (line, index) {
						xAxis.categories.push('#' + (index + 1));
						var values = line.split(';');

						var pr = values[0].split(',').map(function (item) {
								return parseFloat(parseFloat(item).toPrecision(3));
							});

						var re = values[1].split(',').map(function (item) {
								return parseFloat(parseFloat(item).toPrecision(3));
							});

						var del = values[2].split(',').map(function (item) {
								return parseFloat(item);
							});

						seriesBPPR2[0].data.push(getBoxValues(pr));
						seriesBPPR2[1].data.push(getBoxValues(re));
						seriesBPD2[0].data.push(getBoxValues(del));
					});

					$scope.chartBPPR.series.push(seriesBPPR2[0]);
					$scope.chartBPPR.series.push(seriesBPPR2[1]);
					$scope.chartBPD.series.push(seriesBPD2[0]);

					//move Engine 1 Boxplots to the left a bit
					$scope.chartBPPR.series[0].pointPlacement = -0.1;
					$scope.chartBPPR.series[1].pointPlacement = -0.1;

					$scope.chartBPPR.xAxis = xAxis;
					$scope.chartBPD.xAxis = xAxis;

				};

				$scope.loadBPE1Data = function ($fileContent) {

					var xAxis = xAxisPR;
					var lines = $fileContent.split('\n');
					lines.pop();

					//get engineName which is in the first line of the file
					var engineName = lines.shift();

					//series for engine 1
					var seriesBPPR = [{
							name : 'Precision' + getEngineNameForSeriesLabel(engineName),
							yAxis : 0,
							data : []
						}, {
							name : 'Recall' + getEngineNameForSeriesLabel(engineName),
							yAxis : 0,
							data : []
						},
					];

					var seriesBPD = [{
							name : 'Delay' + getEngineNameForSeriesLabel(engineName),
							yAxis : 0,
							data : [],
							tooltip : {
								valueSuffix : ' ms'
							}
						}
					];

					//coloring
					//for pattern use
					//seriesBPPR[0].fillColor = 'url(#highcharts-default-pattern-5)';
					seriesBPPR[0].fillColor = '#7cb5ec';
					seriesBPPR[0].medianColor = '#6390bc';
					seriesBPPR[0].color = '#7cb5ec';
					seriesBPPR[1].fillColor = '#FF9966';
					seriesBPPR[1].medianColor = '#cc7a51';
					seriesBPPR[1].color = '#FF9966';
					seriesBPD[0].fillColor = '#7cb5ec';
					seriesBPD[0].medianColor = '#6390bc';
					seriesBPD[0].color = '#7cb5ec';

					angular.forEach(lines, function (line, index) {
						xAxis.categories.push('#' + (index + 1));

						var values = line.split(';');

						var pr = values[0].split(',').map(function (item) {
								return parseFloat(parseFloat(item).toPrecision(3));
							});

						var re = values[1].split(',').map(function (item) {
								return parseFloat(parseFloat(item).toPrecision(3));
							});

						var del = values[2].split(',').map(function (item) {
								return parseFloat(item);
							});

						seriesBPPR[0].data.push(getBoxValues(pr));
						seriesBPPR[1].data.push(getBoxValues(re));
						seriesBPD[0].data.push(getBoxValues(del));
					});

					$scope.chartBPPR.series = seriesBPPR;
					$scope.chartBPD.series = seriesBPD;

					$scope.chartBPPR.xAxis = xAxis;
					$scope.chartBPD.xAxis = xAxis;

				};

				$scope.loadPData = function ($fileContent) {
					var lines = $fileContent.split('\n');

					var engineName = lines.shift();

					lines = lines.slice(1, lines.length - 1);
					var seriesP = getPerformanceSeries(engineName);
					var xAxis = xAxisP;

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

						if (values.length > 4) {
							if (old1 != values[1] && !first) {
								seriesP[0].data.push([prevtime, Math.round(old1)]);
								seriesP[0].data.push([values[0], Math.round(values[1])]);
							} else if ((old1 != values[1] && first) || last) {
								seriesP[0].data.push([values[0], Math.round(values[1])]);
							}
							/*
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
							}*/

							old1 = values[1];
							old2 = values[3];
							old3 = values[2];
							old4 = values[4];

							prevtime = values[0];
							first = false;

						}
					});

					seriesP[0].color = '#7cb5ec';
					$scope.chartP.series = seriesP;
					$scope.chartP.xAxis = xAxis;
				};

				$scope.loadP2Data = function ($fileContent) {

					var lines = $fileContent.split('\n');

					var engineName = lines.shift();

					lines = lines.slice(1, lines.length - 1);
					var seriesP2 = getPerformanceSeries(engineName);
					var xAxis = xAxisP;

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
						if (values.length > 4) {
							if (old1 != values[1] && !first) {
								seriesP2[0].data.push([prevtime, Math.round(old1)]);
								seriesP2[0].data.push([values[0], Math.round(values[1])]);
							} else if ((old1 != values[1] && first) || last) {
								seriesP2[0].data.push([values[0], Math.round(values[1])]);
							}
							/*
							if (old2 != values[3] && !first) {
							seriesP2[1].data.push([prevtime, old2]);
							seriesP2[1].data.push([values[0], values[3]]);
							} else if ((old2 != values[3] && first) || last) {
							seriesP2[1].data.push([values[0], values[3]]);
							}

							if (old3 != values[2] && !first) {
							seriesP2[2].data.push([prevtime, old3]);
							seriesP2[2].data.push([values[0], values[2]]);
							} else if ((old3 != values[2] && first) || last) {
							seriesP2[2].data.push([values[0], values[2]]);
							}

							if (old4 != values[4] && !first) {
							seriesP2[3].data.push([prevtime, old4]);
							seriesP2[3].data.push([values[0], values[4]]);
							} else if ((old4 != values[4] && first) || last) {
							seriesP2[3].data.push([values[0], values[4]]);
							}*/

							old1 = values[1];
							old2 = values[3];
							old3 = values[2];
							old4 = values[4];

							prevtime = values[0];
							first = false;

						}
					});

					seriesP2[0].color = '#434348';
					$scope.chartP.series.push(seriesP2[0]);
					//$scope.chartP.series.push(seriesP2[1]);
					//$scope.chartP.series.push(seriesP2[2]);
					//$scope.chartP.series.push(seriesP2[3]);

				};

				$document.ready(function () {
					console.log('here again');
					//load BPE1
					$http.get('examples/cqels/BOXPLOTS_medium').
					success(function (data, status, headers, config) {
						$scope.loadBPE1Data(data);
						//load BPE2
						$http.get('examples/c-sparql/BOXPLOTS_big').
						success(function (data, status, headers, config) {
							$scope.loadBPE2Data(data);

							//load PE1
							$http.get('examples/cqels/P_big_agg').
							success(function (data, status, headers, config) {
								$scope.loadPData(data);

								//load PE2
								$http.get('examples/c-sparql/P_big_agg').
								success(function (data, status, headers, config) {
									$scope.loadP2Data(data);

									//load single result
									$http.get('examples/c-sparql/ORACLE_big1').
									success(function (data, status, headers, config) {
										$scope.loadData(data);
									}).
									error(function (data, status, headers, config) {
										console.log('error');
									});

								}).
								error(function (data, status, headers, config) {
									console.log('error');
								});
							}).
							error(function (data, status, headers, config) {
								console.log('error');
							});

						}).
						error(function (data, status, headers, config) {
							console.log('error');
						});
					}).
					error(function (data, status, headers, config) {
						console.log('error');
					});

				});

			}
		]);

	function addToChart(chart, array) {
		angular.forEach(array, function (data, index) {
			chart.series[index].data = data;
		});
	}

	function numSort(a, b) {
		return a - b;
	}

	function getPercentile(data, percentile) {
		data.sort(numSort);
		var index = (percentile / 100) * data.length;
		var result;
		if (Math.floor(index) == index) {
			result = (data[(index - 1)] + data[index]) / 2;
		} else {
			result = data[Math.floor(index)];
		}
		return result;
	}

	function getBoxValues(data) {
		var boxValues = [];
		//min
		boxValues[0] = Math.min.apply(Math, data);
		//q1
		boxValues[1] = getPercentile(data, 25);
		//median
		boxValues[2] = getPercentile(data, 50);
		//q3
		boxValues[3] = getPercentile(data, 75);
		//max
		boxValues[4] = Math.max.apply(Math, data);
		return boxValues;
	}

	function isEven(n) {
		return n % 2 === 0;
	}

	function getEngineNameForSeriesLabel(engineName) {
		engineName = engineName.trim();
		if (engineName == 'unknown') {
			return '';
		} else {
			return ' (' + engineName + ')';
		}
	}

	function getPerformanceSeries(engineName) {
		var ret = [{
				name : 'Memory Usage (MB)' + getEngineNameForSeriesLabel(engineName),
				yAxis : 0,
				data : [],
				dataLabels : {
					enabled : true,
					formatter : function () {
						return this.y + ' MB';
					}
				},
				tooltip : {
					valueSuffix : ' MB'
				}
			},
			/*{name: 'Memory Usage (%)' + getEngineNameForSeriesLabel(engineName), yAxis: 1, data: [],
			dataLabels: {
			enabled: true,
			formatter: function () {
			return this.y + ' %';
			}},
			tooltip: {valueSuffix: ' %'}},{name: 'CPU (%)' + getEngineNameForSeriesLabel(engineName), yAxis: 2, data: [],
			dataLabels: {
			enabled: true,
			formatter: function () {
			return this.y + ' %';
			}},
			tooltip: {valueSuffix: ' %'}},{name: 'Threads' + getEngineNameForSeriesLabel(engineName), yAxis: 3, data: []}*/
		];

		return ret
	}

	var xAxisP = {
		title : {
			text : 'Time'
		}
	};

	var xAxisPR = {
		categories : [],
		title : {
			text : 'Windows',
			format : 'Window #{value}'
		}
	};

})(window.angular, window.Highcharts, Math);
