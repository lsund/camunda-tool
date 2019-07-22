(ns camunda-tool.specs
  (:require [clojure.spec.alpha :as s]
            [medley.core :refer [uuid]]
            [clojure.string :as string]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Options

(s/def ::options-map (s/or :map (s/keys :opt-un [::api ::list-view ::pretty ::no-filter])
                           :nil nil?))

(s/def ::list-view boolean?)

(s/def ::pretty boolean?)

(s/def ::no-filter boolean?)

(s/def ::api (s/and ::protocol ::engine-rest))

(s/def ::protocol (s/or :string #(string/starts-with? % "http://")
                        :string #(string/starts-with? % "https://")))

(s/def ::engine-rest (fn [[ _ x]] (re-find #"engine-rest" x)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Commands

;; A command list is always a collection of strings and is either a
;; 1. List command
(s/def ::command-list (s/and (s/coll-of string?)
                             (s/or :vec ::list-command
                                   :vec ::vars-command
                                   :vec ::start-command)))

;; A List command is a command starting with `list` or `hlist` and
;; consists either one or two commands.  If two commands, the second
;; is a string representing a camunda definition.
(s/def ::list-command #(or (and (= (count %) 1)
                                (some #{(first %)} ["list" "hlist"]))
                           (and (= (count %) 2)
                                (some #{(first %)} ["list" "hlist"])
                                (s/valid? ::camunda-definition
                                          (second %)))))

(s/def ::camunda-definition string?)

(s/def ::vars-command #(and (= (count %) 2)
                            (some #{(first %)} ["vars" "delete"])
                            (uuid? (uuid (second %)))))

(s/def ::start-command #(and (>= (count %) 2)
                             (even? (count %))
                             (= (first %) "start")
                             (string? (second %))))
