(ns air.bin.ui
  (:require [clojure.java.io :as io]
            [com.biffweb :as biff]
            [ring.util.response :as ring-response]
            [rum.core :as rum]))

(defn css-path []
  (if-some [last-modified (some-> (io/resource "public/css/main.css")
                                  ring-response/resource-data
                                  :last-modified
                                  (.getTime))]
    (str "/css/main.css?t=" last-modified) "/css/main.css"))


(defn base [ctx & body]
  (apply biff/base-html
   (-> ctx
       (merge #:base{:title "airbin"
                     :lang "en-US"
                     :icon "/img/glider.png"
                     :description "paste bin made of air" 
                     :image "https://clojure.org/images/clojure-logo-120b.png"})
       (update :base/head (fn [head] (concat [[:link {:rel "stylesheet" :href (css-path)}]
                                              [:script {:src "https://unpkg.com/htmx.org@1.9.10"}]
                                              [:script {:src "https://unpkg.com/hyperscript.org@0.9.8"}]] head))))
   body))

(defn page [& body]
  (base {} body))

(defn on-error [{:keys [status ex] :as ctx}]
  {:status status
   :headers {"content-type" "text/html"}
   :body (rum/render-static-markup
          (page ctx
           [:h1.text-lg.font-bold
            (if (= status 404) "Page not found." "Something went wrong.")]))})
