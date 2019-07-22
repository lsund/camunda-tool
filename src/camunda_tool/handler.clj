(ns camunda-tool.handler
  (:require [cheshire.core :as cheshire]
            [me.lsund.util :refer [const flip]]
            [medley.core :refer [map-kv map-vals map-keys]]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [slingshot.slingshot :refer [try+]]))

(defmulti request!
  (comp keyword first const))

(defn- filter-historic [xs {:keys [historic?]}]
  (if historic?
    xs
    (filter #(= (get % "state") "ACTIVE") xs)))

(defn- filter-process-definition [def xs]
  (if-not def
    xs
    (filter #(= (get % "processDefinitionKey") def) xs)))

(defn- process-json [{:keys [output-format] :as options} data process-fn raw-process-fn]
  (let [f (case output-format
            :camunda-json raw-process-fn
            process-fn)]
    (-> data
        cheshire/parse-string
        f
        (cheshire/generate-string (case output-format
                                    :pretty-json {:pretty true}
                                    {})))))

(defmethod request! :list [[_ definition]
                           {:keys [api history list-format historic?]
                            :as options}]
  (let [resp (->> "/history/process-instance"
                  (str api)
                  client/get
                  :body)]
    (process-json options
                  resp
                  (comp
                   (fn [xs]
                     (map #(select-keys % ["processDefinitionName"
                                           "processDefinitionKey"
                                           "id"
                                           "state"
                                           "startTime"
                                           "businessKey"]) xs))
                   #(filter-process-definition definition %)
                   #(filter-historic % options))
                  #(filter-historic % options))))

(defmethod request! :hlist [commands options]
  (request! [:list] (assoc options :historic? true)))

(defmethod request! :vars [[_ id] {:keys [api] :as options}]
  (let [resp (->> (str "/process-instance/" id "/variables")
                  (str api)
                  client/get
                  :body)]
    (process-json options
                  resp
                  (fn [xs] (map-vals #(get % "value") xs))
                  identity)))

(defn- cli->camunda-variables [args]
  (->> args
       (apply hash-map)
       (map-keys keyword)
       (map-kv (fn [k v] [(name k) {"value" (str v)
                                    "type" "String"}]))))

(defmethod request! :start [[_ definition-key & args] {:keys [api] :as options}]

  (let [resp (->> (str "/process-definition/key/" definition-key "/start")
                  (str api)
                  (flip client/post
                        {:form-params {:variables (cli->camunda-variables args)}
                         :content-type :json})
                  :body)]
    (process-json options resp #(select-keys % ["id"]) identity)))

(defmethod request! :delete [[_ id] {:keys [api]}]
  (try+
   (->> (str "/process-instance/" id)
        (str api)
        client/delete
        :status)
   (cheshire/generate-string {:id id
                              :deleted true})
   (catch [:status 404] {}
     (cheshire/generate-string {:id id
                                :deleted false
                                :msg "No process instance with that ID exists"}))))
