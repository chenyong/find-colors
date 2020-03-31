
(ns server.main
  (:require ["walk-sync" :as walk-sync]
            [clojure.string :as string]
            ["fs" :as fs]
            ["path" :as path]
            ["color" :as Color]))

(defn main! []
  (let [base-dir (or js/process.env.base "")
        paths (filter
               (fn [filepath] (string/ends-with? filepath ".tsx"))
               (walk-sync base-dir))
        *colors (atom [])]
    (doseq [filepath paths]
      (let [content (fs/readFileSync (path/join base-dir filepath) "utf8")]
        (doseq [line (string/split-lines content)]
          (if (string/includes? line "#")
            (let [color (re-find (re-pattern "#[0-9a-f]{3,6}") line)]
              (if (some? color) (swap! *colors conj color) (comment println line))))
          (if (string/includes? line "hsla")
            (let [color (re-find #"hsla\(\d+\,\s*\d+%\,\s*\d+%\,\s*\d+(\.\d*)?\)" line)]
              (if (some? color) (swap! *colors conj (first color)) (comment println line))))
          (if (string/includes? line "hsl(")
            (let [color (re-find #"hsl\(\d+\,\s*\d+%\,\s*\d+%\)" line)]
              (if (some? color) (swap! *colors conj color) (comment println line))))
          (if (string/includes? line "rgba")
            (let [color (re-find #"rgba\(\d+\,\s*\d+\,\s*\d+\,\s*\d+(\.\d+)?\)" line)]
              (if (some? color) (swap! *colors conj (first color)) (comment println line))))
          (if (string/includes? line "rgb(")
            (let [color (re-find #"rgb\(\d+\,\s*\d+\,\s*\d+\)" line)]
              (if (some? color) (swap! *colors conj color) (comment println line)))))))
    (comment println @*colors)
    (println
     (->> @*colors
          (map (fn [x] (str (-> (Color x) .hex) "\t" (-> (Color x) .hsl .round) "\t" x)))
          sort
          distinct
          (string/join "\n")))))

(defn reload! [] (main!))
