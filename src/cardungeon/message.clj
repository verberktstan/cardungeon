(ns cardungeon.message)

(defn set [dungeon s]
  (cond-> dungeon
    s (assoc :message s)))

(defn forget [dungeon]
  (dissoc dungeon :message))
