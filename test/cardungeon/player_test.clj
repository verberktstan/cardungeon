(ns cardungeon.player-test
  (:require [cardungeon.player :as sut]
            [cardungeon.fixtures :refer [make-player weapon2 weapon3]]
            [clojure.test :refer [deftest is testing]]))

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
