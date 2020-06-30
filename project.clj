(defproject assignment4 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/data.csv "0.1.4"]
                 [org.clojure/data.json "0.2.7"]
                 [org.clojure/core.async "1.0.567"]
                 [ring/ring-core "1.8.0"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [ring-middleware-format "0.7.4"]
                 [ring-cors "0.1.13"]
                 [compojure "1.6.1"]
                 [http-kit "2.3.0"]
                 [com.taoensso/faraday "1.10.1"]]
  :plugins [[lein-ring "0.12.5"]
            [cider/cider-nrepl "0.24.0"]]
  :ring {:handler assignment4.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]
                        [org.clojure/test.check "0.9.0"]]}})
