(ns camunda-tool.main
  (:gen-class)
  (:require [camunda-tool.handler :as handler]
            camunda-tool.specs
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
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
    "List format mode. Has to be one of ids | compact | full"]])

(defn- keywordize [{:keys [list-format] :as options}]
  (update options :list-format keyword))

(defn- merge-defaults [options]
  (merge {:api "http://localhost:8080/engine-rest"
          :raw false
          :list-format :full
          :historic? false} options))

(defn -main [& args]
  (let [[commands unparsed-options] (split-with #(not= (first %) \-) args)]
    (let [{:keys [options errors]} (cli/parse-opts unparsed-options
                                                   opt-spec)]
      (s/check-asserts true)
      (s/assert :camunda-tool.specs/command-list commands)
      (s/assert :camunda-tool.specs/options-map options)
      (if-not errors
        (println (handler/request! commands (-> options
                                                merge-defaults
                                                keywordize )))
        (throw+ {:type ::cli-parsing-error}
                (string/join "\n" errors))))))
