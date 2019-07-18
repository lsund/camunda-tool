(ns camunda-tool.handler
  (:require [cheshire.core :as cheshire]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]))

(defn- const [x _]
  x)

(defmulti process!
  (comp keyword first const))

(defn- filter-historic [historic? xs]
  (if historic?
    xs
    (filter #(= (get % "state") "ACTIVE") xs)))

(defn- filter-process-definition [definition xs]
  (if-not definition
    xs
    (filter #(= (get % "processDefinitionName") definition) xs)))

(defmethod process! :list [[_ definition]
                           {:keys [api raw history list-format historic?]
                            :or {api "http://localhost:8080/engine-rest"
                                 raw false
                                 list-format :full
                                 historic? false}
                            :as options}]
  (let [json (->> "/history/process-instance"
                  (str api)
                  client/get
                  :body)
        print-fn (if (= list-format :ids)
                   (comp pprint
                         (fn [xs]
                           (map #(get % "id") xs)))
                   pprint)]
    (if raw
      json
      (->> json
           cheshire/parse-string
           (filter-historic historic?)
           (filter-process-definition definition)
           (map #(select-keys % ["processDefinitionName"
                                 "id"
                                 "state"
                                 "startTime"
                                 "businessKey"]))
           print-fn))))

(defmethod process! :hlist [commands options]
  (process! [:list] (assoc options :historic? true)))
