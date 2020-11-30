(ns sargam.meditation
  (:require [rainbowbamboo.sargam-base :refer [base amp-page rand-str]]
            [rainbowbamboo.sargam-layers :as layers]
            [rainbowbamboo.export.standard :as export]
            [portal.api :as p]))


;; INTENTION
;; This is meant to be the bounded context of meditation stories.
;; Comparable to the bounded context of how-to stories.
;; Or the bounded context of your dreams.
;;
;; The idea is that this namespace, and others like this will
;; use and remix the components avaliable in the others to create
;; a particular "type" of story that has a defined form.
;;
;; In the case of the How-To context, those stories might be concerned
;; with some pattern for taking a series of steps and producing
;; numbered pages interjected with title cards.
;;
;; In the case of this meditation context, we're concerned with creating
;; a simple meditation tool where you can
;; - select how long you want to meditate for
;; - hear some sort of background noise for your chosen duration
;; - possibly some voice guidance
;; - hear an end-tone
;;
;; The format I'm going with is having different locations with different stock
;; footage and sounds. The idea is that you can take a break in nature and breathe.
;; With that in mind, this is the data we'll need before we start to code:
;;
;; 1- Base config with the title of the page, fonts to import through google fonts,
;;    and some seo/amp specific info
(def meditation-config
  {:title "Pine.RIP Meditation"
   :description "Free meditation app with nature sounds"
   :font-list "Inconsolata"
   :logo-src "/img/logo.png"
   :publisher "Pine.RIP"
   :poster-src "/img/logo.png"
   :poster-square-src "/img/logo.png"
   :poster-landscape-src "/img/logo.jpg"
   :landscape true})



;; 2- Global styles this is our method of including css,
;;   it's garden syntax. There are also facilities for adding classes
;;   to your layers. The intention is to add custom styling by adding your
;;   custom classes as necessary, and then generating styles to be injected here.
(def meditation-styles
  [[:* {:font-family "Inconsolata"}]
   [".invisible" {:visibility "hidden"}]
   [".middle-layer"  {:margin "auto"}]
   ["h2" {:background-image "url('/img/patterns/shley-tree-2.png')"
          :color "white"}]])


;; 3- What is a Meditation? This is our domain specific definition
;;   in this case, all we're saying is that our meditations have
;;   a title, a slug, and a duration in seconds.
(def meditation-config [{:title "5 minutes"
                         :url-slug "5-mins/"
                         :duration "300s"}
                        {:title "10 minutes"
                         :url-slug "10-mins/"
                         :duration "600s"}
                        {:title "15 minutes"
                         :url-slug "15-mins/"
                         :duration "900s"}
                        {:title "30 minutes"
                         :url-slug "30-mins/"
                         :duration "1800s"}
                        {:title "60 minutes"
                         :url-slug "60-mins/"
                         :duration "3600s"}])

;; 4- A Meditation theme, the first of many I hope.
;;   if we wanted to expand and include more assets,
;;   we might start by adding a entry to here.
(def camp-assets {:heading "Camp with me..."
                  :audio-url "/audio/meditation/rainy-camp.mp3"
                  :video-url "/video/meditation/rainy-camp.mp4"
                  :end-message "I hope that this was helpful ^_^"
                  :end-tone-url "/audio/meditation/magic-sound.mp3"})

(def fire-assets {:heading "Let's take a second together"
                  :bitmoji "/img/bitmoji/ada.png"
                  :audio-url "/audio/meditation/camfire.mp3"
                  :video-url "/video/meditation/fireside.mp4"
                  :end-message "I hope that this was helpful ^_^"
                  :end-tone-url "/audio/meditation/magic-sound.mp3"})

;; 5- The sitemap with urls, which I'm choosing to generate within this
;;    context, but I believe is hardcoded in the how-to
(defn create-sitemap [d path]
  (let [{:keys [title url-slug]} d]
    {:title title
     :url (str path url-slug)}))

(def camp-sitemap
  (map #(create-sitemap % "/meditation/camping/") meditation-config))


;; END DATA

;; You can imagine that each of the data defined can be swapped out
;; for api calls or an event stream.

;; In order to create  meditation stories from the data,
;; we first transform it into a form we can manipulate.

;; We ultimately want a path (which can be later appended to a root-path) and a collection of pages
;; in this meditation context it's hardcoded to always have three pages per story
;; an :intro, a :meditation and an :end
;; [ In the context of a how-to story, we might have a generator in here that
;; creates one page per step. ]
;;
;; We're defining the pages to have a :kind and a map of :assets
;; the :kind represents the type of page that it is
;; the :assets are any data that is needed for that page to create its layers
(defn meditation-data-transformer [assets config other-pages]
  (let [{:keys [heading audio-url video-url end-tone-url bitmoji]} assets
        {:keys [title url-slug duration]} config]
    {:path url-slug
     :pages [{:kind :intro
              :assets {:title heading
                       :time duration
                       :bitmoji bitmoji
                       :other-pages other-pages}}
             {:kind :meditation
              :assets {:duration duration
                       :audio-url audio-url
                       :video-url video-url}}
             {:kind :end
              :assets {:end-tone-url end-tone-url}}]}))


;; Now that we know the "shape" of the pages
;; the next step is really to create all the helpers that we think we'll need
;; for the story. These are functions that take in data, and then
;; return some bit of hiccup that's a component on a page.
;; They may be regular html as in the other-page-generator
;; or they may use functions from the layers namespace to build up custom
;; layers for combination.



;; This is an example of a helper that just creates a list of
;; links.
(defn other-page-generator [d]
  [:ul.time-list
   (for [page d]
     (let [{:keys [title url]} page]
       [:li [:a {:href url} title]]))])


;; This is an example of redefining the amp-page function from base
;; which does the step of taking a series of layers and returning
;; a :amp-story-page that we can use to create stories.
;; This is actually an unnecesary definition because we're not doing anything
;; different right now, but we could edit this as necessary.
(defn intro-page [layers]
  (let [id (rand-str 12)]
    [:amp-story-page {:id id}
            (for [l layers]
              l)]))


;; This is another example of redefining amp-page except this one takes a
;; config from which we extract an audio-url and a duration.
;; We can now return pages which have background audio, and auto-advance
;; after a set time.
(defn meditation-page [layers config]
  (let [id (rand-str 12)
        {:keys [duration audio-url]} config]
    [:amp-story-page {:id id
                      :auto-advance-after duration
                      :background-audio audio-url}
     (for [l layers]
       l)]))


;; Finally these are the three functions, one per type of page
;; which will take some set of assets, and return a valid amp-story-page
;; Note that since a page is, itself a collection of layers,
;; the core of these functions are those vectors of layers.
;; The order of the vector matters since layers are stacked
;; on top of each other.

(defn intro [assets]
  (intro-page [(layers/bg-fill-layer "/img/meditation/pine-forest.jpg" "pine trees")
               (layers/lower-third-layer [:div [:img {:src "/img/bitmoji/ada.png" :alt "Bitmoji Me"}]
                                          [:h2 (:title assets)]])]))

(defn meditation [assets]
  (meditation-page [(layers/video-bg-fill-layer (:video-url assets) true "0s" "bg-video")]
                   assets))

;; This one just uses the generic amp-page
(defn end [assets]
  (amp-page [(layers/no-display-layer [:amp-audio.invisible
                                       {:src (:end-tone-url assets)
                                        :autoplay true}
                                       [:source {:type "audio/mpeg"
                                                 :src (:end-tone-url assets)}]])
             (layers/video-bg-fill-layer "/video/meditation/pine-drone.mp4" false "0s" "bg-video")
             (layers/lower-third-layer [:h2 "I hope that this was helpful :)"])]))




;; Given the story structure as defined above, we now do the step of
;; generating a valid amp-story, ie a path that we can serve it from
;; and the html to display to a user.
;; Recall that a story is a collection of pages,
;; each of those pages have a :kind and some associated assets.
;; We can use a `case` to call a different function
;; per kind of page. Each individual function is expecting
;; a map of assets, but the specific assets in that map
;; are up to us to define.


(defn meditation-story-generator [story config styles]
         {:path (:path story)
          :html-page (base config
                           (for [page (:pages story)]
                             (let [{:keys [kind assets]} page]
                               (case kind
                                 :intro (intro assets)
                                 :meditation (meditation assets)
                                 :end (end assets)
                                 [])))
                           styles
                           )})


;; This is the function that actually exports and creates html files
;; it's using the export namespace which uses java.io, but I think
;; everything else can be used from clojurescript.
(defn meditation-amp-story-exporter! [story-html root-path]
  (map #(export/export-amp root-path %) story-html)
  story-html)



;; This fuction will ultimately create a tree of meditation amp-stories
;; that is then exported using meditation-amp-story-exporter
;; To determine this we're giving it
;; 1. Assets, ie what's the themed media
;; 2. Config, some configuration like the url
;; 3. A root-path
;; 4. A sitemap
;;
;; Note the first step of creating the story-data.
;; We're mapping one combination of the assets, root-page  and sitemap onto
;; each of the individual configs (the title, duration, url-slug structure)
;; this allows us to create variants based on different lengths of meditations
;; but we can just as easily use this approach to create variants based
;; on different asset groups.
(defn create-meditate [assets configs root-path sitemap styles]
  (let [story-data (map #(meditation-data-transformer assets % sitemap) configs)
        story-html (map #(meditation-story-generator % configs styles) story-data)]
    (tap> story-data)
    (meditation-amp-story-exporter! story-html root-path)))

(tap> meditation-config)

(defn -main []
  (create-meditate camp-assets meditation-config "/export/" camp-sitemap meditation-styles))

(create-meditate camp-assets meditation-config "meditation/" camp-sitemap meditation-styles)
