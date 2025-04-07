# exoscale-auth

Exoscale [API Request Signature](https://openapi-v2.exoscale.com/) implemented in Clojure.

## Usage

Given the following configuratin file `conf.edn`:

```clojure
{:exoscale-api-key "EXO…"
 :exoscale-api-secret "…"
 :exoscale-zone "ch-gva-2"}
```

```clojure
(ns http-requests.core
  (:require [clj-http.client :as client])
  (:require [clojure.data.json :as json])
  (:require [clojure.edn :as edn])
  (:gen-class))

(defn read-config [path]
  (let [data (slurp path)]
    (edn/read-string data)))

(defn exo-get [resource query]
  (let [conf (read-config "conf.edn")
        api-key (:exoscale-api-key conf)
        api-secret (:exoscale-api-secret conf)
        headers {:content-type "application/json"}
        auth-header (build-auth-header api-key api-secret "GET" resource {} query headers)
        headers (assoc headers :Authorization auth-header)
        url-func (exo-base-url-func {:exoscale-zone "ch-gva-2"})
        url (url-func resource)]
    (println url)
    (println auth-header)
    (client/get url {:headers headers})))
                   
(defn -main
  [& args]
  (let [conf (read-config "conf.edn")]
    (exo-get "v2/instance/25fd0b49-be79-44a4-81b8-282d1d609b8e" {})))
```
