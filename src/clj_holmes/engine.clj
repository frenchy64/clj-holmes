(ns clj-holmes.engine
  (:require [clj-holmes.diplomat.code-reader :as diplomat.code-reader]
            [clj-holmes.logic.progress :as progress]
            [clj-holmes.output.main :as output]
            [clj-holmes.rules.loader.loader :as rules.loader]
            [clj-holmes.rules.processor.processor :as rules.processor])
  (:import (java.io StringWriter)))

(defn ^:private check-rules-in-code-structure [code-structure rules progress-size]
  (let [result (->> rules
                    (pmap #(rules.processor/init! code-structure %))
                    (filterv :result))]
    (swap! progress/counter + progress-size)
    result))

(defn scan* [opts]
  (let [code-structures (diplomat.code-reader/code-structure-from-clj-files-in-directory! opts)
        rules (rules.loader/init! opts)
        progress-size (progress/count-progress-size code-structures)
        scans-results (->> code-structures
                           (pmap #(check-rules-in-code-structure % rules progress-size))
                           (reduce into []))
        scan-result-output (output/output scans-results opts)]
    scan-result-output))

(defn scan [{:keys [verbose fail-on-result] :as opts}]
  (let [out (if verbose *out* (new StringWriter))]
    (when verbose (progress/add-watch-to-counter))
    (binding [*out* out]
      (let [result (scan* opts)]
        (when fail-on-result
          (count result))))))
