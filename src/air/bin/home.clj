(ns air.bin.home
  (:require [com.biffweb :as biff :refer [q, lookup]]
            [air.bin.ui :as ui]
            [xtdb.api :as xt]))

(defn codearea [{ :keys [:params :text :readonly] :as ctx }]
 [:textarea#codearea
      {:name "text" :spellcheck "false"
       :placeholder "AirBin" :readonly readonly
       :class '[flex flex-grow border 
                block border-gray-300 resize-none
                rounded focus:border-teal-600
                font-mono focus:ring-teal-600]} text])

(defn option [n dis selected]
  [:option {:value n :selected (= selected n) } dis])

(defn select-lang [{:keys [disabled selected] :or {disabled false selected "txt"}}] 
    [:select {:id "cars" :name "lang" 
              :disabled disabled
              :class "block mr-2 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"}
        (option "txt" "Plain text" selected)
        (option "clj" "Clojure" selected)])

(defn pastebin [{:keys [params]}]
  [:div {:class '[flex flex-grow w-full h-full]}
  (biff/form
   {:id "code" :action "/api/upload" :class "flex flex-auto" :hidden {:on-error "/" }}
    (codearea { :params params  })
    [:div { :class "absolute bottom-2 right-2  flex items-center" } 
      (select-lang {})
      [:button { :type "submit" 
                 :class '[inline-flex border bg-teal-600 hover:bg-teal-600 text-white
                          rounded py-2 px-4 border-transparent shadow-sm items-center]} 
       "Submit"]]
   (when-some [error (:error params)]
     [:<> [:.h-1]
      [:.text-sm.text-red-600
         "Error"]]))])

(defn home-page [ctx]
  (ui/base ctx (pastebin ctx)))

(defn content [{:keys [path-params biff/db] :as ctx}]
  (let [tid  (:id path-params)
        {:keys [post/text post/lang] :as doc} (lookup db :xt/id (parse-uuid tid))]
    (if (some? doc)
        (ui/page 
          [:div {:class '[flex flex-grow w-full h-full]}
            (codearea {:text text :readonly true })
            [:div { :class "absolute bottom-2 right-2  flex items-center" } 
              (select-lang { :disabled true :selected lang })
              [:button.cpy { :class '[inline-flex border bg-teal-600 hover:bg-teal-600 text-white
                                  rounded py-2 px-4 mr-2 border-transparent shadow-sm items-center] } "Copy"]
              [:button { :class '[inline-flex border bg-teal-600 hover:bg-teal-600 text-white
                                  rounded py-2 px-4 border-transparent shadow-sm items-center] 
                                  :onclick "navigator.clipboard.writeText(window.location); alert(`copied`)" } "Share"]]])
      {:status 303 :headers {"Location" "/"}})))

(defn upload [{:keys [biff/db biff.xtdb/node form-params, query-params] :as ctx}]
  (let [{:strs [text lang]} form-params 
        uid  (java.util.UUID/randomUUID)
        transac-result (biff/submit-tx ctx [{ :db/doc-type :post :xt/id   uid
                                              :post/text text :post/lang lang }])]
      (if-let [res (xt/entity (xt/db node) uid)]
        {:status 303 :headers { "Location" (str "/" uid) }}
        {:status 500 :body {:error "something bad happenend"}})))

(def module
  {:routes [["/"  {:get home-page}]
            ["/:id" {:get content}]]
   :api-routes [["/upload" {:post upload}]]})

