(ns exoscale-auth.core
  (:require [clojure.data.json :as json])
  (:require [clojure.string :as str])
  (:import java.util.Base64)
  (:import javax.crypto.spec.SecretKeySpec)
  (:import javax.crypto.Mac))

(defn base64-encode [payload]
  (.encode (Base64/getEncoder) payload))

(defn hmac-sha256 [secret data]
  (let [algo "HmacSHA256"
        secret-utf8 (.getBytes secret "UTF-8")
        data-utf8 (.getBytes data "UTF-8")
        hmac-key (SecretKeySpec. (byte-array secret-utf8) algo)
        hmac (doto (Mac/getInstance algo) (.init hmac-key))]
    (.doFinal hmac (byte-array data-utf8))))

(defn build-signature [api-secret method path body query headers expires]
  (let [l1 (format "%s /%s" method path)
        l2 (json/write-str body)
        l3 (str/join (vals query))
        l4 (json/write-str headers)
        l5 (format "%d" expires)
        message (str/join (interpose "\n" [l1 l2 l3 l4 l5]))
        signature (base64-encode (hmac-sha256 api-secret message))]
    (str/join (map char signature))))

(defn build-auth-header [api-key api-secret method path body query headers]
  (let [expires (+ (quot (System/currentTimeMillis) 1000) 600)
        signature (build-signature api-secret method path body query headers expires)
        signed-query-args (str/join (interpose ";" (keys query)))]
    (format "%s credential=%s,signed-query-args=%s,expires=%d,signature=%s"
            "EXO2-HMAC-SHA256"
            api-key
            signed-query-args
            expires
            signature)))
