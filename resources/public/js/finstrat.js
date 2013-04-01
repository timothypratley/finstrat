angular.module('finstrat', ['charts'])
    .config(function ($routeProvider, $httpProvider) {
        $routeProvider
	    .when("/about", {templateUrl: "about", controller: AboutCtrl})
	    .when("/chart", {templateUrl: "chart", controller: ChartCtrl})
	    .when("/mathbox", {templateUrl: "mathbox", controller: MathboxCtrl})
	    .otherwise({redirectTo: "/about"});
    });

function AboutCtrl () {}
function ChartCtrl ($scope) {
    $scope.url = "/sim/momentum/F";
}
function MathboxCtrl ($scope) {
    $scope.symbol = "^GSPC";
    $scope.domain = [[0, 2], [0, 2], [0, 2]];
    $scope.n = [11, 11, 1];
    $scope.submit = function () {
        $scope.url = "/sim/momentum/" + $scope.symbol + "/"
            + $scope.domain[0][0] + "/"
            + $scope.domain[0][1] + "/"
            + $scope.n[0] + "/"
            + $scope.domain[1][0] + "/"
            + $scope.domain[1][1] + "/"
            + $scope.n[1] + "/"
            + $scope.domain[2][0] + "/"
            + $scope.domain[2][1] + "/"
            + $scope.n[2];
    };
    $scope.submit();
}

// TODO: charts.js relies on these dependencies - move these except angular
google.load('visualization', '1',
        {'packages':['corechart', 'table', 'annotatedtimeline']});
google.setOnLoadCallback(function() {
    angular.bootstrap(document.body, ['finstrat']);
    ThreeBox.preload(
        ['/html/MathBox.glsl.html'],
        function () { 
            // do stuff with mathbox here
        });
});

