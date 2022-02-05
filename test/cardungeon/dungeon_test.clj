(ns cardungeon.dungeon-test
  (:require [clojure.test :refer [are deftest is testing]]
            [cardungeon.fixtures :as f]
            [cardungeon.dungeon :as sut]
            [cardungeon.player :as player]
            [cardungeon.room :as room]
            [cardungeon.card :as card]))

;; Basic fighting interaction with monster cards
(deftest fight-test
  (let [select-relevant #(select-keys % [::player/health :to-discard])]
    (testing "fight"
      (testing "reduces player's health by monster strength and moves monster to `:to-discard`"
        (are [player monster] (= (select-relevant player)
                                 (-> (f/make-player) (#'sut/fight monster) select-relevant))
          (f/make-player {::player/health 17 :to-discard [f/monster3]}) f/monster3
          (f/make-player {::player/health 20 :to-discard [f/monster0]}) f/monster0
          (f/make-player {::player/health 0 :to-discard [f/monster21]}) f/monster21))
      (testing "asserts if supplied card is a monster"
        (is (thrown? AssertionError (#'sut/fight {} (f/make-potion 3))))))))

;; Basic healing interaction with potion cards
(deftest heal-test
  (testing "heal"
    (testing "increases the player's health by potion strength"
      (are [health potion]
          (= health
             (::player/health (#'sut/heal (f/make-player {::player/health 8}) potion)))
        12 f/potion4
        16 f/potion8))
    (testing "doesn't work if already-healed in this room"
      (let [select-relevant #(select-keys % [::player/health :to-discard])]
        (is (= {::player/health 10 :to-discard [f/potion2]}
               (-> {::player/health 10}
                   (room/mark-already-healed)
                   (#'sut/heal f/potion2)
                   select-relevant)))))
    (testing "asserts if supplied card is a potion"
      (is (thrown? AssertionError (#'sut/heal (f/make-player) f/monster3))))))

;; Basic equipping interaction with weapon cards
(deftest auto-discard-test
  (testing "auto-sicard"
    (testing "discards cards in :to-discard"
      (is (= {::sut/discarded [:weapon-a]}
             (#'sut/auto-discard {:to-discard [:weapon-a]}))))))

(deftest play-test
  (testing "play"
    (let [game #(-> (f/make-player {::player/health 13
                                  ::room/east f/monster2
                                  ::room/north f/monster3
                                  ::room/south f/potion4})
                    (sut/play %))]
      (testing "returns the game with the room card removed"
        (is (= {::room/north f/monster3 ::room/south f/potion4}
               (room/select (game ::room/east))))
        (is (= {::room/east f/monster2 ::room/north f/monster3}
               (room/select (game ::room/south)))))
      (testing "returns the game with the room card moved to discarded coll"
        (is (= [f/monster2]
               (-> ::room/east game ::sut/discarded)))
        (is (= [f/potion4]
               (-> ::room/south game ::sut/discarded)))))
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
      (let [dungeon (sut/skip-room {::room/east :a ::room/north :b})]
        (is (nil? (::sut/room dungeon)))
        (is (= #{:a :b} (-> dungeon ::sut/draw-pile set)))))))
