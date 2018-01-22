(ns network-grapher.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]))

(def log-file "network-log.edn")

(defn time-request
  [url]
  (try
    (let [resp (client/get url)
          headers (:headers resp)]
      (merge
       {:url url
        :date (get headers "Date")}
       (select-keys resp [:status :request-time])))
    (catch Exception e
      {:url url
       :status -1
       :ex-type (type e)
       :message (.getMessage e)})))

(defn time-requests-blocking
  [urls]
  (doall
   (map time-request urls)))

(defn read-log
  [f]
  (try
    (-> (slurp f) read-string)
    (catch Exception e
      [])))

(defn conj-log!
  [f o]
  (let [log (read-log f)
        new-log (conj log o)]
    (spit f (with-out-str (pprint new-log)))))

(comment

  (def sites ["https://seesawlabs.com"
              "https://google.com"
              "http://example.com"])

  (map time-request sites)

  (def response (client/get "https://seesawlabs.com"))

  (keys response)

  (:headers response)

  (conj-log! log-file (time-request "https://seesawlabs.com"))

  (read-log log-file)

  (group-by :status (read-log log-file))

  (time-requests-blocking sites)

  )

(defn -main
  [file & urls]
  (println "network-grapher started with urls:")
  (pprint urls)
  (println (format "Check %s for output" file))
  (println "Starting to poll the network...")
  (while true
    (conj-log! file (time-requests-blocking urls))
    (print ".")
    (flush)
    (Thread/sleep 10000)))
