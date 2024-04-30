(ns pathom3.tutorial
  (:require
   [cheshire.core :as json]
   [com.wsscode.pathom3.connect.indexes :as pci]
   [com.wsscode.pathom3.connect.operation :as pco]
   [com.wsscode.pathom3.interface.eql :as p.eql]
   [org.httpkit.client :as http]))

#_(def explico-ip {:ip "45.186.106.167"})

(def tut-ip {:ip "198.29.213.3"})

(def explico-coords {:latitude "14.6343", :longitude "-90.5155"})

(pco/defresolver ip->lat-long
  [{:keys [ip]}]
  {::pco/output [:latitude :longitude]}
  (-> (slurp (str "https://get.geojs.io/v1/ip/geo/" ip ".json"))
      (json/parse-string keyword)
      (select-keys [:latitude :longitude])))


(pco/defresolver lat-long->temperature
  [{:keys [latitude longitude]}]
  {:temperature
   (-> @(http/request
         {:url (str "http://www.7timer.info/bin/api.pl?lon=" longitude
                   "&lat=" latitude
                   "&product=astro&output=json")})
          :body
          (json/parse-string keyword)
          :dataseries first :temp2m)})

(pco/defresolver temperature->is-cold?
  [{:keys [temperature]}]
  {:is-cold?
   (if (< temperature 15)
     true false)})

(pco/defresolver my-ip
  []
  {:ip
   (-> @(http/request
         {:headers {"Accept" "Application/json"}
          :url (str "https://ifconfig.me")})
       :body)})

(pco/defresolver my-ip2
  []
  {:ip
   (:ip tut-ip)})

(def env
  (pci/register [my-ip
                 my-ip2
                 ip->lat-long
                 lat-long->temperature
                 temperature->is-cold?]))

(comment

  (ip->lat-long tut-ip)
  (lat-long->temperature explico-coords)

  (-> (my-ip)
      ip->lat-long
      lat-long->temperature)

  (p.eql/process env {} [:temperature]))

(defn get-weather
  "Invoke me with clojure -X pathom3.pathom3/get-weather"
  [{:keys [ip]}]
  (println "exec with" ip))

(defn -main [ip]
  "Invoke me with clojure -M -m pathom3.pathom3 [ip]"
  (let [temp (p.eql/process-one env {:ip ip} :temperature)]
    (println "it's currently " temp "C at " (pr-str ip))))
