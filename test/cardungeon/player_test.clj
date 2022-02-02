(ns cardungeon.player-test
  (:require [cardungeon.player :as sut]
            [clojure.test :refer [deftest is testing]]))

(deftest equip-test
  (testing "equip"
    (testing "returns the player if 'nil' card is supplied"
      (is (= {::sut/equipped :weapon}
             (sut/equip {::sut/equipped :weapon} nil))))
    (testing "assoc's the card with ::player/equipped"
      (is (= {::sut/equipped :weapon-a}
             (sut/equip {} :weapon-a))))
    (testing "assoc's previously equipped card with :to-discard"
      (is (= {::sut/equipped :weapon-b :to-discard [:weapon-a]}
             (sut/equip {::sut/equipped :weapon-a} :weapon-b))))))
