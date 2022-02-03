(ns cardungeon.cli
  (:require [cardungeon.dungeon :as dungeon]
            [cardungeon.player :as player]
            [cardungeon.card :as card]
            [cardungeon.room :as room]
            [clojure.string :as str]))

(defn- print-welcome-msg! []
  (newline)
  (println "Welcome to Cardungeon!")
  (println "type 'exit' to stop the game.")
  (println "type 'skip' to skip the current room.")
  (println "To play, enter the 'east', 'north', 'south' or 'west' to play the card in that direction."))

(defn- print-room-card [[idx card]]
  (println (name idx) ":" (card/->str card)))

(defn- print-dungeon! [{::player/keys [equipped health last-slain max-health] :as dungeon}]
  (newline)
  (println "Health:" (str health "/" max-health))
  (when equipped
    (println "Equipped:" (card/->str equipped)))
  (when last-slain
    (println "Last slain:" (card/->str last-slain)))
  (run! print-room-card (room/prepare-for-print dungeon)))

(defn- parse-cmd
  "Parses input string into a map with a command and a value.
  `(parse-cmd \"exit\") => {:exit true}`"
  [s]
  (when s
    (let [lcs (-> s str/trim str/lower-case)
          room-idx (room/->index s)]
      (cond
        (#{"exit"} lcs) {:exit? true}
        (#{"skip"} lcs) {:skip? true}
        room-idx {:room-idx room-idx}))))

(defn- post-turn-fn [dungeon]
  (cond-> dungeon
    (and (room/cleared? dungeon) (dungeon/deal dungeon)) dungeon/deal))

(defn -main [& _]
  (print-welcome-msg!)
  (loop [dungeon (doto (dungeon/new-game) print-dungeon!)
         input (read-line)]
    (let [{:keys [exit? room-idx skip?] :as cmd} (parse-cmd input)
          skipped (and skip? (dungeon/skip-room dungeon))
          slayed (and room-idx (dungeon/play (assoc dungeon :slay? true) room-idx))
          slay? (when slayed
                  (println "Use your weapon to slay? Type 'y'.")
                  (-> (read-line) str/lower-case #{"y"}))
          played (and room-idx (dungeon/play dungeon room-idx))
          new-dungeon (post-turn-fn (or skipped (when slay? slayed) played dungeon))]
      (when-not cmd
        (println "Can't do that! Please type 'exit', 'skip' or a room card number."))
      (when (and skip? (not skipped))
        (println "Can't skip this room!"))
      (when skipped
        (println "Skipping this dungeon room! Prepare for the next dungeon room.."))
      (when (and room-idx (and (not slayed) (not played)))
        (println "Can't play this!"))
      (when played
        (println "Playing.."))
      (cond
        exit? (println "Stopping game..")
        (player/dead? new-dungeon) (println "You didn't survide the dungeon!")
        (dungeon/finished? new-dungeon) (println "You cleared the dungeon!")
        :else (recur (doto new-dungeon print-dungeon!) (read-line))))))
