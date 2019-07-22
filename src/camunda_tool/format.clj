(ns camunda-tool.format
  (:require [cheshire.core :as cheshire]
            [medley.core :refer [map-keys]]))

(defmulti pprint-json
  (fn [commands _ _] (first commands)))

(defn make-space [len maxlen]
  (apply str (take (- (+ maxlen 1) len) (repeat " "))))

(defmethod pprint-json "vars" [_ {:keys [human-readable]} data]
  (if human-readable
    (let [data (cheshire/parse-string data)
          maxlen (->> data (map-keys count) keys (apply max))]
      (println (str "KEY" (make-space 3 maxlen) "VALUE"))
      (println (str "===" (make-space 3 maxlen) "====="))
      (doseq [[k v] data]
        (println (str k (make-space (count k) maxlen) v))))
    (do
      (print data)
      (flush))))

(defmethod pprint-json "list" [_ {:keys [human-readable]} data]
  (if human-readable
    (doseq [inst (cheshire/parse-string data)]
      (println (str (get inst "processDefinitionName") "\n"
                    "Id: " (get inst "id") "\n"
                    "Start: " (get inst "startTime") "\n")))
    (do
      (print data)
      (flush))))

(defmethod pprint-json "hlist" [commands options data]
  (pprint-json (conj (rest commands) "list") options data))

(defmethod pprint-json "start" [_ {:keys [human-readable]} data]
  (if human-readable
    (println (str "Started process with id: " (-> data cheshire/parse-string (get "id"))))
    (println data)))
