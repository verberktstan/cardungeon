(ns cardungeon.cli
  (:require [cardungeon.core :as game]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(defn -main [& _]
  (newline)
  (println "Welcome to Cardungeon!")
  (println "type 'exit' to stop the game.")
  (println "To play, enter the number of the dungeon room card to play it.")
  (loop [game {:player/health 13
               :dungeon/room
               {0 {:card/monster 2}
                1 {:card/monster 3}}}
         input ""]
    (let [parsed-input (edn/read-string input)
          new-game (some-> game (game/play parsed-input))]
      (cond
        (#{"exit"} (str/lower-case input))
        (println "stopping game..")

        (game/finished? new-game)
        (println "You cleared the dungeon!")

        :else
        (recur (doto (or new-game game) println) (read-line))))))
