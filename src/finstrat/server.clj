(ns finstrat.server
  (:require [compojure.core :refer :all]
            [compojure.route :refer :all]
            [noir.util.middleware :refer :all]
            [finstrat.pages :refer :all]
            [finstrat.services :refer :all]))


(defn simt [params]
  (println (str params))
  (str "ok! " params))

(defn simb [x]
  "ok!")

(def app-routes
  [(GET "/" [] (home))
   (GET "/chart" [] (chart))
   (GET "/about" [] (about))
   (GET "/simt/:x/:y/:z" {params :params} (simt params))
   (GET "/simt/*" {params :params} (simt params))
   (GET ["/simb/:x" :x #"[0-9]+"] [x] (simb x))
   (GET "/sim/:screens/:symbols/:astart/:aend/:acount/:bstart/:bend/:bcount/:cstart/:cend/:ccount"
        [screens symbols astart aend acount bstart bend bcount cstart cend ccount]
        (sim screens symbols astart aend acount bstart bend bcount cstart cend ccount))
   (GET "/sim/:screens/:symbols/:a/:b/:c"
        [screens symbols a b c]
        (sim screens symbols a b c))
   (resources "/")
   (not-found "Not found")])

(def handler
  (app-handler app-routes))
