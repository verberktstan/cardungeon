(ns cardungeon.dungeon-test
  (:require [clojure.test :refer [are deftest is testing]]
            [cardungeon.dungeon :as sut]
            [cardungeon.player :as player]
            [cardungeon.room :as room]))

;; Basic fighting interaction with monster cards
(deftest fight-test
  (testing "fight"
    (testing "reduces player's health by monster strength"
      (are [dungeon monster] (= dungeon (#'sut/fight {::player/health 13} monster))
        {::player/health 10} {:card/monster 3}
        {::player/health 13} {:card/monster 0}
        {::player/health -1} {:card/monster 14}))
    (testing "asserts if supplied card is a monster"
        (is (thrown? AssertionError (#'sut/fight {} {:card/potion 3}))))))

;; Basic healing interaction with potion cards
(deftest heal-test
  (testing "heal"
    (testing "increases the player's health by potion strength"
      (are [dungeon potion] (= dungeon (#'sut/heal {::player/health 8} potion))
        (room/mark-already-healed {::player/health 13}) {:card/potion 5}
        (room/mark-already-healed {::player/health 16}) {:card/potion 8}))
    (testing "doesn't work if already-healed in this room"
      (is (= (room/mark-already-healed {})
             (#'sut/heal
              (room/mark-already-healed {})
              {:card/potion 3}))))
    (testing "asserts if supplied card is a potion"
      (is (thrown? AssertionError (#'sut/heal {} {:card/monster 3}))))))

(deftest play-test
  (testing "play"
    (let [game (-> {::player/health 13
                    ::sut/room {0 {:card/monster 2}
                                1 {:card/monster 3}}}
                   (sut/play 0))]
      (testing "returns the game with the room card removed"
        (is (= {1 {:card/monster 3}}
               (::sut/room game))))
      (testing "returns the game with the room card moved to discarded coll"
        (is (= [{:card/monster 2}]
               (::sut/discarded game)))))
    (testing "returns nil if the room card isn't there"
      (is (nil? (sut/play {::sut/room {0 :card}} 10))))))

(deftest finished?-test
  (testing "finished?"
    (are [predicate dungeon] (predicate (sut/finished? dungeon))
      true? {::sut/room {} ::sut/draw-pile []}
      false? {::sut/room {0 :card} ::sut/draw-pile []}
      false? {::sut/room {} ::sut/draw-pile [:card]}
      false? nil)))

(deftest skip-room-test
  (testing "skip-room"
    (testing "returns nil if this room cannot be skipped"
      (is (nil? (sut/skip-room (room/mark-cannot-skip {})))))
    (testing "places current room's cards back into the draw pile"
      (let [dungeon (sut/skip-room {::sut/room {0 :a 1 :b}})]
        (is (nil? (::sut/room dungeon)))
        (is (= #{:a :b} (-> dungeon ::sut/draw-pile set)))))))