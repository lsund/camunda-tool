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

(defn- historic-filter [historic? xs]
  (if historic?
    xs
    (filter #(= (get % "state") "ACTIVE") xs)))

(defn- process-definition-filter [definition xs]
  (if-not definition
    xs
    (filter #(= (get % "processDefinitionName") definition) xs)))

(defmethod handle-command! :list [[_ definition]
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
           (historic-filter historic?)
           (process-definition-filter definition)
           (map #(select-keys % ["processDefinitionName"
                                 "id"
                                 "state"
                                 "startTime"
                                 "businessKey"]))
           print-fn))))

(defmethod handle-command! :hlist [commands options]
  (handle-command! [:list] (assoc options :historic? true)))

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
