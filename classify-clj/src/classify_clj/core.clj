(ns classify-clj.core
  (:gen-class)
  (:require 
    [classify-clj.classify :refer :all]
    [classify-clj.data :refer :all]))

(defn test-on-data []
  (println
    (tree :loanworthy samples :rid))
  
  ;; {:salary 
  ;;     ([:>50k true]
  ;;      [:>20k {:age 
  ;;                 ([:<25 false] [:>25 true])}]
  ;;      [:<20k false])}
  
  (println
    (tree :repeat customers :rid)))
  ;;
  ;; {:education 
  ;; 	([:college {:age 
  ;;			([:20-30 true] 
  ;;			 [:31-40 true] 
  ;;			 [:51-60 false]
  ;;			 [:41-50 true])}] 
  ;;	 [:graduate true]
  ;;	 [:high-school false])}



(defn -main
  [& args]
  (test-on-data))
