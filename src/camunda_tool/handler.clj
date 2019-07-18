(ns camunda-tool.handler
  (:require [cheshire.core :as cheshire]
            [medley.core :refer [map-kv]]
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

(defn- filter-process-definition [def xs]
  (if-not def
    xs
    (filter #(= (get % "processDefinitionName") def) xs)))

(defn- print! [{:keys [list-format]} xs]
  (pprint (case list-format
            :ids (map #(get % "id") xs)
            :compact (map (fn [x]
                            (vals (select-keys x ["id"
                                                  "processDefinitionName"])))
                          xs)
            xs)))

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
                  :body)]
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
           (print! options)))))

(defmethod process! :hlist [commands options]
  (process! [:list] (assoc options :historic? true)))

(defmethod process! :vars [[_ id]
                           {:keys [api raw]
                            :or {api "http://localhost:8080/engine-rest"
                                 raw false}}]
  (let [json (->> (str "/process-instance/" id "/variables")
                  (str api)
                  client/get
                  :body)]
    (if raw
      json
      (->> json
           cheshire/parse-string
           (map-kv (fn [k v] [k (get v "value")]))
           pprint))))
