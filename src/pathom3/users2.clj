(ns pathom3.users2
  (:require
   [clojure.java.io :as io]
   [com.wsscode.pathom3.connect.indexes :as pci]
   [com.wsscode.pathom3.connect.operation :as pco]
   [com.wsscode.pathom3.interface.smart-map :as psm]
   [tablecloth.api :as tc]))

(def dataset
  (-> (io/resource "users2.csv")
        .getFile
        tc/dataset))

(pco/defresolver users2->id-name
  [{::keys [raw]}]
  {::id (tc/column raw "ID")
   ::name (tc/column raw "Name")})

(comment
  (users2->id-name {::raw dataset}))

;; TODO remove extra spaces and capitalize
;; each word
(pco/defresolver name->clean-name
  [{::keys [name]}]
  {::clean-name name})

(def index (pci/register [users2->id-name
                          name->clean-name]))


(comment
  (def m (-> (psm/smart-map index {::raw dataset})
             (psm/sm-touch! [::clean-name])) )
  )
