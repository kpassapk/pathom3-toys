(ns pathom3.datasets.toy
  (:require
   [com.wsscode.pathom3.connect.indexes :as pci]
   [com.wsscode.pathom3.connect.operation :as pco]
   [com.wsscode.pathom3.interface.smart-map :as psm]
   [pathom3.datasets.users1 :as u1]
   [pathom3.datasets.users2 :as u2]
   [tablecloth.api :as tc]))

(pco/defresolver u1-u2-joined
  [{::keys [u1 u2]}]
  {::pco/output [::id ::name ::phone]}
  (let [ds1-fields [::u1/clean-name ::u1/clean-phone]
        ds2-fields [::u2/clean-name ::u2/id]

        ds1 (-> (psm/smart-map u1/index {::u1/raw u1})
                (psm/sm-touch! ds1-fields)
                (select-keys ds1-fields)
                (tc/dataset))

        ds2 (-> (psm/smart-map u2/index {::u2/raw u2})
                (psm/sm-touch! ds2-fields)
                (select-keys ds2-fields)
                (tc/dataset))
        join (-> ds1
                 (tc/inner-join ds2 {:left ::u1/clean-name :right ::u2/clean-name}))]
    {::id (::u2/id join)
     ::name (::u1/clean-name join)
     ::phone (::u1/clean-phone join)}))

(def env (pci/register [u1-u2-joined]))

(comment
  (def m (-> (psm/smart-map env {::u1 u1/dataset ::u2 u2/dataset})
             (psm/sm-touch! [::id ::name ::phone])
             (select-keys [::id ::name ::phone])
             (tc/dataset)))

  m
  ;; | :pathom3.datasets/id | :pathom3.datasets/name | :pathom3.datasets/phone |
  ;; |---------------------:|------------------------|------------------------:|
  ;; |                  123 |           Jose Avelino |                50010604 |


)
