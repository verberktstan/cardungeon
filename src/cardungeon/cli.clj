(ns cardungeon.cli
  (:require [cardungeon.core :as game]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(defn- print-dungeon [dungeon]
  (-> dungeon
      (update :dungeon/discarded count)
      (update :dungeon/draw-pile count)
      println))

(defn -main [& _]
  (newline)
  (println "Welcome to Cardungeon!")
  (println "type 'exit' to stop the game.")
  (println "To play, enter the number of the dungeon room card to play it.")
  (loop [game (game/new-game)
         input ""]
    (let [parsed-input (edn/read-string input)
          new-game (or (game/play game parsed-input) game)
          new-game (cond-> new-game (game/room-cleared? new-game) game/deal)]
      (cond
        (#{"exit"} (str/lower-case input))
        (println "stopping game..")

        (game/finished? new-game)
        (do
          (println new-game)
          (println "You cleared the dungeon!"))

        :else
        (recur (doto (or new-game game) print-dungeon) (read-line))))))
