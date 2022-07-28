;; To generate decision trees for classification.
;; Copyright 2022 Luc Sahar. MIT License.
;; For Advanced DB Management, Florida International Univerisity

(ns classify-clj.atom-print
  (:gen-class))

(refer
 'classify-clj.classify
 :exclude '[entropy gain
            determine-best-gain get-gains
            pair-values-and-classes
            tree])

;; provide an option to incrementally output each of the
;; entropy(s) and gains we calculate along the way

;; flagged in calls to `gain` and `entropy`

(def out (atom ""))

(defn out-append [& inputs]
  (apply swap! out str inputs))

(defn out-wipe []
  (reset! out ""))

(defn entropy [classifier attribute data] ; `data` is a list of maps
  ;; each map looks like
  ;; {:gender :m, :city "nyc", :repeat true,
  ;;  :education :college, :age "20..30"}

  (let [msg (str
             "Calculating entropy of " attribute
             ;" for classifier " classifier
             "...\n"
             "\tEntropy is ")
        options (possibilities attribute data)
        cross-on-option 
            (fn [option] 
                (info-cross-preponderance
                 classifier attribute option data))
        result (sum (map cross-on-option options))]
    (out-append msg)
    (out-append result "\n")

    result))

   
(defn gain [classifier attribute data]
  (out-append (str "\nCalculating gain of " attribute
                   ;" for classifier " classifier
                   "...\n"))

  (let [result (- 
                (apply I (count-per-class classifier data)) 
                (entropy classifier attribute data))]
    (out-append "\tGain is " result "\n")
    result))

(defn get-gains 
  "Returns a map from attributes to gains."
  [classifier data & ignores]
  (let [spec (first data)
        cleaned (reduce dissoc spec (cons classifier ignores))
        ks (keys cleaned)]
    (into {}
      (map (fn [attribute]
             {attribute 
             (gain classifier attribute data)})
           ks))))

(defn determine-best-gain [classifier data & ignores]
  (let [gains  (apply get-gains classifier data ignores)
        pair (max-by-value gains)
        result (first pair)
        value  (second pair)]
    (out-append "\nThe maximum gain is " result " (" value ")\n\n")
    result))

(defn pair-values-and-classes [classifier data & ignores]
  (let [best-gain (apply determine-best-gain classifier data ignores)]
  (map 
    (fn [kv] {(first kv) 
              (possibilities
                classifier (second kv))}) 
     (partitioner best-gain data))))

(defn pair-values-and-classes [classifier data gain]
  (map 
    (fn [kv] [(first kv) 
              (->> kv second 
                 (possibilities classifier) extract-if-one)])
     (partitioner gain data)))

(defn tree [classifier data & ignores]
  (let [best-gain (apply determine-best-gain classifier data ignores)
        value-class-pairings (pair-values-and-classes classifier data best-gain)]
    {best-gain
     (map (fn [pairing] 
             (if (seq? (second pairing))
              ;; then we have to recurse
               [(first pairing)
                (apply tree 
                      classifier 
                      (only-this best-gain (first pairing) data)
                      best-gain ;; also ignore what we've already split on
                      ignores)]
	      ;; otherwise we return our result
              pairing)) 
           value-class-pairings)}))





;; todo: don't use copies of unchanged functions
