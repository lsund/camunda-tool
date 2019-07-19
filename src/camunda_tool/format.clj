(ns camunda-tool.format
  (:require [cheshire.core :as cheshire]
            [medley.core :refer [map-keys]]))

(defn json->list [{:keys [output]} data]
  (if (= output :list)
    (let [data (cheshire/parse-string data)
          maxlen (->> data (map-keys count) keys (apply max))]
      (println (str "KEY" (make-space 3 maxlen) "VALUE"))
      (println (str "===" (make-space 3 maxlen) "====="))
      (doseq [[k v] data]
        (println (str k (make-space (count k) maxlen) v))))
    (println data)))

(defn make-space [len maxlen]
  (apply str (take (- (+ maxlen 1) len) (repeat " "))))
