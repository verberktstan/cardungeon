(ns cardungeon.core)

(defn- fight
  "Returns the dungeon with monster's strength subtracted from player's health"
  [dungeon {:card/keys [monster]}]
  {:pre [(number? monster)]}
  (update dungeon :player/health - monster))

(defn- heal [dungeon {:card/keys [potion]}]
  {:pre [(number? potion)]}
  (update dungeon :player/health + potion))

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

(defn finished?
  "Returns true when the dungeon's room and draw-pile are both empty."
  [{:dungeon/keys [room draw-pile] :as dungeon}]
  (boolean (and dungeon (empty? room) (empty? draw-pile))))
