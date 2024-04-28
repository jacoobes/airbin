(ns air.bin.schema)

(def schema
  {:post/id :uuid
   :post [:map {:closed true}
           [:xt/id                       :post/id]
           [:post/lang {:optional true}  :string]
           [:post/text                 :string]]})

(def module
  {:schema schema})
