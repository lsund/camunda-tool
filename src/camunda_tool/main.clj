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
   ["-n"
    "--no-filter"
    "Return the original camunda json response"]
   ["-p"
    "--pretty"
    "Pretty print output. Only effective if -h is not set."]
   ["-h"
    "--human-readable"
    "Output a human-readable readable list instead of JSON"]
   ["-m"
    "--monitor"
    "Monitor started process. Only effective with the `start` command"]])

(defn- merge-defaults [options]
  (merge {:api "http://localhost:8080/engine-rest"
          :historic? false}
         options))

(defn -main [& args]
  (let [[commands unparsed-options] (split-with #(not= (first %) \-) args)]
    (let [{:keys [options errors]} (cli/parse-opts unparsed-options
                                                   opt-spec)
          options (-> options merge-defaults)]
      (s/check-asserts true)
      (s/assert :camunda-tool.specs/command-list commands)
      (s/assert :camunda-tool.specs/options-map options)
      (if-not errors
        (pprint-json commands options (handler/request! commands options))
        (throw+ {:type ::cli-parsing-error}
                (string/join "\n" errors))))))
