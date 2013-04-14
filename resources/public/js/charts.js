angular.module('charts', [])
	.value('options', {
		general: {
			width: 1000,
			height: 500
		},
        dist: {
            type: "AreaChart",
            title: "Distribution",
            hAxis: {title: 'X'},
            vAxis: {title: 'Y'}
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
                        //// TODO: this is a workaround,
                        //why is o not working???
                        o = {width: 1000, height: 500};
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
//TODO: avoid riddiculous indentation 
    .directive('histogram', function(options, $log, $http) {
	    return function(scope, elem, attrs) {
            // Generate an Irwin–Hall distribution of 10 random variables.
            var values = d3.range(1000).map(d3.random.irwinHall(10));

            // A formatter for counts.
            var formatCount = d3.format(",.0f");

            var margin = {top: 10, right: 30, bottom: 30, left: 30},
                width = 960 - margin.left - margin.right,
                height = 500 - margin.top - margin.bottom;

            var x = d3.scale.linear()
                .domain([0, 1])
                .range([0, width]);

            // Generate a histogram using twenty uniformly-spaced bins.
            var data = d3.layout.histogram()
                .bins(x.ticks(20))
                (values);

            var y = d3.scale.linear()
                .domain([0, d3.max(data, function(d) { return d.y; })])
                .range([height, 0]);

            var xAxis = d3.svg.axis()
                .scale(x)
                .orient("bottom");

            var svg = d3.select(elem[0]).append("svg")
                .attr("width", width + margin.left + margin.right)
                .attr("height", height + margin.top + margin.bottom)
                .append("g")
                .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

            var bar = svg.selectAll(".bar")
                .data(data)
                .enter().append("g")
                .attr("class", "bar")
                .attr("transform", function(d) { return "translate(" + x(d.x) + "," + y(d.y) + ")"; });

            bar.append("rect")
                .attr("x", 1)
                .attr("width", x(data[0].dx) - 1)
                .attr("height", function(d) { return height - y(d.y); });

            bar.append("text")
                .attr("dy", ".75em")
                .attr("y", 6)
                .attr("x", x(data[0].dx) / 2)
                .attr("text-anchor", "middle")
                .text(function(d) { return formatCount(d.y); });

            svg.append("g")
                .attr("class", "x axis")
                .attr("transform", "translate(0," + height + ")")
                .call(xAxis);
        }
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
                        // find min/max across z and t dimensions
                        mathbox.viewport({
                          range: [[-3, 3], [-30, 30], [-3, 3]]
                        });
                        // TODO: why is Y up not Z?? service thinks Z is up lol
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

