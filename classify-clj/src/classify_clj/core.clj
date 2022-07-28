(ns classify-clj.core
  (:gen-class)
  (:require 
    [classify-clj.classify :refer :all]
    [classify-clj.data :refer :all]
    [classify-clj.atom-print :as ap]))

(defn compare-to-expected [result expected]
  ;; `assert` could be good but in this case not throwing the
  ;; exception is somewhat easier

  (println result)
  (let [success? (= result expected)]
    (println (if success?
               "This was the expected result."
               "Unexpected result!"))
    success?))
  

(defn test-on-data []
  (compare-to-expected (tree :loanworthy samples :rid) expected1)
  ;; {:salary 
  ;;     ([:>50k true]
  ;;      [:>20k {:age 
  ;;                 ([:<25 false] [:>25 true])}]
  ;;      [:<20k false])}
  
  (compare-to-expected (tree :repeat customers :rid) expected2))
  ;;
  ;; {:education 
  ;; 	([:college {:age 
  ;;			([:20-30 true] 
  ;;			 [:31-40 true] 
  ;;			 [:51-60 false]
  ;;			 [:41-50 true])}] 
  ;;	 [:graduate true]
  ;;	 [:high-school false])}

(defn test-on-data-incremental-print []
  (ap/out-wipe)
  (compare-to-expected (ap/tree :loanworthy samples :rid) expected1)
  (println @ap/out)

  (ap/out-wipe)
  (compare-to-expected (ap/tree :repeat customers :rid) expected2)
  (println @ap/out))


(defn -main
  [& args]
  (case (first args)
    ("--verbose" "-v") (test-on-data-incremental-print)
    (test-on-data)))
