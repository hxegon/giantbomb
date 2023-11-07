(ns backend.utils
  (:require [clojure.string :as cs]
            [clojure.walk :as walk]))

(defn kebab-keywordize
  "Identity for keywords. Otherwise trims, and converts strings to lowercase /
  kebab case keyword"
  [x]
  (if (keyword? x)
    x
    (-> x
        cs/lower-case
        cs/trim
        (cs/replace #"[\s_]+" "-")
        keyword)))

(comment
  (kebab-keywordize "Hello World")
  (kebab-keywordize :Hello-World)
  (kebab-keywordize "Hello_World"))

(defn kebab-keywordize-keys
  "deeply transform map keys from strings to lowercase, kebab-case keywords.
  Any non-string key is left alone."
  [m]
  (let [entry-f (fn [[k v]] [(if (string? k) (kebab-keywordize k) k) v])
        map-f   (fn [v] (if (map? v) (into {} (map entry-f) v) v))]
    (walk/postwalk map-f m)))

(comment
  (kebab-keywordize-keys {"hello" {:foo      1
                                   "bar bar" {"BAA" 2}}
                          "world" {"buz" 3}
                          4       5}))

(defn validator
  "takes a list of [pred fail-msg], where pred is a function that returns
  a truthy value if x is valid, and returns a function that returns a seq
  of validation fail-msgs for x"
  [& validators]
  (fn [x]
    (keep (fn [[pred fail-msg]]
            (when-not (pred x) fail-msg))
          validators)))

(comment
  ((validator [odd? "not odd"] [#(> % 5) "wasn't greater than 5"])
   7))

(defn- error-kw?
  [kw]
  (when
   (keyword? kw)
    (or (= :error kw) (= "error" (namespace kw)))))

(comment
  (error-kw? :foo)
  (error-kw? :error)
  (error-kw? :error/foo))

(defn errors
  "Returns a list of :error or :error/* entries of a map"
  [x]
  (when (map? x)
    (into {} (filter (comp error-kw? key)) x)))

(comment
  (errors {:error "Something" :a 1})
  (errors {:a 1})
  (errors {:error/code "22" :a 5}))
