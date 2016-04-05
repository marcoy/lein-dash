(defproject lein-dash "0.1.1"

  :description "Generated docsets from Codox"

  :url "http://www.marcoyuen.com"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[codox "0.8.11"]
                 [org.xerial/sqlite-jdbc "3.8.7"]
                 [yesql "0.4.0"]]
                 [enlive "1.1.6"]

  :min-lein-version "2.5.0"

  :eval-in-leiningen true)
