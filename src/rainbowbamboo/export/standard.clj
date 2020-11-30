(ns rainbowbamboo.export.standard
  (:require
   [clojure.java.io :as io]))

(defn export-amp [dir story]
  (println "inside export amp")
  (let [{:keys [path html-page]} story]
    (let [full-path (cond-> (str dir path)
                      (clojure.string/ends-with? path "/")
                      (str "index.html"))]
      (io/make-parents full-path)
      (spit full-path (cond-> html-page
                        (not (string? html-page)) str)))))
