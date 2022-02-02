(ns cardungeon.card
  (:refer-clojure :exclude [<]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Private functions
(defn- value [{::keys [monster potion weapon]}]
  (or monster potion weapon))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public functions

(defn make
  "Returns a function that returns a new card with a given type and value.
  `((make :monster) 2) => {::monster 2}`"
  [type]
  {:pre [(#{:monster :potion :weapon} type)]}
  (fn [value]
    {:pre [(pos? value)]}
    {(keyword "cardungeon.card" (name type)) value}))

(def monster? ::monster)
(def potion? ::potion)
(def weapon? ::weapon)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Functions operating on a single card

(defn ->str
  "Returns a human readable string representing the card.
  `(->str {::monster 2}) => \"Monster(2)\"`"
  [{::keys [monster potion weapon]}]
  (cond
    monster (str "Monster(" monster ")")
    potion (str "Potion(" potion ")")
    weapon (str "Weapon(" weapon ")")))

(defn damage
  "Returns monster card with damage dealt."
  [{::keys [monster] :as card} damage]
  {:pre [(monster? card)]}
  (cond-> card
    monster (update ::monster - damage)
    monster (update ::monster (partial max 0))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Collection functions

(defn <
  "Returns non-nil if values of cards are in monotonically increasing order,
  otherwise false."
  [& cards]
  (apply clojure.core/< (map value cards)))
