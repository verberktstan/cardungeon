(ns cardungeon.core)

(def BASE_DECK
  (let [monster-range (map #(if (<= % 10) % 10) (range 2 15))]
    (concat
     (map (partial assoc {} :card/monster) monster-range)
     (map (partial assoc {} :card/monster) monster-range)
     (map (partial assoc {} :card/potion) (range 2 11)))))

(defn- fight
  "Returns the dungeon with monster's strength subtracted from player's health."
  [dungeon {:card/keys [monster]}]
  {:pre [(number? monster)]}
  (update dungeon :player/health - monster))

(defn- heal
  "Returns the dungeon with the potion's value added to player's health, if not
  already healed in the current room."
  [{:room/keys [already-healed?] :as dungeon} {:card/keys [potion]}]
  {:pre [(number? potion)]}
  (cond-> dungeon
    (not already-healed?) (update :player/health + potion)
    (not already-healed?) (assoc :room/already-healed? true)))

(defn- play-fn [{:card/keys [monster potion]}]
  (cond
    monster fight
    potion heal))

(defn play
  "Returns the game with dungeon-room card moved to dungeon-discarded, playing
  the room card on the fly"
  [{:dungeon/keys [room] :as dungeon} room-idx]
  (when-let [card (get room room-idx)]
    (let [play* (play-fn card)]
      (-> dungeon
          (update :dungeon/room dissoc room-idx)
          (play* card)
          (update :dungeon/discarded conj card)))))

(defn- reshuffle
  "Returns the game with discarded cards shuffled into the draw pile."
  [{:dungeon/keys [discarded] :as dungeon}]
  (-> dungeon
      (dissoc :dungeon/discarded)
      (update :dungeon/draw-pile concat (shuffle discarded))))

(defn- merge-room
  "Returns the game with cards merged into the room, using free indices."
  [room cards]
  (let [free-indices (remove (partial contains? room) (range))]
    (merge room (zipmap free-indices cards))))

(defn room-cleared? [{:dungeon/keys [room]}]
  (#{0 1} (count room)))

(defn deal
  "Returns the game with up to 4 cards dealt from draw pile into the room."
  [{:dungeon/keys [draw-pile room] :as dungeon}]
  (let [n-cards (- 4 (count room))
        drawn (take n-cards draw-pile)]
    (-> dungeon
        (update :dungeon/draw-pile (partial drop n-cards))
        (update :dungeon/room merge-room drawn)
        (dissoc :room/already-healed?)
        (update :room/cannot-skip? dec))))

(defn new-game []
  (-> {:player/health 20 :dungeon/discarded BASE_DECK :room/cannot-skip? 1}
      reshuffle
      deal))

(defn finished?
  "Returns true when the dungeon's room and draw-pile are both empty."
  [{:dungeon/keys [room draw-pile] :as dungeon}]
  (boolean (and dungeon (empty? room) (empty? draw-pile))))

(defn skip
  "Returns the dungeon with the current room returnd to the back of the draw
  pile. Returns nil when impossible to skip."
  [{:room/keys [cannot-skip?] :dungeon/keys [room] :as dungeon}]
  (when (< cannot-skip? 1)
    (-> dungeon
        (dissoc :dungeon/room)
        (update :dungeon/draw-pile concat (-> room vals shuffle))
        (assoc :room/cannot-skip? 2))))
