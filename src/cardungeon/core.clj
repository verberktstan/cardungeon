(ns cardungeon.core)

(defn fight
  "Returns the dungeon with monster's strength subtracted from player's health"
  [dungeon {:card/keys [monster]}]
  {:pre [(number? monster)]}
  (update dungeon :player/health - monster))
