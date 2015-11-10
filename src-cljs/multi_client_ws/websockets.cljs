(ns multi-client-ws.websockets
  (:require [cognitect.transit :as t])) ; https://github.com/cognitect/transit-cljs

;; Next, we'll define an atom to hold our websocket channel and a couple of helpers for reading and writing the JSON encoded transit messages.
(defonce ws-chan (atom nil))
(def json-reader (t/reader :json))
(def json-writer (t/writer :json))

;; create function to handle received messages.
;; Accepts the callback handler, returns a _function_ that decodes the transit message and passes it to the handler.
(defn receive-transit-msg!
  [update-fn]
  (fn [msg]
    (update-fn
     (->> msg .-data (t/read json-reader)))))

;; Send messages to the socket if it's open
(defn send-transit-msg!
  [msg]
  (if @ws-chan
    (.send @ws-chan (t/write json-writer msg))
    (throw (js/Error. "Gashdarnitall, websocket is not available."))))

;; Creates a new websocket given the URL and the received message handler.
(defn make-websocket! [url receive-handler]
  (println "Trying to connect to the websocket, giving it all she's got!")
  (if-let [chan (js/WebSocket. url)]
    (do
      (set! (.-onmessage chan) (receive-transit-msg! receive-handler))
      (reset! ws-chan chan)
      (println "Righteous!Websocket established with: " url))
    (throw (js/Error. "Websocket connection failed :/ sorry"))))
