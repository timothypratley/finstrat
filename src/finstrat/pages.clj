(ns finstrat.pages
  (:require [noir.session :as session]
            [noir.response :as response])
  (:use [noir.core]
        [hiccup.core]
        [hiccup.element]
        [hiccup.form]
        [hiccup.page]))

(defpage "/" []
  (html5
    [:head
     ; When run on an intranet, IE defaults to compatibility
     ; which does not work for Google Visualization library
     [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
     [:title "finstrat"]
     [:link {:rel "icon"
             :href "img/favicon.ico"
             :type "image/x-icon"}]
     [:link {:rel "shortcut"
             :href "img/favicon.ico"
             :type "image/x-icon"}]
     (include-css "//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-combined.min.css")
     (include-css "/css/finstrat.css")]
    [:body 
     [:header.navbar
      [:div.navbar-inner
       [:a.brand {:href "#/"} [:strong "S"]]
       [:ul.nav
        [:li.divider-vertical]
        [:li (link-to "/#/" "Home")]
        [:li.divider-vertical]
        [:li (link-to "/#/chart" "Chart")]
        [:li.divider-vertical]
        [:li (link-to "/#/mathbox" "Mathbox")]
        [:li.divider-vertical]]]]

      [:div#content.ng-view "Loading..."]
     (include-js "//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js")
     (include-js "//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/js/bootstrap.min.js")
     (include-js "//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min.js")
     (include-js "https://www.google.com/jsapi")
     (include-js "/js/MathBox-bundle.min.js")
     (include-js "/js/charts.js")
     (include-js "/js/finstrat.js")]))

(defpage "/chart" []
  (html
    [:div {:chart "performance"}]))

(defpage "/mathbox" []
  (html
    [:form {:ng-submit "submit()"
            :novalidate true
            ;gives the container size so it will envelope floats
            :style "overflow:hidden;width:100%"}
     [:label "symbol" (text-field {:ng-model "symbol"} "symbol")]
     ;[:fieldset [:legend "Tolerance"]
      [:label "ts" [:input {:ng-model "domain[0][0]" :type "number"}]]
      [:label "te" [:input {:ng-model "domain[0][1]" :type "number"}]]
      [:label "tc" [:input {:ng-model "n[0]" :type "number"}]]
     ;[:fieldset [:legend "Days"]
      [:label "ds" [:input {:ng-model "domain[1][0]" :type "number"}]]
      [:label "de" [:input {:ng-model "domain[1][1]" :type "number"}]]
      [:label "dc" [:input {:ng-model "n[1]" :type "number"}]]
     (submit-button "Plot")]
    [:div {:mathbox "performance"
           ;; workaround to avoid mathbox overlay for labels position issue
           :style "width:600px;height:600px;position:relative"}]))

(defpage "/about" []
  (html
    [:div "Todo"]))

(defpage "/test" []
  (html
    [:body
     [:h1 "heading"]
     [:div#e {:style "width:400;height:400;position:relative"}]
     (include-js "/js/MathBox-bundle.min.js")
     [:script "
DomReady.ready(function() {
  ThreeBox.preload([
    '/html/MathBox.glsl.html',
  ], function () {

var element = document.getElementById('e');
var mathbox = mathBox(element, {
              cameraControls: true,
              cursor:         true,
              controlClass:   ThreeBox.OrbitControls,
              elementResize:  true,
              fullscreen:     true,
              screenshot:     true,
              stats:          false,
              scale:          1,
}).start()
.viewport({
  type: 'cartesian',
  range: [[-3, 3], [-3, 3]],
  scale: [1, 1],
})
// Apply automatic 300ms fade in/out
.transition(300)

// Add XYZ axes
.axis({
  id: 'x-axis',
  axis: 0,
  color: 0xa0a0a0,
  ticks: 5,
  lineWidth: 2,
  size: .05,
  labels: true,
})
.axis({
  id: 'y-axis',
  axis: 1,
  color: 0xa0a0a0,
  ticks: 5,
  lineWidth: 2,
  size: .05,
  labels: true,
  zero: false,
})
.axis({
  id: 'z-axis',
  axis: 2,
  color: 0xa0a0a0,
  ticks: 5,
  lineWidth: 2,
  size: .05,
  zero: false,
  labels: true,
})
// Grid
.grid({
  id: 'my-grid',
  axis: [0, 2],
  color: 0xc0c0c0,
  lineWidth: 1,
})

// Curve, explicit function
.curve({
  id: 'my-curve',
  domain: [-3, 3],
  expression: function (x) { return Math.cos(x); },
  line: true,
  points: true,
  lineWidth: 2,
})

// Curve, parametric function
.curve({
  id: 'my-circle',
  domain: [-3, 3],
  expression: function (x) { return [Math.sin(x)*2, Math.cos(x)*2]; },
  line: true,
  points: true,
  lineWidth: 2,
});
              

  });
});
      "]]))

