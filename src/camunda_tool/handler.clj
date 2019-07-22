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

(defn- process-json [{:keys [pretty no-filter] :as options} data process-fn raw-process-fn]
  (let [f (if no-filter raw-process-fn process-fn)]
    (-> data
        cheshire/parse-string
        f
        (cheshire/generate-string (if pretty {:pretty true} {})))))

(defmethod request! :list [[_ definition]
                           {:keys [api history historic?]
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

(defn- current-external-tasks-for [id {:keys [api]}]
  (->> (str api "/external-task")
       (flip client/get {:accept :json :query-params {:processInstanceId id}})
       :body
       cheshire/parse-string
       (map #(select-keys % ["workerId" "errorMessage" "activityId"]))))

(defmethod request! :start [[_ definition-key & args] {:keys [api monitor] :as options}]
  (let [resp (->> (str "/process-definition/key/" definition-key "/start")
                  (str api)
                  (flip client/post
                        {:form-params {:variables (cli->camunda-variables args)}
                         :content-type :json})
                  :body)]
    ;; TODO should pprint the start message before monitoring finishes
    (let [out (process-json options resp #(select-keys % ["id"]) identity)
          id (-> (get (cheshire/parse-string out) "id"))
          errors (atom nil)]
      (when monitor
        (do
          (while (not @errors)
            (let [tasks (current-external-tasks-for id options)]
              (reset! errors (some #(get % "errorMessage") tasks))
              (println "Executing activity: " (map #(get % "activityId") tasks))
              (Thread/sleep 2000)))
          (if @errors
            (println (str "Process instance failed with: " @errors))
            (println "Process instance ended normally"))
          out)
        out))))

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
