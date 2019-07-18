(ns camunda-tool.main
  (:require [clojure.tools.cli :as cli]
            [clojure.pprint :refer [pprint]]
            [cheshire.core :as cheshire]
            [clojure.string :as string]
            [clj-http.client :as client]
            [slingshot.slingshot :refer [throw+]]))

(def cli-options
  [["-a" "--api API_ENDPOINT" "API endpoint to use"]
   ["-r" "--raw" "Raw JSON output?"]])

(defn- const [x _]
  x)

(defmulti go!
  const)

(defmethod go! :list [_ {:keys [api raw history]
                         :or {api "http://localhost:8080/engine-rest"
                              raw false
                              history false}}]
  (let [json (->> "/history/process-instance"
                  (str api)
                  client/get
                  :body)
        filter-fn (if history
                    identity
                    (fn [xs]
                      (filter #(= (get % "state") "ACTIVE")
                              xs)))]
    (if-not raw
      (->> json
           cheshire/parse-string
           filter-fn
           (map #(select-keys % ["processDefinitionName"
                                 "id"
                                 "state"
                                 "startTime"
                                 "businessKey"]) )
           pprint)
      json)))

(defmethod go! :hlist [command options]
  (go! :list (assoc options :history true)))

;; TODO need (let [commands options] (parse args))

(defn -main [command & args]
  (let [{:keys [options errors]} (cli/parse-opts args cli-options)]
    (if-not errors
      (go! (keyword command) options)
      (throw+ {:type ::error-parsing-command-line}
              (string/join "\n" errors)))))
