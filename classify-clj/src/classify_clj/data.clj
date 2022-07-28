(ns classify-clj.data
  (:gen-class))

(def sample1
{ :rid 1
  :married false
  :salary :>50k
  :balance :<5k
  :age :>25
  :loanworthy true
})

(def sample2
{ :rid 2
  :married true
  :salary :>50k
  :balance :>5k
  :age :>25
  :loanworthy true
})

(def sample3
{ :rid 3
  :married true
  :salary :>20k
  :balance :<5k
  :age :<25
  :loanworthy false
})

(def sample4
{ :rid 4
  :married false
  :salary :<20k
  :balance :>5k
  :age :<25
  :loanworthy false
})

(def sample5
{ :rid 5
  :married false
  :salary :<20k
  :balance :<5k
  :age :>25
  :loanworthy false
})

(def sample6
{ :rid 6
  :married true
  :salary :>20k
  :balance :>5k
  :age :>25
  :loanworthy true
})

(def samples [sample1 sample2 sample3 sample4 sample5 sample6])





(def customer1
{ :rid 101
  :age :20-30
  :city "ny"
  :gender :f
  :education :college
  :repeat true
})

(def customer2
{ :rid 102
  :age :20-30
  :city "sf"
  :gender :m
  :education :graduate
  :repeat true
})

(def customer3
{ :rid 103
  :age :31-40
  :city "ny"
  :gender :f
  :education :college
  :repeat true
})

(def customer4
{ :rid 104
  :age :51-60
  :city "ny"
  :gender :f
  :education :college
  :repeat false
})

(def customer5
{ :rid 105
  :age :31-40
  :city "la"
  :gender :m
  :education :high-school
  :repeat false
})

(def customer6
{ :rid 106
  :age :41-50
  :city "ny"
  :gender :f
  :education :college
  :repeat true
})

(def customer7
{ :rid 107
  :age :41-50
  :city "ny"
  :gender :f
  :education :graduate
  :repeat true
})

(def customer8
{ :rid 108
  :age :20-30
  :city "la"
  :gender :m
  :education :college
  :repeat true
})

(def customer9
{ :rid 109
  :age :20-30
  :city "ny"
  :gender :f
  :education :high-school
  :repeat false
})

(def customer10
{ :rid 110
  :age :20-30
  :city "ny"
  :gender :f
  :education :college
  :repeat true
})

(def customers [customer1 customer2 customer3
                customer4 customer5 customer6
                customer7 customer8 customer9
                customer10])
      


;; (tree :loanworthy samples :rid)

(def expected1
  {:salary 
      '([:>50k true]
        [:>20k {:age 
                   ([:<25 false] [:>25 true])}]
        [:<20k false])})

;; (tree :repeat customers :rid)

(def expected2
  {:education 
    '([:college {:age 
                      ([:20-30 true] 
                       [:31-40 true] 
                       [:51-60 false]
                       [:41-50 true])}] 
      [:graduate true]
      [:high-school false])})

