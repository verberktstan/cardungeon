(ns cardungeon.message
  (:refer-clojure :exclude [get set]))

(defn set [dungeon s]
  (cond-> dungeon
    s (assoc :message s)))

(def get :message)

(defn forget [dungeon]
  (dissoc dungeon :message))
