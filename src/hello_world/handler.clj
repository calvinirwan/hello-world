(ns hello-world.handler
  (:use compojure.core
        [ring.adapter.jetty :only (run-jetty)])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as response]))

(defroutes app-routes
  ;; (GET "/manga/:name/:chapter" [name chapter] (str "<h1>Manga   " name " chapter " chapter "</h1>"))

  ;; (GET "/:foo/:name" { {foo :foo name :name} :params}
  ;;      (str "foo = "foo" name = "name ))
  (GET "/:foo" [foo]
  (str "Foo whsdadasat the foo = " foo))
  (route/resources "/")
  (route/not-found "Not Found"))

(defroutes my-3-parameter-route
  (GET "/:my" [x y z]
       (str "x -> " x " ; "
            "y -> " y " ; "
            "z -> " z " ; ")
))

(defroutes my-2-parameters-and-remainder-route
  (GET "/:my" [x y & z]
       (str "x -> " x " ; "
            "y -> " y " ; "
            "z -> " z )
       )
)

(defroutes my-remainder-symbol-route 
  (GET "/:my" [x y :as r]
       (str "x -> " x "; "
            "y -> " y "; "
            "r -> " r)
                                     
))

(defroutes my-destructuring-map-route
  (GET "/:my" [x y :as {u :uri}]
       (str "x -> " x "; "
            "y -> " y "; "
            "u -> " u)
))

(def my-request {:request-method :get
                 :uri "/my-uri"
                 :headers []
                 :params {:x "foo" :y "bar" :z "baz" :w "qux"}})

(def ^:private counter (atom 0))

(def ^:private mappings (ref {}))

(defn url-for
  [id]
  (@mappings id)
)

(defn shorten!
  ([url]
     (let [id (swap! counter inc)
           id (Long/toString id 36)]
       (or (shorten! url id)
           (recur url))))
  ([url id]
     (dosync 
      (when-not (@mappings id)
        (alter mappings assoc id url)
        id)))
)

(defn retain 
  [& [url id :as args]]
  (if-let [id (apply shorten! args)]
    {:status 201
     :headers {"location" id}
     :body (list "URL " url "assigned the short identifier " id)}
    {:status 409 :body (format "Short URL %s is already taken" id)}
))

(defn redirect
  [id]
  (if-let [url (url-for id)]
    (response/redirect url)
    {:status 404 :body (str "No such short URL nyeet etyey : " id)})
)

(defn plus [x]
  (+ x x))

(defroutes app*
  (GET "/" request "Welcome!")
  (PUT "/:id" [id url] (retain url id))
  (POST "/" [url] (retain url))
  (GET "/:id" [id] (redirect id))
  (GET "/list/" [] (interpose "\n" (keys @mappings)))
  (route/not-found "Sorry, there's nothing here nyeet."))

(retain ["/calfang" "cal" ])

(def app
  (handler/site app*))



