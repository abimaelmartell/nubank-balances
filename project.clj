(defproject balances "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [http-kit "2.3.0"]
                 [compojure "1.6.1"]
                 [org.clojure/data.json "0.2.7"]
                 [clj-time "0.15.2"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [ring/ring-headers "0.3.0"]
                 [ring/ring-mock "0.4.0"]
                 [ring/ring-json "0.5.0"]]
  :main ^:skip-aot balances.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
