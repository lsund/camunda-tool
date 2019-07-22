(ns camunda-tool.main
  (:gen-class)
  (:require [camunda-tool.handler :as handler]
            [camunda-tool.format :refer [pprint-json]]
            camunda-tool.specs
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [slingshot.slingshot :refer [throw+]]))

(def opt-spec
  [["-a"
    "--api API_ENDPOINT"
    "API endpoint to use"]
   ["-o"
    "--output-format OUTPUT_FORMAT"
    "Output format. Valid values : pretty-json | json | camunda-json | list"]])

(defn- merge-defaults [options]
  (merge {:api "http://localhost:8080/engine-rest"
          :output-format :json
          :historic? false}
         options))

(defn- keywordize [options]
  (update options :output-format keyword))

(defn -main [& args]
  (let [[commands unparsed-options] (split-with #(not= (first %) \-) args)]
    (let [{:keys [options errors]} (cli/parse-opts unparsed-options
                                                   opt-spec)
          options (-> options
                      merge-defaults
                      keywordize)]
      (s/check-asserts true)
      (s/assert :camunda-tool.specs/command-list commands)
      (s/assert :camunda-tool.specs/options-map options)
      (if-not errors
        (pprint-json commands options (handler/request! commands options))
        (throw+ {:type ::cli-parsing-error}
                (string/join "\n" errors))))))
