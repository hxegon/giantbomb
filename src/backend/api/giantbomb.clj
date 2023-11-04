(ns backend.api.giantbomb
  (:require [clojure.data.json :as json]
            [clojure.string :as cs]
            [clj-http.client :as http]
            [backend.api.utils :as u]))

(def ^:private api-key
  ;; FIXME: read from env var instead
  "f8533635eb99850dea55ce9fb56ea16db0fbd2ae")

(def ^:private api-base-url
  "http://www.giantbomb.com/api/")

(def ^:private user-agent
  "curl/7.350")

(def ^:private endpoints
  {:search (str api-base-url "search")})

(def ^:private resources
  #{:game})

(defn- ->field-name
  "coerce into string formatted like a GB field name.
  i.e. :thumb-url    -> \"thumb_url\"
       \"Thumb URL\" -> \"thumb_url\"
  returns nil if it doesn't know how to convert"
  [x]
  (-> (name x)
      (cs/lower-case)
      (cs/trim)
      (cs/replace #"[\s-]+" "_")))

;; Really should have skipped straight to malli for this
(def ^:private api-call-validator
  (u/validator [#(or (:endpoint %) (endpoints (:type %)))
                "No :endpoint or matching :type found"]

               [#(resources (:resource %))
                "Must specify a resource"]))

(defn- call-api
  [{:keys [endpoint] :as params}]
  (if-let [validation-errors (not-empty (api-call-validator params))]
    {:error/invalid validation-errors}
    (let [req {:accept       :json
               :headers      {"User-Agent" user-agent}
               :query-params (merge {"api_key" api-key
                                     "format"  "json"}
                                    (dissoc params :endpoint))}]
      (-> (http/get endpoint req)
          :body
          json/read-str
          u/kebab-keywordize-keys))))

(defn search
  ;; TODO: add docstring
  ([resource query] (search resource query {}))
  ([resource query {:keys [field-list] :as _opts}]
   (call-api {:endpoint   (:search endpoints)
              :query      query
              :resource   resource
              :field-list (cs/join "," (mapv ->field-name field-list))})))
