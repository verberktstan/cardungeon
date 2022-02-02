(ns cardungeon.cli
  (:require [cardungeon.dungeon :as dungeon]
            [cardungeon.player :as player]
            [cardungeon.card :as card]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(defn- print-welcome-msg! []
  (newline)
  (println "Welcome to Cardungeon!")
  (println "type 'exit' to stop the game.")
  (println "type 'skip' to skip the current room.")
  (println "To play, enter the number of the dungeon room card to play it."))

(defn- print-dungeon! [{::player/keys [health max-health]
                       ::dungeon/keys [room]}]
  (newline)
  (println "Health:" (str health "/" max-health))
  (doseq [[i card] (sort-by key room)]
    (println i ":" (card/->str card))))

(defn- parse-cmd
  "Parses input string into a map with a command and a value.
  `(parse-cmd \"exit\") => {:exit true}`"
  [s]
  (when s
    (let [lcs (-> s str/trim str/lower-case)
          room-idx (edn/read-string s)]
      (cond
        (#{"exit"} lcs) {:exit? true}
        (#{"skip"} lcs) {:skip? true}
        (nat-int? room-idx) {:room-idx room-idx}))))

(defn- post-turn-fn [dungeon]
  (cond-> dungeon
    (dungeon/room-cleared? dungeon) dungeon/deal))

(defn -main [& _]
  (print-welcome-msg!)
  (loop [dungeon (doto (dungeon/new-game) print-dungeon!)
         input (read-line)]
    (let [{:keys [exit? room-idx skip?] :as cmd} (parse-cmd input)
          skipped (and skip? (dungeon/skip-room dungeon))
          played (and room-idx (dungeon/play dungeon room-idx))
          new-dungeon (post-turn-fn (or skipped played dungeon))]
      (when-not cmd
        (println "Can't do that! Please type 'exit', 'skip' or a room card number."))
      (when (and skip? (not skipped))
        (println "Can't skip this room!"))
      (when skipped
        (println "Skipping this dungeon room! Prepare for the next dungeon room.."))
      (when (and room-idx (not played))
        (println "Can't play this!"))
      (when played
        (println "Playing.."))
      (cond
        exit? (println "Stopping game..")
        (player/dead? new-dungeon) (println "You didn't survide the dungeon!")
        (dungeon/finished? new-dungeon) (println "You cleared the dungeon!")
        :else (recur (doto new-dungeon print-dungeon!) (read-line))))))
