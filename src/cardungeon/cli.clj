(ns cardungeon.cli
  (:require [cardungeon.dungeon :as dungeon]
            [cardungeon.player :as player]
            [cardungeon.card :as card]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(defn- print-dungeon [{::player/keys [health max-health]
                       ::dungeon/keys [room]}]
  (newline)
  (println "Health:" (str health "/" max-health))
  (doseq [[i card] (sort-by key room)]
    (println i ":" (card/->str card))))

(defn -main [& _]
  (newline)
  (println "Welcome to Cardungeon!")
  (println "type 'exit' to stop the game.")
  (println "type 'skip' to skip the current room.")
  (println "To play, enter the number of the dungeon room card to play it.")
  (loop [dungeon (dungeon/new-game)
         input ""]
    (let [exit? (#{"exit"} (str/lower-case input))
          skip? (#{"skip"} (str/lower-case input))
          parsed-input (edn/read-string input)
          skipped-room (and skip? (dungeon/skip-room dungeon))
          played-dungeon (dungeon/play dungeon parsed-input)
          new-dungeon (or skipped-room played-dungeon dungeon)
          room-cleared? (dungeon/room-cleared? new-dungeon)
          new-dungeon (cond-> new-dungeon room-cleared? dungeon/deal)
          dead? (player/dead? new-dungeon)]
      (when (and skip? (not skipped-room))
        (println "Cannot skip this room!"))
      (when skipped-room
        (println "Skipping this dungeon room! Prepare for the next dungeon room.."))
      (when (and room-cleared? (not skipped-room) (not dead?))
        (println "Dungeon room cleared! Entering the next dungeon room.."))
      (cond
        exit?
        (println "stopping game..")

        dead?
        (println "You didn't survive the dungeon!")

        (dungeon/finished? new-dungeon)
        (println "You cleared the dungeon!")

        :else
        (recur (doto new-dungeon print-dungeon) (read-line))))))
