(ns cardungeon.fixtures
  (:require [cardungeon.card :as card]
            [cardungeon.player :as player]))

(def make-catapult (card/make :catapult))

(def catapult1 (make-catapult 1))

(def make-monster (card/make :monster))

(def monster0 (make-monster 0))
(def monster1 (make-monster 1))
(def monster2 (make-monster 2))
(def monster3 (make-monster 3))
(def monster4 (make-monster 4))
(def monster21 (make-monster 21))

(def make-potion (card/make :potion))

(def potion2 (make-potion 2))
(def potion4 (make-potion 4))
(def potion8 (make-potion 8))

(def make-shield (card/make :shield))

(def shield1 (make-shield 1))
(def shield4 (make-shield 4))
(def shield5 (make-shield 5))

(def make-shieldify (card/make :shieldify))

(def shieldify5 (make-shieldify 5))

(def make-weapon (card/make :weapon))

(def weapon2 (make-weapon 2))
(def weapon3 (make-weapon 3))
(def weapon6 (make-weapon 6))

(def make-player (partial merge player/BASE))
