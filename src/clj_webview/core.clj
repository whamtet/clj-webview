(ns clj-webview.core)

(import javafx.application.Application)
(import javafx.application.Platform)
(import javafx.scene.web.WebView)
;(import hk.molloy.MyApplication)
(import netscape.javascript.JSObject)
(import javafx.beans.value.ChangeListener)
(import javafx.concurrent.Worker$State)
(import WebUIController)
(import MyEventDispatcher)

;;launch calls the fxml which in turn loads WebUIController
(defonce launch (future (Application/launch hk.molloy.Browser (make-array String 0))))

(defmacro run-later [& forms]
  `(let [
          p# (promise)
          ]
     (Platform/runLater
       (fn []
         (deliver p# (try ~@forms (catch Throwable t# t#)))))
     p#))

(def url "https://app.sli.do/event/dhsnnxay/ask")

(defonce webengine (do
                     (Thread/sleep 1000)
                     WebUIController/engine
                     #_@(run-later (.getEngine (WebView.)))))

(def webview WebUIController/view)

(defn execute-script [s]
  (run-later
    (let [
           result (.executeScript webengine s)
           ]
      (if (instance? JSObject result)
        (str result)
        result))))

(defn inject-firebug []
  (execute-script (slurp "js-src/inject-firebug.js")))

(defn execute-script-async [s]
  (let [
         p (promise)
         *out* *out*
         ]
    (Platform/runLater
      (fn []
        (let [
               o (.executeScript webengine "new Object()")
               ]
          (.setMember o "cb" (fn [s] (deliver p s)))
          (.setMember o "println" (fn [s] (println s)))
          (.eval o s))))
    @p))

(defn repl []
  (let [s (read-line)]
    (when (not= "" (.trim s))
      (println @(execute-script s))
      (recur))))

(defn bind [s obj]
  (run-later
    (.setMember
      (.executeScript webengine "window")
      s obj)))

(defonce cookie-manager
  (doto (java.net.CookieManager.)
    java.net.CookieHandler/setDefault))

(defn clear-cookies []
  (-> cookie-manager .getCookieStore .removeAll))

(defn async-load [url]
  (let [
         p (promise)
         f (fn [s]
             (binding [*out* *out*] (println s)))
         listener (reify ChangeListener
                    (changed [this observable old-value new-value]
                             (when (= new-value Worker$State/SUCCEEDED)
                               ;first remove this listener
                               (.removeListener observable this)
                               ;and then redefine log and error (fresh page)
                               (bind "println" f)
                               (future
                                 (Thread/sleep 1000)
                                 (execute-script "console.log = function(s) {println.invoke(s)};
                                                 console.error = function(s) {println.invoke(s)};
                                                 "))
                               (deliver p true))))
         ]
    (run-later
      (doto webengine
        (-> .getLoadWorker .stateProperty (.addListener listener))
        (.load url)))
    @p
    ))

(defn back []
  (execute-script "window.history.back()"))

;over riding URL handlers
(import sun.net.www.protocol.http.Handler)
(import sun.net.www.protocol.http.HttpURLConnection)
(import java.net.URL)
(import java.net.URLStreamHandler)
(import java.net.URLStreamHandlerFactory)
(import java.net.URLStreamHandler)

(defn my-connection-handler [protocol]
  (proxy [Handler] []
    (openConnection [& [url proxy :as args]]
                    (println args)
                    #_(HttpURLConnection. url proxy))))

(defonce stream-handler-factory
  (URL/setURLStreamHandlerFactory
    (reify URLStreamHandlerFactory
      (createURLStreamHandler [this protocol] (#'my-connection-handler protocol)))))
