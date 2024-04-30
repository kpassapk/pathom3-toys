(ns pathom3.datasets.users1
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [com.wsscode.pathom3.connect.indexes :as pci]
   [com.wsscode.pathom3.connect.operation :as pco]
   [com.wsscode.pathom3.interface.eql :as p.eql]
   [tablecloth.api :as tc]
   [tablecloth.column.api :as tcc]
   [com.wsscode.pathom3.interface.smart-map :as psm]))

(def dataset
  (-> (io/resource "users1.csv")
      .getFile
      tc/dataset))

(pco/defresolver raw->first-name-last-name-phone
  "Maps source fields to known fields"
  [{::keys [raw]}]
  {::first-name (tc/column raw "Nombre")
   ::last-name (tc/column raw "Apellido")
   ::phone (tc/column raw "Telefono")})

(comment
  (raw->first-name-last-name-phone {::raw dataset}))

(pco/defresolver first-name->clean-first-name
  [{::keys [first-name]}]
  {::clean-first-name (-> first-name
                          (tcc/column-map str/capitalize))})

(comment
  (first-name->clean-first-name {::first-name (tcc/column ["JUAN"])}))

(pco/defresolver last-name->clean-last-name
  [{::keys [last-name]}]
  {::clean-last-name (-> last-name
                         (tcc/column-map str/capitalize))})

(pco/defresolver clean-first-name-clean-last-name->clean-name
  [{::keys [clean-first-name clean-last-name]}]
  {::clean-name (tcc/column-map [clean-first-name clean-last-name]
                                 (fn [f l] (str f " " l)))})

;; TODO integrate libphonenumber
(pco/defresolver phone->clean-phone
  [{::keys [phone]}]
  {::clean-phone phone})

(comment
  (clean-first-name-clean-last-name->clean-name
   {::clean-first-name (tcc/column ["Juan"])
    ::clean-last-name (tcc/column ["Apellido"])}))

(def index (pci/register [raw->first-name-last-name-phone
                          first-name->clean-first-name
                          last-name->clean-last-name
                          clean-first-name-clean-last-name->clean-name
                          phone->clean-phone]))

(comment

  ;; Doesn't work
  (p.eql/process index {::raw dataset} [::first-name])

  ;; Works
  (def m (-> (psm/smart-map index {::raw dataset})
             (psm/sm-touch! [::clean-name])))

  m
  ;; {:raw [1 3]:

  ;; | Nombre | Apellido | Telefono |
  ;; |--------|----------|---------:|
  ;; |   JOSE |  Avelino | 50010604 |
  ;; , :first-name ("JOSE"),
  ;; :last-name ("Avelino"),
  ;; :phone (50010604),
  ;; :clean-last-name ("Avelino"),
  ;; :clean-first-name ("Jose"),
  ;; :clean-name ("Jose Avelino")}

)
