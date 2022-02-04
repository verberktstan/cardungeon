(ns cardungeon.card-test
  (:require [cardungeon.card :as sut]
            [cardungeon.fixtures :as f]
            [clojure.test :refer [are deftest testing]]))

(deftest value-test
  (testing "value"
    (testing "returns the strength value of a card"
      (are [strength-value card] (= strength-value (sut/value card))
        1 f/catapult1
        2 f/potion2
        3 f/monster3
        4 f/shield4
        5 f/shieldify5
        6 f/weapon6
        nil {}))))

(deftest damage-test
  (testing "damage"
    (testing "returns monster card with damage dealt"
      (are [result card damage] (= result (-> card (sut/damage damage)))
        f/monster1 f/monster3 2
        f/monster0 f/monster3 4))
    (testing "returns non-monster card with no damage dealt"
      (are [result card damage] (= result (-> card (sut/damage damage)))
        f/potion2 f/potion2 2
        f/weapon6 f/weapon6 4))))

(deftest shieldify-test
  (testing "shieldify"
    (testing "returns a shield of the same value"
      (are [result card] (= result (sut/shieldify card))
        f/shield1 f/catapult1
        f/shield4 f/monster4
        f/shield5 f/shieldify5))))

(deftest <-test
  (testing "<"
    (testing "returns non-nil if in increasing value"
      (are [pred cards] (pred (apply sut/< cards))
        true? [f/monster1 f/monster3]
        false? [f/monster3 f/monster1]
        true? [f/monster1 f/potion2 f/monster3]
        false? [f/monster3 f/monster1 f/potion2]))))

(deftest ->str-test
  (testing "->str"
    (are [result card] (= result (sut/->str card))
      "Catapult(1)" f/catapult1
      "Potion(2)" f/potion2
      "Monster(3)" f/monster3
      "Shield(4)" f/shield4
      "Shieldify!" f/shieldify5
      "Weapon(6)" f/weapon6)))
