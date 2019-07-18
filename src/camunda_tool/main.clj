(ns camunda-tool.main
  (:require [clojure.tools.cli :as cli]
            [camunda-tool.specs]
            [clojure.spec.alpha :as s]
            [clojure.pprint :refer [pprint]]
            [cheshire.core :as cheshire]
            [clojure.string :as string]
            [clj-http.client :as client]
            [slingshot.slingshot :refer [throw+]]))

(def opt-spec
  [["-a"
    "--api API_ENDPOINT"
    "API endpoint to use"]
   ["-r"
    "--raw"
    "Raw JSON output?"]
   ["-f"
    "--list-format MODE"
    "List format mode. Has to be one of [ids, full]"]])

(defn- const [x _]
  x)

(defmulti handle-command!
  (comp keyword first const))

(defmethod handle-command! :list [_ {:keys [api raw history list-format]
                                     :or {api "http://localhost:8080/engine-rest"
                                          raw false
                                          list-format :full
                                          history false}}]
  (let [json (->> "/history/process-instance"
                  (str api)
                  client/get
                  :body)
        filter-fn (if history
                    identity
                    (fn [xs]
                      (filter #(= (get % "state") "ACTIVE") xs)))
        print-fn (if (= list-format :ids)
                   (comp pprint
                         (fn [xs]
                           (map #(get % "id") xs)))
                   pprint)]
    (if-not raw
      (->> json
           cheshire/parse-string
           filter-fn
           (map #(select-keys % ["processDefinitionName"
                                 "id"
                                 "state"
                                 "startTime"
                                 "businessKey"]))
           print-fn)
      json)))

(defmethod handle-command! :hlist [commands options]
  (handle-command! [:list] (assoc options :history true)))

(defn- tweak [{:keys [list-format] :as options}]
  (update options :list-format keyword))

(defn -main [& args]
  (let [[commands unparsed-options] (split-with #(not= (first %) \-) args)]
    (let [{:keys [options errors]} (cli/parse-opts unparsed-options
                                                   opt-spec)]
      (s/check-asserts true)
      (s/assert :camunda-tool.specs/command-list commands)
      (s/assert :camunda-tool.specs/options-map options)
      (if-not errors
        (handle-command! commands (tweak options))
        (throw+ {:type ::error-parsing-command-line}
                (string/join "\n" errors))))))
