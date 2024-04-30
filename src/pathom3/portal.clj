(ns pathom3.portal
  (:require [portal.api :as p]))

(add-tap #'p/submit)

(p/open)

