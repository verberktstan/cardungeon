(ns cardungeon.dungeon
  (:require [cardungeon.player :as player]
            [cardungeon.room :as room]
            [cardungeon.card :as card]))

(def BASE_DECK
  (let [monster-range (map #(if (<= % 10) % 10) (range 2 15))]
    (concat
     (map (card/make :monster) monster-range)
     (map (card/make :monster) monster-range)
     (map (card/make :potion) (range 2 11))
     (map (card/make :weapon) (range 2 11)))))

(def BASE {::discarded BASE_DECK})

(defn- discard [dungeon card]
  (update dungeon :to-discard conj card))

(defn- auto-discard [{:keys [to-discard] :as dungeon}]
  (cond-> dungeon
    (seq to-discard) (dissoc :to-discard)
    (seq to-discard) (update ::discarded concat to-discard)))

(defn- fight
  "Returns the dungeon with monster's strength subtracted from player's health."
  [dungeon {::card/keys [monster] :as card}]
  {:pre [(nat-int? monster)]}
  (-> dungeon
      (player/update-health - monster)
      (discard card)))

(defn- heal
  "Returns the dungeon with the potion's value added to player's health, if not
  already healed in the current room."
  [{::room/keys [already-healed?] :as dungeon} {::card/keys [potion] :as card}]
  {:pre [(pos-int? potion)]}
  (cond-> dungeon
    (not already-healed?) (player/update-health + potion)
    (not already-healed?) room/mark-already-healed
    :always (discard card)))

(defn- equip [dungeon card]
  (-> dungeon
      (player/equip card)
      player/forget-last-slain))

(defn- slay [damage]
  {:pre [(pos-int? damage)]}
  (fn slay [{::player/keys [last-slain] :as dungeon}
            {::card/keys [monster] :as card}]
    {:pre [(pos-int? monster)]}
    (when (or (not last-slain) (card/< card last-slain))
      (let [damaged (card/damage card damage)]
        (-> dungeon
            (player/update-health - (::card/monster damaged))
            (discard damaged)
            (player/remember-last-slain card))))))

(defn- play-fn [{:keys [slay?] :as dungeon} card]
  (let [{::card/keys [monster potion weapon]} card
        damage (player/equipped-weapon-damage dungeon)]
    (if slay?
      (when (and monster damage)
        (slay damage))
      (cond
        monster fight
        potion heal
        weapon equip))))

(defn play
  "Returns the game with dungeon-room card moved to dungeon-discarded, playing
  the room card on the fly"
  [{::keys [room] :as dungeon} room-idx]
  (when-let [card (get room room-idx)]
    (when-let [play* (play-fn dungeon card)]
      (some-> dungeon
        (update ::room room/remove-card room-idx)
        (play* card)
        auto-discard
        (dissoc :slay?)))))

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
