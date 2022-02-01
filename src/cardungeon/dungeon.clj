(ns cardungeon.dungeon
  (:require [cardungeon.player :as player]
            [cardungeon.room :as room]))

(def BASE_DECK
  (let [monster-range (map #(if (<= % 10) % 10) (range 2 15))]
    (concat
     (map (partial assoc {} :card/monster) monster-range)
     (map (partial assoc {} :card/monster) monster-range)
     (map (partial assoc {} :card/potion) (range 2 11)))))

(def BASE {::discarded BASE_DECK})

(defn- fight
  "Returns the dungeon with monster's strength subtracted from player's health."
  [dungeon {:card/keys [monster]}]
  {:pre [(number? monster)]}
  (player/update-health dungeon - monster))

(defn- heal
  "Returns the dungeon with the potion's value added to player's health, if not
  already healed in the current room."
  [{::room/keys [already-healed?] :as dungeon} {:card/keys [potion]}]
  {:pre [(number? potion)]}
  (cond-> dungeon
    (not already-healed?) (player/update-health + potion)
    (not already-healed?) room/mark-already-healed))

(defn- play-fn [{:card/keys [monster potion]}]
  (cond
    monster fight
    potion heal))

(defn play
  "Returns the game with dungeon-room card moved to dungeon-discarded, playing
  the room card on the fly"
  [{::keys [room] :as dungeon} room-idx]
  (when-let [card (get room room-idx)]
    (let [play* (play-fn card)]
      (-> dungeon
          (update ::room room/remove-card room-idx)
          (play* card)
          (update ::discarded conj card)))))

(defn- reshuffle
  "Returns the game with discarded cards shuffled into the draw pile."
  [{::keys [discarded] :as dungeon}]
  (-> dungeon
      (dissoc ::discarded)
      (update ::draw-pile concat (shuffle discarded))))

(defn room-cleared? [{::keys [room]}]
  (room/cleared? room))

(defn deal
  "Returns the game with up to 4 cards dealt from draw pile into the room."
  [{::keys [draw-pile room] :as dungeon}]
  (let [n-cards (- 4 (count room))
        drawn (take n-cards draw-pile)]
    (-> dungeon
        (update ::draw-pile (partial drop n-cards))
        (update ::room room/merge drawn)
        room/unmark-already-healed
        room/dec-cannot-skip)))

(defn new-game []
  (-> BASE (merge player/BASE room/BASE) reshuffle deal))

(defn finished?
  "Returns true when the dungeon's room and draw-pile are both empty."
  [{::keys [room draw-pile] :as dungeon}]
  (boolean (and dungeon (empty? room) (empty? draw-pile))))

(defn skip-room
  "Returns the dungeon with the current room returnd to the back of the draw
  pile. Returns nil when impossible to skip."
  [{::keys [room] :as dungeon}]
  (when (room/can-skip? dungeon)
    (-> dungeon
        (dissoc ::room)
        (update ::draw-pile concat (-> room vals shuffle))
        room/mark-cannot-skip)))
