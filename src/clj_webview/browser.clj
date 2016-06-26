(ns clj-webview.browser)
(import javafx.application.Application)
(import javafx.fxml.FXMLLoader)
(import javafx.scene.Parent)
(import javafx.scene.Scene)
(import javafx.stage.Stage)
(import java.net.URL)
(import java.io.File)

(gen-class
  :extends javafx.application.Application
  :name hk.molloy.Browser
  )

;(require '[clojure.java.io :as io])

(defn -start [this stage]
  (let [
         root (FXMLLoader/load (-> "resources/WebUI.fxml" File. .toURI .toURL))
         scene (Scene. root)
         ]
    (.setScene stage scene)
    (.show stage)
    ))
