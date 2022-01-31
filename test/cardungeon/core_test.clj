(ns cardungeon.core-test
  (:require [clojure.test :refer [are deftest is testing]]
            [cardungeon.core :as sut]))

;; Basic fight interaction with monster cards
(deftest fight-test
  (testing "fight"
    (testing "reduces player's health by monster strength"
      (are [dungeon monster] (= dungeon (sut/fight {:player/health 13} monster))
        {:player/health 10} {:card/monster 3}
        {:player/health 13} {:card/monster 0}
        {:player/health -1} {:card/monster 14}))
    (testing "asserts if supplied card is a monster"
        (is (thrown? AssertionError (sut/fight {} {:card/potion 3}))))))
