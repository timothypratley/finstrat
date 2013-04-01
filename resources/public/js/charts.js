angular.module('charts', [])
	.value('options', {
		general: {
			width: 1000,
			height: 500
		},
		performance: {
			type: "AnnotatedTimeLine",
			title: "Performance",
			vAxis: {title: "Value", minValue: 0},
		    hAxis: {title: "Time"},
            displayAnnotations: true,
			areaOpacity: 0.0
		}
    })
    .directive('chart', function(options, $log, $http) {
	    return function(scope, elem, attrs) {
	        var chart, query, o = {};
	    	$.extend(o, options.general);
	    	$.extend(o, options[attrs.chart]);
	        elem[0].innerHTML = "Loading " + o.title + "...";
	        chart = new google.visualization[o.type](elem[0]);
	    	query = function(url) {
	    		$log.info("Querying " + url);
                $http.get(url)
                    .success(function (data) {
                        chart.draw(
                            google.visualization.arrayToDataTable(data),
                            o);
                    })
                    .error(function (data, status) {
                        $log.error(status);
                        google.visualization.errors
                            .addErrorFromQueryResponse(
                                elem[0], data);
                    });
	        }
            scope.$watch("url", query, true);
	    };
	})
    .directive('mathbox', function(options, $log, $http) {
	    return function(scope, elem, attrs) {
	        var mathbox, o = {};
	    	$.extend(o, options.general);
	    	$.extend(o, options[attrs.mathbox]);
	        //elem[0].innerHTML = "Loading " + o.title + "...";

            mathbox = mathBox(elem[0], {
              cameraControls: true,
              cursor:         true,
              controlClass:   ThreeBox.OrbitControls,
              elementResize:  true,
              fullscreen:     true,
              screenshot:     true,
              stats:          false,
              scale:          1,
            })
            .start()
            .viewport({
              type: 'cartesian',
              range: [[-3, 3], [-3, 3], [-3, 3]],
              scale: [1, 1, 1],
            })
            .camera({
              orbit: 3.5,
              phi: Math.PI/3,
              theta: 0.3,
            })
            .transition(300)
            .axis({
              id: 'a',
              axis: 0,
              color: 0xa0a0a0,
              ticks: 5,
              lineWidth: 2,
              size: .05,
              labels: true,
            })
            .axis({
              id: 'b',
              axis: 1,
              color: 0xa0a0a0,
              ticks: 5,
              lineWidth: 2,
              size: .05,
              zero: false,
              labels: true,
            })
            .axis({
              id: 'c',
              axis: 2,
              color: 0xa0a0a0,
              ticks: 5,
              lineWidth: 2,
              size: .05,
              zero: false,
              labels: true,
            })
            .grid({
              axis: [0, 2],
              color: 0xc0c0c0,
              lineWidth: 1,
            })
            .surface({
              id: "surface",
              domain: [[scope.domain[0][0], scope.domain[0][1]],
                       [scope.domain[1][0], scope.domain[1][1]]],
              n: [scope.n[0], scope.n[1]],
              points: true,
              line: false,
              mesh: true,
              doubleSided: true,
              flipSided: false,
              shaded: true   
            });

	    	query = function(url) {
                var domain = [[scope.domain[0][0], scope.domain[0][1]],
                              [scope.domain[1][0], scope.domain[1][1]]],
                    n = [scope.n[0], scope.n[1]];
	    		$log.info("Querying " + url);
                $http.get(url)
                    .success(function (data) {
                        $log.info("got some data!");
        // TODO: make the viewport adjust to data size
        // find min/max across 4 dimensions o_O
            mathbox.viewport({
              range: [[-3, 3], [-30, 30], [-3, 3]]
            });
            // TODO: why is Y up not Z?? service thinks Z is up lololol
                        var s = mathbox.get("#surface");
                        if (s.n[0] != n[0] || s.n[1] != n[1]) {
                            // TODO: use a single object as the source
                            // for properties in original, animation
                            // and recreation.
                            mathbox.remove("#surface");
                            // recreation is because Mathbox
                            // surface size does not change when you change n
                            mathbox.surface({
                                id: "surface",
                                domain: domain,
                                n: n,
                                points: true,
                                line: false,
                                mesh: true,
                                doubleSided: true,
                                flipSided: false,
                                shaded: true,
                                expression: function(x, y, i, j) {
                                    var k = 0; // time % n[2]
                                    return data[i][j][k];
                                }
                            });
                        } else {
                            mathbox.animate("#surface", {
                                // arrays are objects, not values
                                // so we need to recapture the values
                                domain: domain,
                                n: n,
                                expression: function(x, y, i, j) {
                                    var k = 0; // time % n[2]
                                    return data[i][j][k];
                                }
                            });
                        }
                    })
                    .error(function (data, status) {
                        $log.error(status);
                        //mathbox.addErrorFromQueryResponse(elem[0], data);
                    });
	        }
            scope.$watch("url", query, true);
	    };
	});

