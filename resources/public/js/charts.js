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
	        var mathbox, query, o = {};
	    	$.extend(o, options.general);
	    	$.extend(o, options[attrs.chart]);
	        elem[0].innerHTML = "Loading " + o.title + "...";

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
              n: scope.n,
              domain: scope.domain,
              points: true,
              line: false,
              mesh: true,
              doubleSided: true,
              flipSided: false,
              shaded: true   
            });

	    	query = function(url) {
	    		$log.info("Querying " + url);
                $http.get(url)
                    .success(function (data) {
                        $log.info("got some data!");
                        mathbox.animate("#surface", {
                            expression: function(x, y, i, j) { return data[i][j]; }
                        });
                    })
                    .error(function (data, status) {
                        $log.error(status);
                        //mathbox.addErrorFromQueryResponse(elem[0], data);
                    });
	        }
            scope.$watch("url", query, true);
	    };
	});

