(ns cardungeon.card)

(defn make
  "Returns a function that returns a new card with a given type and value.
  `((make :monster) 2) => {::monster 2}`"
  [type]
  {:pre [(#{:monster :potion} type)]}
  (fn [value]
    {:pre [(pos? value)]}
    {(keyword "cardungeon.card" (name type)) value}))

(defn ->str
  "Returns a human readable string representing the card.
  `(->str {::monster 2}) => \"Monster(2)\"`"
  [{::keys [monster potion]}]
  (cond
    monster (str "Monster(" monster ")")
    potion (str "Potion(" potion ")")))
