(ns cardungeon.cli
  (:require [cardungeon.core :as game]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(defn- print-dungeon [{:player/keys [health]
                       :dungeon/keys [room]}]
  (newline)
  (println "Health:" health)
  (doseq [[i {:card/keys [monster potion]}] (sort-by key room)]
    (println i
     (cond
       monster (str "Monster(" monster ")")
       potion (str "Potion(" potion ")")))))

(defn -main [& _]
  (newline)
  (println "Welcome to Cardungeon!")
  (println "type 'exit' to stop the game.")
  (println "type 'skip' to skip the current room.")
  (println "To play, enter the number of the dungeon room card to play it.")
  (loop [game (game/new-game)
         input ""]
    (let [exit? (#{"exit"} (str/lower-case input))
          skip? (#{"skip"} (str/lower-case input))
          parsed-input (edn/read-string input)
          skipped-game (and skip? (game/skip game))
          played-game (game/play game parsed-input)
          new-game (or skipped-game played-game game)
          room-cleared? (game/room-cleared? new-game)
          new-game (cond-> new-game room-cleared? game/deal)
          dead? (game/dead? new-game)]
      (when (and skip? (not skipped-game))
        (println "Cannot skip this room!"))
      (when skipped-game
        (println "Skipping this dungeon room! Prepare for the next dungeon room.."))
      (when (and room-cleared? (not skipped-game) (not dead?))
        (println "Dungeon room cleared! Entering the next dungeon room.."))
      (cond
        exit?
        (println "stopping game..")

        dead?
        (println "You didn't survive the dungeon!")

        (game/finished? new-game)
        (println "You cleared the dungeon!")

        :else
        (recur (doto (or new-game game) print-dungeon) (read-line))))))
