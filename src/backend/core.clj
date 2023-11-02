(ns backend.core
  (:require [ring.adapter.jetty :as jetty]
            [muuntaja.core :as muu]
            [reitit.ring :as ring]
            [reitit.ring.middleware.parameters :as ring-params]
            [reitit.ring.coercion :as ring-coerce]
            [reitit.ring.middleware.muuntaja :as ring-muu]))

(defn test-handler
  [_req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Hello World"})

(def backend
  (ring/ring-handler
   (ring/router
    [["/" {:get {:handler test-handler}}]
     ;; No favicon, return empty response to prevent server error
     ["/favicon.ico" {:get {:handler (fn [_] {})}}]]
    {:data {:muuntaja   muu/instance
            :middleware [ring-params/parameters-middleware
                         ring-coerce/coerce-request-middleware
                         ring-muu/format-response-middleware
                         ring-coerce/coerce-response-middleware]}})))

(defn -main
  [& _args]
  (jetty/run-jetty backend {:port 3001}))
