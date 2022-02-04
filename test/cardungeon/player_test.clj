(ns cardungeon.player-test
  (:require [cardungeon.player :as sut]
            [cardungeon.fixtures :refer [make-player weapon2 weapon3]]
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
      (are [result op x] (= result
                            (-> {::sut/health 10 ::sut/max-health 20}
                                (sut/update-health op x)
                                ::sut/health))
        12 + 2
         8 - 2
        20 + 12
         0 - 12))))

(deftest equip-test
  (testing "equip"
    (testing "returns nil when card is not supplied"
      (is (nil? (sut/equip (make-player) nil))))
    (testing "assoc's the card with ::player/equipped"
      (is (= (make-player {::sut/equipped weapon2})
             (sut/equip (make-player) weapon2))))
    (testing "assoc's previously equipped card with :to-discard"
      (is (= (make-player {::sut/equipped weapon3
                      :to-discard [weapon2]})
             (sut/equip
              (make-player {::sut/equipped weapon2})
              weapon3))))))
