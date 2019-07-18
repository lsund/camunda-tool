(ns camunda-tool.main
  (:require [clojure.tools.cli :as cli]
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
    "--format-list MODE"
    "List format mode. Has to be one of [simple, full]"]])

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
                              xs)))
        print-fn pprint]
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

(defn split-arguments [args]
  (split-with #(not= (first %) \-) args))

(defn commands [args]
  (first (split-arguments args)))

(defn options [args]
  (second (split-arguments args)))

(s/def ::camunda-definition string?)

;; A List command is a command starting with `list` or `hlist` and
;; consists either one or two commands.  If two commands, the second
;; is a string representing a camunda definition.
(s/def ::list-command #(or (and (= (count (commands %)) 1)
                                (some #{(first %)} ["list" "hlist"]))
                           (and (= (count (commands %)) 2)
                                (some #{(first %)} ["list" "hlist"])
                                (s/valid? ::camunda-definition
                                          (second %)))))

;; An argument list is always a collection of strings and is either a
;; 1. List command
(s/def ::argument-list (s/and (s/coll-of string?)
                              (s/or :vec ::list-command)))

(defn -main [& args]
  (s/assert ::argument-list args)
  (let [[commands unparsed-options] (split-arguments args)
        {:keys [options errors]} (cli/parse-opts unparsed-options opt-spec)]
    (if-not errors
      (handle-command! commands options)
      (throw+ {:type ::error-parsing-command-line}
              (string/join "\n" errors)))))
