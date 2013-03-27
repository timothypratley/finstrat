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
	});

