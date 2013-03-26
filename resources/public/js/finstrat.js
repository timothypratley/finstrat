angular.module('finstrat', ['charts'])
    .config(function ($routeProvider, $httpProvider) {
        $routeProvider
	    .when("/about", {templateUrl: "/partial/about", controller: AboutCtrl})
	    .when("/chart", {templateUrl: "/partial/chart", controller: ChartCtrl})
	    .otherwise({redirectTo: "/about"});
    });

// note that charts relies on this, can I put it in there?
google.load('visualization', '1',
        {'packages':['corechart', 'table', 'annotatedtimeline']});
google.setOnLoadCallback(function() {
   angular.bootstrap(document.body, ['finstrat']);
});

