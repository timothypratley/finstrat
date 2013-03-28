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
    [:div {:mathbox "performance"}]))

(defpage "/about" []
  (html
    [:div "Todo"]))
