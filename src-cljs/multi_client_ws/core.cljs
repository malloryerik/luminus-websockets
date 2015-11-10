;; ````````````````````````````````````````````````````````````
;; ClojureScript with nREPL
;; ............................................................
;; To connect the IDE to a ClojureScript REPL make sure that you have the :nrepl-port 7002 key in your :figwheel config in project.clj. When Figwheel starts, it will open nREPL on the specified port. Once you connect to the nREPL then run the following commands to open the ClojureScript REPL:
;;
;; user> (use 'figwheel-sidecar.repl-api)
;; user> (cljs-repl) [So far this last part hasn't worked]
;; ............................................................

(ns multi-client-ws.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]])
  (:import goog.History))


;;````````````````````````````````````````````````````````````
;; COMPONENTS
;;............................................................

(def gyumie-stuff "Da sweet scents of touch")
(def click-count (reagent/atom 0)) ; reagent demo, in about-page
(defn counting-component []
  [:div
   "The atom " [:code "click-count"] " has value: "
   @click-count ". "
   [:input {:type "button" :value "Click me!"
            :on-click #(swap! click-count inc)}]])

;; using in gyumie-page
(defn timer-component []
  (let [seconds-elapsed (reagent/atom 0)]
    (fn []
      (js/setTimeout #(swap! seconds-elapsed inc) 1000)
      [:div
       "Seconds Elapsed: " @seconds-elapsed])))


;;...........................
;; shared-state input example
(defn atom-input [value]
  [:input {:type "text"
           :value @value
           :on-change #(reset! value (-> % .-target .-value))}])

(defn shared-state []
  (let [val (reagent/atom "foo")]
    (fn []
      [:div
       [:p "The value is now: " @val]
       [:p "Change it here: " [atom-input val]]])))


;; ..........
;; BMI STUFF
(def bmi-data (reagent/atom {:height 180 :weight 80}))

(defn calc-bmi []
  (let [{:keys [height weight bmi] :as data} @bmi-data
        h (/ height 100)]
    (if (nil? bmi)
      (assoc data :bmi (/ weight (* h h)))
      (assoc data :weight (* bmi h h)))))

(defn slider [param value min max]
  [:input {:type "range" :value value :min min :max max
           :style {:width "100%"}
           :on-change (fn [e]
                        (swap! bmi-data assoc param (.-target.value e))
                        (when (not= param :bmi)
                          (swap! bmi-data assoc :bmi nil)))}])

(defn bmi-component []
  (let [
        {:keys [weight height bmi]} (calc-bmi),
        [color diagnose] (cond
                          (< bmi 18.5) ["orange" "underweight"]
                          (< bmi 25) ["inherit" "normal"]
                          (< bmi 30) ["orange" "overweight"]
                          :else ["red" "obese"])]
    [:div
     [:h3 "BMI calculator"]
     [:div
      "Height: " (int height) "cm"
      [slider :height height 100 220]]
     [:div
      "Weight: " (int weight) "kg"
      [slider :weight weight 30 150]]
     [:div
      "BMI: " (int bmi) " "
      [:span {:style {:color color}} diagnose]
      [slider :bmi bmi 10 50]]]
    )
  )
;; !! END COMPONENTS
;; ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,


(defn nav-link [uri title page collapsed?]
  [:li {:class (when (= page (session/get :page)) "active")}
   [:a {:href uri
        :on-click #(reset! collapsed? true)}
    title]])


(defn navbar []
  (let [collapsed? (atom true)]
    (fn []
      [:nav.navbar.navbar-inverse.navbar-fixed-top
       [:div.container
        [:div.navbar-header
         [:button.navbar-toggle
          {:class         (when-not @collapsed? "collapsed")
           :data-toggle   "collapse"
           :aria-expanded @collapsed?
           :aria-controls "navbar"
           :on-click      #(swap! collapsed? not)}
          [:span.sr-only "Toggle Navigation"]
          [:span.icon-bar]
          [:span.icon-bar]
          [:span.icon-bar]]
         [:a.navbar-brand {:href "#/"} "multi-client-ws"]]
        [:div.navbar-collapse.collapse
         (when-not @collapsed? {:class "in"})
         [:ul.nav.navbar-nav
          [nav-link "#/" "Home" :home collapsed?]
          [nav-link "#/gyumie" "Gyuuumie!" :gyumie  collapsed?]
          [nav-link "#/about" "About" :about collapsed?]
          [nav-link "#/sticky" "Sticky?" :sticky collapsed?]
          [nav-link "#/mrgrey" "Mister Grey!" :mrgrey collapsed?]
          [nav-link "#/bmi" "Biggitup" :bmi collapsed?]
          ]
         ]
        ]
       ])))

;; ````````````````````````````````````````````````````````````
;;    PAGE FUNCTIONS
(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:h2.hotpink "The story of O See"]
     [:p "the story of Ohhhh"]
     [:br]
     [:p "more of it"]
     [:div
      [:div [counting-component]]
   [:p.someclass
    "I have " [:strong "bold"]
    [:span {:style {:color "red"}} " and red "] "text."]]
     ]]])


(defn home-page []
  [:div.container
   [:div.jumbotron
    [:div.wrappit
     [:h1 "Bienvenue"]
     [:p "Whoa!"]
     [:ul
      [:li "That's freaking fast!"]
      [:li "It's cool as hell"]
      [:li "It makes me really want to make stuff, because of the feedback."]]
     [:h3 "can a paragraph go inside of a list item?"]
     [:p "But where did it all go?"]
     [:p [:a.btn.btn-primary.btn-lg {:href "http://luminusweb.net"} "Learn more »"]]
     [:div.row
      [:div.col-md-12
       [:h2 "Welcome to ClojureScript"]]]
     (when-let [docs (session/get :docs)]
       [:div.row
        [:div.col-md-12
         [:div {:dangerouslySetInnerHTML
                {:__html (md->html docs)}}]]])]]])

(defn gyumie-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:h2.hotpink "Gyumie-ya!^^"]
     [:gimme gyumie-stuff]
     [:br][:br][:br]
     [:p "been touching for... "]
     [:div.hotpink [timer-component]]
     ]]])

(defn sticky-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:h2.hotpink "Sticky!"]
     [:p "mmmeowowoweeewa!"]]]])

(defn mrgrey-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:h2.hotpink "Mister Grey!"]
     [:p "Gimme..."]
     [:div [shared-state]]]]])

(defn bmi-page []
  [:div.container
   [:h3.hotpink "Biggie-up your Body Mass"]
   [:div [bmi-component]] ; compenent func is up top
   ]) 


;; ............................................................
;; PAGES VAR -- LIST PAGES HERE in map
;; ALSO SET ROUTE BELOW
(def pages
  {:home #'home-page
   :about #'about-page
   :gyumie #'gyumie-page
   :sticky #'sticky-page
   :mrgrey #'mrgrey-page
   :bmi #'bmi-page
   })

(defn page []
  [(pages (session/get :page))])



;; ````````````````````````````````````````````````````````````
;;    ROUTES
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :page :home))

(secretary/defroute "/about" [] 
  (session/put! :page :about))

(secretary/defroute "/gyumie" []
  (session/put! :page :gyumie))
;; maybe this is the missing link for putting in a new route?
(secretary/defroute "/sticky" [] 
  (session/put! :page :sticky))

(secretary/defroute "/mrgrey" [] 
  (session/put! :page :mrgrey))

(secretary/defroute "/bmi" []
  (session/put! :page :bmi))
;; ..................................................
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
        (events/listen
          EventType/NAVIGATE
          (fn [event]
              (secretary/dispatch! (.-token event))))
        (.setEnabled true)))

;;..................................................
;; Initialize app
(defn fetch-docs! []
  (GET (str js/context "/docs") {:handler #(session/put! :docs %)}))

(defn mount-components []
  (reagent/render [#'navbar] (.getElementById js/document "navbar"))
  (reagent/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))







