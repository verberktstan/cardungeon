(ns cardungeon.dungeon
  (:require [cardungeon.card :as card]
            [cardungeon.player :as player]
            [cardungeon.room :as room]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Private data

(def ^:private BASE_DECK
  (let [monster-range (map #(if (<= % 10) % 10) (range 2 15))
        monster-range2 (map #(if (<= % 12) % 12) (range 2 15))]
    (concat
     (map (card/make :monster) monster-range)
     (map (card/make :monster) monster-range2)
     (map (card/make :potion) (range 2 9))
     (map (card/make :weapon) (range 2 7))
     (map (card/make :catapult) (range 1 4))
     (map (card/make :shield) (range 1 4))
     (map (card/make :shieldify) [5]))))

(def ^:private BASE {::discarded BASE_DECK})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Private functions

(defn- discard
  "Returns dungeon with card conj'ed to the `:to-discard` key."
  [dungeon card]
  (update dungeon :to-discard conj card))

(defn- auto-discard
  "Returns dungeon with all cards associated with `:to-discard` moved to the back
  of the discard pile."
  [{:keys [to-discard] :as dungeon}]
  (cond-> dungeon
    (seq to-discard) (dissoc :to-discard)
    (seq to-discard) (update ::discarded concat to-discard)))

(defn- heal
  "Returns the dungeon with the potion's value added to player's health, if not
  already healed in the current room."
  [{::room/keys [already-healed?] :as dungeon} card]
  {:pre [(card/potion? card)]}
  (cond-> dungeon
    (not already-healed?) (player/update-health + (card/value card))
    (not already-healed?) room/mark-already-healed
    :always (discard card)))

(defn- equip
  "Returns the dungeon with card equipped and forget about the last slain monster."
  [dungeon card]
  (some-> dungeon (player/equip card) player/forget-last-slain))

(defn- fight
  "Returns the dungeon with monster's strength subtracted from player's health."
  [dungeon monster-card]
  {:pre [(card/monster? monster-card)]}
  (let [shield-strength (player/equipped-shield-value dungeon)
        make-shield (card/make :shield)
        new-shield (when-let [new-val (and shield-strength
                                           (> shield-strength 1)
                                           (dec shield-strength))]
                     (make-shield new-val))
        damage (cond-> (card/value monster-card)
                 shield-strength (- shield-strength)
                 :always (max 0))]
    (cond-> dungeon
      (pos? damage) (player/update-health - damage)
      new-shield (equip new-shield)
      :always (discard monster-card))))

(defn- slay [damage]
  {:pre [(pos-int? damage)]}
  (fn slay [{::player/keys [last-slain] :as dungeon} card]
    {:pre [(card/monster? card)]}
    (when (or (not last-slain) (card/< card last-slain))
      (let [damaged (card/damage card damage)]
        (-> dungeon
            (player/update-health - (card/value damaged))
            (discard damaged)
            (player/remember-last-slain card))))))

(defn- shoot [dungeon catapult]
  (let [[room-idx _] (room/random-entry card/monster? (room/select dungeon))]
    (-> dungeon
        (update room-idx card/damage (card/value catapult))
        (discard catapult))))

(defn- shieldify [dungeon card]
  (let [[room-idx _] (room/random-entry (room/select dungeon))]
    (-> dungeon
        (update room-idx card/shieldify)
        (discard card))))

(defn- play-fn
  "Returns the function to be used for playing a card given a dungeon and card."
  [{:keys [slay?] :as dungeon} card]
  (let [damage (player/equipped-weapon-value dungeon)]
    (if slay?
      (when (and (card/monster? card) damage) (slay damage))
      (cond
        (card/catapult?  card) shoot
        (card/monster?   card) fight
        (card/potion?    card)  heal
        (card/shield?    card) equip
        (card/shieldify? card) shieldify
        (card/weapon?    card) equip))))

(defn- reshuffle
  "Returns the game with discarded cards shuffled into the draw pile."
  [{::keys [discarded] :as dungeon}]
  (-> dungeon
      (dissoc ::discarded)
      (update ::draw-pile concat (shuffle discarded))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public functions

(defn play
  "Returns the game with dungeon-room card moved to dungeon-discarded, playing
  the room card on the fly"
  [dungeon room-idx]
  (when-let [card (get dungeon room-idx)]
    (when-let [play* (play-fn dungeon card)]
      (some-> dungeon
        (room/remove-card room-idx)
        (play* card)
        auto-discard
        (dissoc :slay?)))))

(defn deal
  "Returns the game with up to 4 cards dealt from draw pile into the room. Returns
  nil when no cards can be drawn from the draw pile."
  [{::keys [draw-pile room] :as dungeon}]
  (let [n-cards (- 4 (count room))
        drawn (take n-cards draw-pile)]
    (when (seq drawn)
      (-> dungeon
          (update ::draw-pile (partial drop n-cards))
          (room/merge drawn)
          room/unmark-already-healed
          room/dec-cannot-skip))))

(defn new-game []
  (-> BASE (merge player/BASE room/BASE) reshuffle deal))

(defn finished?
  "Returns true when the dungeon's room and draw-pile are both empty."
  [{::keys [room draw-pile] :as dungeon}]
  (boolean (and dungeon (empty? room) (empty? draw-pile))))

(defn skip-room
  "Returns the dungeon with the current room returnd to the back of the draw
  pile. Returns nil when it's impossible to skip."
  [dungeon]
  (let [room (room/select dungeon)]
    (when (room/can-skip? dungeon)
      (-> dungeon
          room/forget
          (update ::draw-pile concat (-> room vals shuffle))
          room/mark-cannot-skip))))
