(ns cardungeon.room
  (:refer-clojure :exclude [dissoc select merge]))

(def BASE {::cannot-skip? 1})

(def INDICES [::north ::east ::south ::west])

(defn index? [k]
  (contains? (set INDICES) k))

(defn ->index [s]
  (let [idx (keyword "cardungeon.room" (name s))]
    (when (index? idx) idx)))

(defn mark-already-healed [room]
  (assoc room ::already-healed? true))

(defn unmark-already-healed [room]
  (clojure.core/dissoc room ::already-healed?))

(defn mark-cannot-skip [room]
  (assoc room ::cannot-skip? 2))

(defn dec-cannot-skip [room]
  (update room ::cannot-skip? dec))

(defn can-skip? [{::keys [cannot-skip?]}]
  (or (not cannot-skip?) (< cannot-skip? 1)))

(defn remove-card [room idx]
  {:pre [((set INDICES) idx)]}
  (clojure.core/dissoc room idx))

(defn cleared? [room]
  (-> (select-keys room INDICES) count #{0 1}))

(defn merge
  "Returns the game with cards merged into the room, using free indices."
  [room cards]
  (let [free-indices (remove (partial contains? room) INDICES)]
    (clojure.core/merge room (zipmap free-indices cards))))

(defn select [room]
  (select-keys room INDICES))

(defn dissoc [room]
  (apply clojure.core/dissoc room INDICES))

(defn prepare-for-print [room]
  (sort-by key (select room)))
