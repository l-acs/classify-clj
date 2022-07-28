(defproject classify-clj "0.1.0-SNAPSHOT"
  :description "To generate decision trees for classification."
  :url "http://github.com/l-acs/classify-clj/"
  :license {:name "MIT"
            :url "FIXME"}
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :main ^:skip-aot classify-clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
