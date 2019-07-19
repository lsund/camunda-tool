(ns camunda-tool.main
  (:gen-class)
  (:require [camunda-tool.handler :as handler]
            [camunda-tool.format :refer [json]]
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
    "--output OUTPUT_TYPE"
    "Output type. Can be one of json | camunda-json | list"]])

(defn- merge-defaults [options]
  (merge {:api "http://localhost:8080/engine-rest"
          :output :json
          :historic? false} options))

(defn- keywordize [options]
  (update options :output keyword))

(defn -main [& args]
  (let [[commands unparsed-options] (split-with #(not= (first %) \-) args)]
    (let [{:keys [options errors]} (cli/parse-opts unparsed-options
                                                   opt-spec)]
      (s/check-asserts true)
      (s/assert :camunda-tool.specs/command-list commands)
      (s/assert :camunda-tool.specs/options-map options)
      (if-not errors
        (format/json options
                     (handler/request! commands (-> options
                                                    merge-defaults
                                                    keywordize)))
        (throw+ {:type ::cli-parsing-error}
                (string/join "\n" errors))))))
