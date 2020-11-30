(ns rainbowbamboo.sargam-layers
  (:require  [garden.core :refer [css]]
             [hiccup.page :refer [html5]]
             [rainbowbamboo.sargam-base :refer [rand-str]]))

;; These are a collection of functions for the creation of amp-story-layers.


(def sample-style [:h1 {:color "red"}])


;; How exciting, I made a function that returns a function
(defn layer-function [template]
  (fn [content class]
    [:amp-story-grid-layer {:template template
                            :class class}
     content]))

(defn new-layer-function [template]
  (fn [content class]
    [:story-grid-layer {:class [template class]}
     content]))

(defn no-display-layer [content]
  ((layer-function "nodisplay") content "invisible"))


(defn thirds-layer [content]
  ((layer-function "thirds") content "thirds-layer"))


(defn middle-layer [content]
  (thirds-layer [:section.middle-layer
                 {:grid-area "middle-third"} content]))

(defn lower-third-layer [content]
  (thirds-layer [:section.lower-third-layer
                   {:grid-area "lower-third"} content]))

;; What I want is for this function to be able to take
;; a content & option with default class.
(defn fill-layer [content]
  ((layer-function "fill") content "fill-layer"))


(defn vertical-layer [content]
  ((layer-function "vertical") content "vertical-layer"))

(defn bg-fill-layer [src alt]
  (fill-layer [:amp-img {:src src :alt alt :layout "fill"}]))

(defn video-bg-fill-layer [src loop delay class]
  (fill-layer [:amp-video {:src src
                           :loop loop
                           :autoplay true
                           :layout "fill"
                           :class class
                           :animate-in "fade-in"
                           :animate-in-delay delay}]))

;; Note that loop can be true, false or a number
;; https://amp.dev/documentation/components/amp-bodymovin-animation/
(defn middle-bodymovin-layer [src width height loop class]
  (middle-layer [:amp-bodymovin-animation {:layout "fixed"
                                           :width width
                                           :height height
                                           :src src
                                           :class class
                                           :loop loop}]))




;; See how we are defining the styles outside
;; any function that returns valid styles would do
(def sample-header-styles [:h1 {:color "white"
                                :background-color "#618FFF"}])


(defn lower-third-header-layer [header]
  (lower-third-layer [:h1 header]))


;; This function implements the ken-burns effect as described on https://amp.dev/documentation/examples/visual-effects/ken_burns/
;; it's a fill image with a pan and a zoom combined to create a woooosh
;; I think that it might create some sexy product shots.
(defn ken-burns-layer [d]
  (let [id (rand-str 12)
        {:keys [image-url zoom pan anim-duration]} d]
    [:amp-story-grid-layer {:template "fill"}
             [:div.img-container {:animate-in zoom
                                  :animate-in-duration anim-duration}
              [:amp-img {:id (str "img-" id)
                         :src image-url
                         :animate-in pan
                         :animate-in-duration anim-duration
                         :layout "fixed"
                         :width "1600"
                         :height "1200"}]]]))
