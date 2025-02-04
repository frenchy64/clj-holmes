(ns clj-holmes.logic.progress
  (:require [progrock.core :as pr]))

(def ^:private bar (atom (pr/progress-bar 100)))

(def counter (atom 0))

(defn add-watch-to-counter []
  (add-watch counter
             :print (fn [_ _ _ new-state]
                      (-> @bar (pr/tick new-state) pr/print))))

(defn count-progress-size [files rules]
  (let [amount-of-files (count files)
        amount-of-rules (count rules)
        total-tasks (* amount-of-files amount-of-rules)]
    (if (zero? total-tasks)
      1
      (->> total-tasks (/ 100) float))))
