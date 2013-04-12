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
        [:li (link-to "/#/histogram" "Histogram")]
        [:li.divider-vertical]
        [:li (link-to "/#/mathbox" "Mathbox")]
        [:li.divider-vertical]]]]

      [:div#content.ng-view "Loading..."]

     ;TODO: some of these dependencies only belong to certain sub-pages
     ;can they be moved there?
     (include-js "//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js")
     (include-js "//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/js/bootstrap.min.js")
     (include-js "//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min.js")
     (include-js "//cdnjs.cloudflare.com/ajax/libs/d3/3.0.8/d3.min.js")
     (include-js "https://www.google.com/jsapi")
     (include-js "/js/MathBox-bundle.min.js")
     (include-js "/js/charts.js")
     (include-js "/js/finstrat.js")]))

(defpage "/chart" []
  (html
    ;[:div {:chart "performance"}]
    [:div {:chart "dist"}]))

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
           ; workaround to avoid mathbox overlay for labels position issue
           :style "width:600px;height:600px;position:relative"}]))

(defpage "/about" []
  (html
    [:div "Todo"]))

(defpage "/histogram" []
  (html
    [:style "
.bar rect {
  fill: steelblue;
  shape-rendering: crispEdges;
}

.bar text {
  fill: #fff;
}

.axis path, .axis line {
  fill: none;
  stroke: #000;
  shape-rendering: crispEdges;
}
"]
    [:div {:histogram "general"
           :style "font: 10px sans-serif"}]))
                  

