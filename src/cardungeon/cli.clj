(ns cardungeon.cli
  (:require [cardungeon.dungeon :as dungeon]
            [cardungeon.player :as player]
            [cardungeon.room :as room]
            [cardungeon.message :as message]
            [clojure.string :as str]))

(def ^:private WELCOME
  [""
   "Welcome to Cardungeon!"
   "type 'help' for instructions."
   "type 'exit' to stop the game."
   "type 'skip' to skip the current room."])

(def ^:private INSTRUCTIONS
  ["You have to survive a creepy dungeon. You'll enter rooms with 4 cards."
   "type 'east', 'north', 'south' or 'west' to play the corresponding card."
   "Monsters will fight you, and you'll lose health."
   "If you select a weapon, you'll equip it and damage the monsters you fight."
   "Potions will heal you."
   "If you select a catapult, a random monster will take some damage."
   "Shields can be equipped like weapons, monsters will harm you less if you carry one."
   "If you select a shieldify, a random card will transform into a shield!"])

(defn- parse-cmd
  "Parses input string into a map with a command and a value.
  `(parse-cmd \"exit\") => {:exit true}`"
  [s]
  (when s
    (let [lcs (-> s str/trim str/lower-case)
          room-idx (room/->index lcs)]
      (cond
        (#{"exit"} lcs) {:exit? true}
        (#{"help"} lcs) {:help? true}
        (#{"skip"} lcs) {:skip? true}
        room-idx {:room-idx room-idx}))))

(defn- post-turn-fn [dungeon]
  (cond-> dungeon
    (and (room/cleared? dungeon) (dungeon/deal dungeon)) dungeon/deal))

(defn- info-text
  "Returns info, if cmd or skip didn't workout, or the message after a turn."
  [{:keys [cmd message skip? skipped]}]
  (cond
    (not cmd) "Can't do that!"
    (and skip? (not skipped)) "Can't skip this room!"
    message message))

(defn- get-slay-input! [slayed]
  (when slayed
    (println "Type 'y' to use your weapon to slay..")
    (-> (read-line) str/lower-case #{"y"})))

(defn -main [_]
  (run! println WELCOME)
  (loop [dungeon (dungeon/new-game)]
    (newline)
    (run! println (dungeon/prepare-for-print dungeon))
    (let [{:keys [exit? help? room-idx skip?] :as cmd} (-> (read-line) parse-cmd)
          skipped (and skip? (dungeon/skip-room dungeon))
          slayed (dungeon/play (assoc dungeon :slay? true) room-idx)
          slay? (get-slay-input! slayed)
          played (dungeon/play dungeon room-idx)
          new-dungeon (post-turn-fn (or skipped (when slay? slayed) played dungeon))]
      (some-> {:cmd cmd
               :message (message/get new-dungeon)
               :skip? skip?
               :skipped skipped}
              info-text
              println)
      (when help?
        (run! println INSTRUCTIONS))
      (cond
        exit? (println "Stopping game..")
        (player/dead? new-dungeon) (println "You didn't survide the dungeon!")
        (dungeon/finished? new-dungeon) (println "You cleared the dungeon!")
        :else (recur (message/forget new-dungeon))))))
