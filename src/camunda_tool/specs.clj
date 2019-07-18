(ns camunda-tool.specs
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]))

(s/def ::camunda-definition string?)

;; A List command is a command starting with `list` or `hlist` and
;; consists either one or two commands.  If two commands, the second
;; is a string representing a camunda definition.
(s/def ::list-command #(or (and (= (count %) 1)
                                (some #{(first %)} ["list" "hlist"]))
                           (and (= (count %) 2)
                                (some #{(first %)} ["list" "hlist"])
                                (s/valid? ::camunda-definition
                                          (second %)))))

;; An command list is always a collection of strings and is either a
;; 1. List command
(s/def ::command-list (s/and (s/coll-of string?)
                             (s/or :vec ::list-command)))

(s/def ::protocol (s/or :string #(string/starts-with? % "http://")
                        :string #(string/starts-with? % "https://")))
(s/def ::engine-rest (fn [[ _ x]] (re-find #"engine-rest" x)))
(s/def ::raw boolean?)
(s/def ::api (s/and ::protocol ::engine-rest))
(s/def ::list-format #(some #{%} ["ids" "compact" "full"]))

(s/def ::options-map (s/or :map (s/keys :opt-un [::api ::raw ::list-format])
                           :nil nil?))
