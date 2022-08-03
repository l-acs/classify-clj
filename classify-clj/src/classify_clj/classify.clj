;; To generate decision trees for classification.
;; Copyright 2022 Luc Sahar. MIT License.
;; For Advanced DB Management, Florida International Univerisity

(ns classify-clj.classify
  (:gen-class))

(defn log2 [n]
   (/ (Math/log n) (Math/log 2)))

(defn sum [l] (apply + l))

(defn I [& values]
   (let [total (sum values)
	 prob #(/ % total)
         logprob #(* (prob %) (log2 (prob %)))]

      (->> values
          (map logprob) sum (* -1))))

(defn possibilities [attribute data]
      (distinct (map attribute data)))

(defn only-this [attribute trait data]
   (filter #(= (get % attribute) trait) data))

(defn partitioner [attribute data]
   (let [options (possibilities attribute data)]
     (->> options
         (map 
             (fn [option] 
                 {option (only-this attribute option data)}))
         
	  (into {}))))

(defn rates [attribute data] 
    (->> data (map attribute) frequencies))

(defn count-per-class [classifier data]
  (vals
    (rates classifier data)))

(defn occurences [attribute trait data]
   (let [frequency (rates attribute data)]
      (get frequency trait))) 

(defn preponderance [attribute trait data]
    (let [number (occurences attribute trait data)]
       (/ number (count data))))
	

(defn classify-with-frequency [classifier classes subset]
  ;; subset is one kv partition on an attribute
  ;; we'll break it down

  (let [trait   (first  subset)
        matches (second subset)]
     
    {trait (frequencies 
               (map classifier matches))}))


(defn partition-and-classify 
  "Partitions on some attribute: within each partition (each possible value of that attribute), how many items belong to each class?"
  [classifier attribute data]
  (let [classes (possibilities classifier data)
        partitions (partitioner attribute data)]
    (->> partitions
         (map
	    #(classify-with-frequency classifier classes %))
         (into {}))))

(defn info-cross-preponderance [classifier attribute option data]
  ;; take, e.g., classifier :salary :>50k samples
  ;; and return 
  ;; (* preponderance-of-salary==:>50k-in-samples
  ;;    (I number-of-:>50ks-that-belong-to-class-one
  ;;       number-of-:>50ks-that-belong-to-class-two
  ;;       number-of-:>50ks-that-belong-to-class-...
  ;;       number-of-:>50ks-that-belong-to-class-n))

  (let [freq (preponderance attribute option data)
        partitions (partition-and-classify classifier attribute data)
        belong-to-class (-> partitions (get option) vals)]
      (* freq
         (apply I belong-to-class))))


(defn entropy [classifier attribute data] ; `data` is a list of maps
  ;; each map looks like
  ;; {:gender :m, :city "nyc", :repeat true,
  ;;  :education :college, :age "20..30"}

  (let [options (possibilities attribute data)
        cross-on-option 
            (fn [option] 
                (info-cross-preponderance
                    classifier attribute option data))]
  (sum  
     (map cross-on-option options))))
   
(defn gain [classifier attribute data]
   (- 
     (apply I (count-per-class classifier data)) 
     (entropy classifier attribute data)))

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


;; now let's break down data, first by automating getting the top gain
(defn kv-value-compare [kv1 kv2]
  (let [v1 (second kv1)
        v2 (second kv2)]
    (if (> v1 v2) kv1 kv2)))

(defn max-by-value
  "Given a hashmap, return the kv pair whose v is highest"
   [hmap]
   (reduce kv-value-compare hmap))

(defn determine-best-gain [classifier data & ignores]
   (let [gains (apply get-gains classifier data ignores)]
     (first (max-by-value gains))))


;; now implement the tree recursively:
;; - get the best gain,
;; - split off the values into leaf nodes that do not span multiple classes
;; - recurse on the values as intermediate nodes that *do* span multiple classes

;; e.g. Whereas all salaries <20k are non-loanworthy and all salaries >50k are loanworthy, 
;; there are some 20k..50k salaries that are loanworthy and others that are not.
;; Therefore, an intermediate node under 20k..50k is needed, which will split again on some other attribute

;; steps:
;; - get best gain
;; - map over the possibile values of that attribute:
;;     - if records with a given possibility show up in only type of class, create a leaf node for that possibility and designate its class
;;     - otherwise recurse, this time on the subset of the data having that value for that attribute

;; that subset: 
;;   (only-this best-gain <value spanning multiple classes> data)

(defn pair-values-and-classes [classifier data & ignores]
  (let [best-gain (apply determine-best-gain classifier data ignores)]
  (map 
    (fn [kv] {(first kv) 
              (possibilities
                classifier (second kv))}) 
     (partitioner best-gain data))))

(defn extract-if-one
  "Pull an item out of a list if it is the only member. Useful to later be able to filter data by `seq?`."
  [item]
  (if (= 1 (count item))
      (first item)
      item))

(defn pair-values-and-classes [classifier data gain]
  (map 
    (fn [kv] [(first kv) 
              (->> kv second 
                 (possibilities classifier) extract-if-one)])
     (partitioner gain data)))

(defn apply-if-seq [kv f & args]
  (if-not (seq? (second kv))
    kv
    [(first kv)
     (apply f args)]))

(defn tree [classifier data & ignores]
  (let [best-gain (apply determine-best-gain classifier data ignores)
        value-class-pairings (pair-values-and-classes classifier data best-gain)]
    {best-gain
     (map 
      #(apply-if-seq
        %
        tree classifier  ; we continue to classify
        (only-this best-gain (first %) data) ;  recursing on the matching data, e.g. only the ones whose :education is :college

        best-gain ;; and now we also add :education to the list of
                  ;; attributes we can't use to classify the data
        ignores)
      value-class-pairings)}))

;; (defn tree [classifier data & ignores]
;;   (let [best-gain (apply determine-best-gain classifier data ignores)
;;         value-class-pairings (pair-values-and-classes classifier data best-gain)]
;;     {best-gain
;;      (map (fn [pairing] ;; for every pairing
;;             (if-not (seq? (second pairing)) ;; if it only corresponds to one possible value for our classifier
;;               pairing ;; then we return it
;;               ;; otherwise we have to recurse
;;               (let [only-matching-data (only-this best-gain (first pairing) data)] ;; we only consider the data that have that value for that attribute (e.g., the customers whose :education is :college)
;;                 [(first pairing) ;; when the value for the best gain
;;                                  ;; attribute corresponds to this
;;                                  ;; value (say :education
;;                                  ;; and :college)
                 
;;                 (apply tree classifier  ; we continue to classify recursing on the matching data
;;                       only-matching-data
;;                       best-gain ;; and now we also add :education to
;;                                 ;; the list of attributes we ignore
;;                                 ;; when determining the attribute with
;;                                 ;; the best gain
;;                       ignores)]))) 
;;            value-class-pairings)}))





