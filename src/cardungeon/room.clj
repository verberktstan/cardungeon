(ns cardungeon.room
  (:refer-clojure :exclude [select merge])
  (:require [cardungeon.card :as card]))

(def BASE {::cannot-skip? 1})

(def ^:private INDICES #{::north ::east ::south ::west})

(def index? INDICES)

(defn ->index [s]
  (let [idx (keyword "cardungeon.room" (name s))]
    (when (index? idx) idx)))

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
  {:pre [(index? idx)]}
  (dissoc room idx))

(defn cleared? [room]
  (-> (select-keys room INDICES) count #{0 1}))

(defn merge
  "Returns the game with cards merged into the room, using free indices."
  [room cards]
  (when (seq cards)
    (let [free-indices (remove room (sort INDICES))]
      (clojure.core/merge room (zipmap free-indices cards)))))

(defn select
  "Returns the room with only the indices kept."
  [room]
  (select-keys room INDICES))

(defn forget
  "Returns the room with all the indices dissociated."
  [room]
  (apply dissoc room INDICES))

(defn prepare-for-print
  "Returns a collection with collections that are printable in a humanly readable
  way."
  [room]
  (->> (select room)
       (sort-by key)
       (map (juxt (comp name key) (comp card/->str val)))
       (map (partial interpose ":"))))
