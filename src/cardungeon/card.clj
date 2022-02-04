(ns cardungeon.card
  (:refer-clojure :exclude [<]))

(def ^:private TYPES #{:catapult :monster :potion :shield :shieldify :weapon})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public functions

(defn make
  "Returns a function that returns a new card with a given type and value.
  `((make :monster) 2) => {::monster 2}`"
  [type]
  {:pre [(contains? TYPES type)]}
  (fn [value]
    {:pre [(nat-int? value)]}
    {(keyword "cardungeon.card" (name type)) value}))

(def catapult? ::catapult)
(def monster? ::monster)
(def potion? ::potion)
(def shield? ::shield)
(def shieldify? ::shieldify)
(def weapon? ::weapon)

(defn value [{::keys [catapult monster potion shield shieldify weapon]}]
  (or catapult monster potion shield shieldify weapon))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Functions operating on a single card

(defn ->str
  "Returns a human readable string representing the card.
  `(->str {::monster 2}) => \"Monster(2)\"`"
  [card]
  (let [v (value card)]
    (cond
      (catapult? card) (str "Catapult(" v ")")
      (monster? card) (str "Monster(" v ")")
      (potion? card) (str "Potion(" v ")")
      (shield? card) (str "Shield(" v ")")
      (shieldify? card) (str "Shieldify!")
      (weapon? card) (str "Weapon(" v ")"))))

(defn damage
  "Returns monster card with damage dealt."
  [card damage]
  (let [monster (monster? card)]
    (cond-> card
      monster (update ::monster - damage)
      monster (update ::monster (partial max 0)))))

(def ^:private make-shield (make :shield))

(defn shieldify [card]
  (make-shield (value card)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Collection functions

(defn <
  "Returns non-nil if values of cards are in monotonically increasing value,
  otherwise false."
  [& cards]
  (apply clojure.core/< (map value cards)))
