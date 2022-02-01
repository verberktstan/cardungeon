(ns cardungeon.room
  (:refer-clojure :exclude [merge]))

(def BASE {::cannot-skip? 1})

(defn mark-already-healed [room]
  (assoc room ::already-healed? true))

(defn unmark-already-healed [room]
  (dissoc room ::already-healed?))

(defn mark-cannot-skip [room]
  (assoc room ::cannot-skip? 2))

(defn dec-cannot-skip [room]
  (update room ::cannot-skip? dec))

(defn can-skip? [{::keys [cannot-skip?]}]
  (or (not cannot-skip?) (< cannot-skip? 1)))

(defn remove-card [room idx]
  (dissoc room idx))

(defn cleared? [room]
  (-> room count #{0 1} boolean))

(defn merge
  "Returns the game with cards merged into the room, using free indices."
  [room cards]
  (let [free-indices (remove (partial contains? room) (range))]
    (clojure.core/merge room (zipmap free-indices cards))))
