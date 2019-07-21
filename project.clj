(defproject camunda-tool "0.1.0-SNAPSHOT"
  :description "See README.md"
  :url "https://github.com/lsund/camunda-tool"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.cli "0.4.2"]
                 [me.raynes/fs "1.4.6"]
                 [me.lsund/util "0.6.0"]
                 [clj-http "3.10.0"]
                 [medley "1.2.0"]
                 [slingshot "0.12.2"]
                 [cheshire "5.8.1"]]
  :main camunda-tool.main
  :pedantic :abort
  :repl-options {:init-ns camunda-tool.main}
  :profiles {:uberjar {:aot :all
                       :uberjar-name "camunda-tool.jar"}})
