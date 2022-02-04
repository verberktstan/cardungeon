(ns cardungeon.card
  (:refer-clojure :exclude [<]))

(def ^:private TYPES #{:catapult :monster :potion :shield :shieldify :weapon})

(defn- namespaced-keyword [x]
  (keyword "cardungeon.card" (name x)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public functions

(defn make
  "Returns a function that returns a new card with a given type and value.
  `((make :monster) 2) => {::monster 2}`"
  [type]
  {:pre [(contains? TYPES type)]}
  (fn [value]
    {:pre [(nat-int? value)]}
    {(namespaced-keyword type) value}))

(defn- make-check
  "Returns a function that checks for a given key and returns the full map."
  [s]
  (fn check* [card]
    (and ((namespaced-keyword s) card) card)))

(def catapult? (make-check "catapult"))
(def monster? (make-check "monster"))
(def potion? (make-check "potion"))
(def shield? (make-check "shield"))
(def shieldify? (make-check "shieldify"))
(def weapon? (make-check "weapon"))

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
