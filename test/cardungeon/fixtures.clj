(ns cardungeon.fixtures
  (:require [cardungeon.card :as card]
            [cardungeon.player :as player]))

(def make-monster (card/make :monster))

(def monster0 (make-monster 0))
(def monster3 (make-monster 3))
(def monster21 (make-monster 21))

(def make-potion (card/make :potion))

(def potion2 (make-potion 2))

(def make-weapon (card/make :weapon))

(def weapon2 (make-weapon 2))
(def weapon3 (make-weapon 3))

(def make-player (partial merge player/BASE))
