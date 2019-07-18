(ns camunda-tool.main
  (:require [clojure.tools.cli :as cli]
            [clojure.spec.alpha :as s]
            [clojure.pprint :refer [pprint]]
            [cheshire.core :as cheshire]
            [clojure.string :as string]
            [clj-http.client :as client]
            [slingshot.slingshot :refer [throw+]]))

(def opt-spec
  [["-a" "--api API_ENDPOINT" "API endpoint to use"]
   ["-r" "--raw" "Raw JSON output?"]])

(defn- const [x _]
  x)

(defmulti handle-command!
  (comp keyword first const))

(defmethod handle-command! :list [_ {:keys [api raw history]
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

(defmethod handle-command! :hlist [commands options]
  (handle-command! [:list] (assoc options :history true)))

(s/def ::camunda-definition string?)

(s/def ::arguments (s/and (s/coll-of string?)
                          (s/or :vec #(and (= (first %) "list")
                                           (s/conform ::camunda-definition
                                                      (second %)))
                                :vec #(= (first %) "hlist"))))

(defn -main [& args]
  (s/assert ::arguments args)
  (let [[commands unparsed-options] (split-with #(not= (first %) \-) args)
        {:keys [options errors]} (cli/parse-opts unparsed-options opt-spec)]
    (if-not errors
      (handle-command! commands options)
      (throw+ {:type ::error-parsing-command-line}
              (string/join "\n" errors)))))
