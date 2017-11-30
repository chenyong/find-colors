
(ns server.main
  (:require ["walk-sync" :as walk-sync] [clojure.string :as string] ["fs" :as fs]))

(defn main! []
  (let [base-dir js/process.env.base
        paths (filter
               (fn [filepath] (string/ends-with? filepath ".tsx"))
               (walk-sync base-dir))
        *colors (atom [])]
    (doseq [filepath paths]
      (let [content (fs/readFileSync (str base-dir filepath) "utf8")]
        (doseq [line (string/split-lines content)]
          (if (string/includes? line "#")
            (let [color (re-find (re-pattern "#[0-9a-f]{3,6}") line)]
              (if (some? color) (swap! *colors conj color) (println line)))))))
    (println (string/join "\n" (sort (set @*colors))))))

(defn reload! [] (main!))
