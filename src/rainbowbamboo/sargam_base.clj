(ns rainbowbamboo.sargam-base
  (:require
   [garden.core :refer [css]]
   [hiccup.page :refer [html5]]
   [portal.api :as p]))

;; This file contains the scaffolding for creating amp web stories.
;; The intention is that if we supply the amp-story with amp-story-pages and amp-story-layers,
;; it will wrap it in the appropriate boilerplate (importing the 3rd party js and css)
;; and return a valid html5 page that can be rendered directly or then fed into an amp-story-player


;; (p/open)
;; (p/tap)

;; List / S-expression
(* 1 3)

;; Vector
(last ["pineapple" "mango" "orange" (+ 1 1)])

;; Map
(:title {:author "N. K. Jemisim" :title "The Stone Sky"})




;; Note, uuids can't be css ids because uuids can sometimes start with a number
;; but css ids can never start with a number.


(defn rand-str
  "Given an integer length, returns a random string of uppercase letters we can use as ids"
  [length]
  (apply str (repeatedly length #(char (+ (rand-int 26) 65)))))

(apply str (repeatedly 10 #(char (+ (rand-int 26) 65))))



;; These are the hardcoded amp specific css and js imports
(def amp-css-boilerplate
  "body{-webkit-animation:-amp-start 8s steps(1,end) 0s 1 normal both;-moz-animation:-amp-start 8s steps(1,end) 0s 1 normal both;-ms-animation:-amp-start 8s steps(1,end) 0s 1 normal both;animation:-amp-start 8s steps(1,end) 0s 1 normal both}@-webkit-keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}@-moz-keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}@-ms-keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}@-o-keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}@keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}")

(def amp-css-noscript-boilerplate
  "body{-webkit-animation:none;-moz-animation:none;-ms-animation:none;animation:none}")

;; This is a handy little function that allows us to import new amp-components by adding their name and src
;; eg.  <script async custom-element="amp-bodymovin-animation" src="https://cdn.ampproject.org/v0/amp-bodymovin-animation-0.1.js"></script>
;; is: (import-amp-component "amp-bodymovin-animation" "https://cdn.ampproject.org/v0/amp-bodymovin-animation-0.1.js" )
(defn import-amp-component
  [element-name src]
  [:script {:async true :custom-element element-name :src src}])

(html5 (import-amp-component "amp-bodymovin-animation" "https://cdn.ampproject.org/v0/amp-bodymovin-animation-0.1.js"))

;; This will create the hiccup for a valid amp-story page.
;; Note that this base also includes the amp-audio import which is not strictly necessary.
(defn base [config pages styles]
  (let [{:keys [title description font-list publisher logo-src poster-src poster-square-src landscape poster-landscape-src]} config]
    (html5 {:amp true  :lang "en"}
           [:head
            [:meta {:charset "utf-8"}]
            [:meta {:name "viewport" :content "width=device-width,minimum-scale=1,initial-scale=1"}]
            [:meta {:name "description" :content description}]
            [:link {:rel "preload" :as "script" :href "https://cdn.ampproject.org/v0.js"}]
            [:link {:rel "preconnect dns-prefetch" :href "https://fonts.gstatic.com/" :crossorigin true} ]
            [:script {:async true :src "https://cdn.ampproject.org/v0.js"}]
            (import-amp-component "amp-story" "https://cdn.ampproject.org/v0/amp-story-1.0.js")
            (import-amp-component "amp-audio" "https://cdn.ampproject.org/v0/amp-audio-0.1.js") ;; optional
            [:link {:href (str "https://fonts.googleapis.com/css?family=Inconsolata:wght@757" font-list) :rel "stylesheet"}] ;; optional
            [:link {:rel "canonical" :href "."}]
            [:title title ]
            [:style {:amp-custom true}
             (for [s styles]
               (css s))]
            [:style {:amp-boilerplate true}
             amp-css-boilerplate]
            [:noscript
             [:style {:amp-boilerplate true}
              amp-css-noscript-boilerplate]]]
           [:body
            [:amp-story {:standalone true
                         :publisher publisher
                         :title title
                         :supports-landscape landscape
                         :publisher-logo-src logo-src
                         :poster-portrait-src poster-src
                         :poster-square-src poster-square-src
                         :poster-landscape-src poster-landscape-src}
             (for [p pages]
               p)]])))


;; This is a sampe config for the base
(def sample-config {:title "Pine.RIP"
                    :description "What a wonderful day"
                    :font-list "Inconsolata"
                    :logo-src "/img/fake-moth-closed.jpg"
                    :publisher "Pine.RIP"
                    :poster-src "/img/fake-moth-closed.jpg"
                    :poster-square-src "/img/fake-moth-closed.jpg"
                    :landscape false
                    :poster-landscape-src "/img/fake-moth-closed.jpg"})


(def sample-styles [[:* {:font-family "Inconsolata"}]
                    [:amp-story-grid-layer {:background-color "red"}]])


(def sample-pages
  [[:amp-story-page
    [:amp-story-grid-layer
     [:div.classy-class "Hello World"]]]
   [:amp-story-page
    [:amp-story-grid-layer
     [:div.classy-class "Goodbye World"]]]])


;; This is the base `<amp-page>` tag with a randomly generated id.
;; It assumes a vector of [:amp-story-grid-layer ... ]
;; Note the `for` step that allows us to destructure and render
;; each story-layer. Otherwise the page would load as
;; <html> <amp-story> <amp-page>
;; [:amp-story-grid-layer {:template "thirds"}
;; [:div.content {:grid-are "lower-third"} "Hello Header"]]
;; </html> </amp-story> </amp-page>
;;
;; NOTE: Maybe we should be passing in a custom id in order to name it?
(defn amp-page [layers]
  (let [id (rand-str 12)]
    [:amp-story-page {:id id}
            (for [l layers]
              l)]))

(def sample-layers [[:amp-story-layer [:h1 "Hello"]]
                    [:amp-story-layer [:h2 "World"]]])

