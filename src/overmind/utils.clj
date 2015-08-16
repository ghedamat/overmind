(ns overmind.utils)


(defn format-space-watcher
  [event]
  (let [level (-> event :payload :level)
        dev (->> event :payload :dev)]
    (cond
      (= level "danger") (str "Danger space level on dev: " dev)
      (= level "warning")(str "Warning space level on dev: " dev))))

(defn format-path-watcher
  [event]
  (let [action(-> event :payload :event)
        filename (->> event :payload :filename)]
    (cond
      (= action :create) (str "New file/directory found: " filename)
      (= action :delete) (str "File/directory moved: " filename))))

(defn format-event
  [event]
  (case (:name event)
    :path-watcher (format-path-watcher event)
    :space-watcher (format-space-watcher event)))
