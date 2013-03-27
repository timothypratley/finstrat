angular.module('finstrat', ['charts'])
    .config(function ($routeProvider, $httpProvider) {
        $routeProvider
	    .when("/about", {templateUrl: "about", controller: AboutCtrl})
	    .when("/chart", {templateUrl: "chart", controller: ChartCtrl})
	    .otherwise({redirectTo: "/about"});
    });

function AboutCtrl () {}
function ChartCtrl ($scope) {
    $scope.url = "/json/foo/F";
}

// note that charts relies on this, can I put it in there?
google.load('visualization', '1',
        {'packages':['corechart', 'table', 'annotatedtimeline']});
google.setOnLoadCallback(function() {
   angular.bootstrap(document.body, ['finstrat']);
});

