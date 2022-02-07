(ns cardungeon.player-test
  (:require [cardungeon.player :as sut]
            [cardungeon.fixtures :as f]
            [cardungeon.card :as card]
            [clojure.test :refer [are deftest is testing]]))

(deftest update-health-test
  (testing "update-health"
    (testing "returns the player with new health, capped to zero."
      (are [result op x] (= result (sut/update-health {::sut/health 10} op x))
        {::sut/health 12} + 2
        {::sut/health  8} - 2
        {::sut/health 22} + 12
        {::sut/health  0} - 12))
    (testing "returns the player with new health, capped to max-health."
      (are [result op x]
          (= result
             (-> {::sut/health 10 ::sut/max-health 20} (sut/update-health op x) ::sut/health))
        12 + 2
         8 - 2
        20 + 12
         0 - 12))))

(deftest equip-test
  (testing "equip"
    (testing "returns nil when card is not supplied"
      (is (nil? (sut/equip (f/make-player) nil))))
    (testing "assoc's the card with ::player/equipped"
      (is (= f/weapon2
             (-> (f/make-player) (sut/equip f/weapon2) ::sut/equipped))))
    (testing "assoc's previously equipped card with :to-discard"
      (is (= [f/weapon2]
             (-> {::sut/equipped f/weapon2}
                 f/make-player
                 (sut/equip f/weapon3)
                 :to-discard))))))

(deftest healt-text-test
  (testing "health-text"
    (testing "returns a human readable string representing player's health"
      (are [result input] (= result (sut/health-text input))
        "Health: 20/20" {::sut/health 20 ::sut/max-health 20}
        "Health: 19/21" {::sut/health 19 ::sut/max-health 21}
        "Health: 18"    {::sut/health 18}))))

(deftest equipped-text-test
  (testing "equipped-text"
    (testing "returns a human readable string representing equipped card and last slain"
      (are [result input] (= result (sut/equipped-text input))
        "" {}

        (str "Equipped: " (card/->str f/weapon2))
        {::sut/equipped f/weapon2}

        (str "Equipped: " (card/->str f/weapon2) " - last slain: " (card/->str f/monster3))
        {::sut/equipped f/weapon2 ::sut/last-slain f/monster3}

        "" {::sut/last-slain f/monster3}))))
