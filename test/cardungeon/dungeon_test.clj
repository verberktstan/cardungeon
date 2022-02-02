(ns cardungeon.dungeon-test
  (:require [clojure.test :refer [are deftest is testing]]
            [cardungeon.fixtures :refer [monster0 monster3 monster21 make-player make-potion potion2]]
            [cardungeon.dungeon :as sut]
            [cardungeon.player :as player]
            [cardungeon.room :as room]
            [cardungeon.card :as card]))

;; Basic fighting interaction with monster cards
(deftest fight-test
  (testing "fight"
    (testing "reduces player's health by monster strength and moves monster to `:to-discard`"
      (are [player monster] (= player (#'sut/fight (make-player) monster))
        (make-player {::player/health 17 :to-discard [monster3]}) monster3
        (make-player {::player/health 20 :to-discard [monster0]}) monster0
        (make-player {::player/health 0 :to-discard [monster21]}) monster21))
    (testing "asserts if supplied card is a monster"
        (is (thrown? AssertionError (#'sut/fight {} (make-potion 3)))))))

;; Basic healing interaction with potion cards
(deftest heal-test
  (testing "heal"
    (testing "increases the player's health by potion strength"
      (are [health potion]
          (= health
             (::player/health (#'sut/heal (make-player {::player/health 8}) potion)))
        13 (make-potion 5)
        16 (make-potion 8)))
    (testing "doesn't work if already-healed in this room"
      (is (= (room/mark-already-healed (make-player {::player/health 10 :to-discard [potion2]}))
             (#'sut/heal
              (room/mark-already-healed (make-player {::player/health 10}))
              potion2))))
    (testing "asserts if supplied card is a potion"
      (is (thrown? AssertionError (#'sut/heal (make-player) monster3))))))

;; Basic equipping interaction with weapon cards
(deftest auto-discard-test
  (testing "auto-sicard"
    (testing "discards cards in :to-discard"
      (is (= {::sut/discarded [:weapon-a]}
             (#'sut/auto-discard {:to-discard [:weapon-a]}))))))

(deftest play-test
  (testing "play"
    (let [game #(-> (make-player {::player/health 13
                                  ::sut/room {0 {::card/monster 2}
                                              1 {::card/monster 3}
                                              2 {::card/potion 4}}})
                    (sut/play %))]
      (testing "returns the game with the room card removed"
        (is (= {1 {::card/monster 3} 2 {::card/potion 4}}
               (::sut/room (game 0))))
        (is (= {0 {::card/monster 2} 1 {::card/monster 3}}
               (::sut/room (game 2)))))
      (testing "returns the game with the room card moved to discarded coll"
        (is (= [{::card/monster 2}]
               (-> 0 game ::sut/discarded)))
        (is (= [{::card/potion 4}]
               (-> 2 game ::sut/discarded)))))
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
