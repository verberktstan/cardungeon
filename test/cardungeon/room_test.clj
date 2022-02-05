(ns cardungeon.room-test
  (:require [cardungeon.room :as sut]
            [cardungeon.fixtures :as f]
            [cardungeon.card :as card]
            [clojure.test :refer [are deftest is testing]]))

(deftest ->index-test
  (testing "->index"
    (are [result input] (= result (sut/->index input))
      ::sut/north "north"
      ::sut/north :north
      nil "unknown"
      nil :unknown
      nil nil)))

(deftest cleared?-test
  (testing "cleared?"
    (testing "returns a truethy value when room is cleared"
      (are [room] (sut/cleared? room)
        {}
        {::sut/east :card}))
    (testing "returns a falsey value when room is not cleared"
      (are [room] (-> room sut/cleared? not)
        {::sut/east :card ::sut/west :card}
        {::sut/east :card ::sut/south :card ::sut/west :card}))))

(def ^:private room
  {::sut/east :first-card})

(def ^:private another-room
  (assoc room ::sut/north :second-card ::sut/south :third-card
         ::sut/west :fourth-card))

(def ^:private cards
  [:first-card])

(def ^:private more-cards
  [:second-card :third-card :fourth-card])

(deftest merge-test
  (testing "merge"
    (testing "returns nil if room and cards are empty"
      (is (nil? (sut/merge {} []))))
    (testing "merges the supplied cards into the room"
      (are [result room cards] (= result (sut/merge room cards))
        room {} cards
        (assoc room ::sut/north :second-card) {} (conj cards :second-card)
        (assoc room ::sut/north :second-card) room [:second-card]
        another-room room more-cards
        another-room room (conj more-cards :fifth-card)))))

(deftest prepare-for-print-test
  (testing "prepare-for-print"
    (testing "returns a coll of direction, a colon and the card."
      (is (=
           [["east" ":" (card/->str f/potion2)]
            ["north" ":" (card/->str f/monster3)]]
           (sut/prepare-for-print {::sut/north f/monster3 ::sut/east f/potion2})))
      (is (empty? (sut/prepare-for-print {:other "stuff"}))))))
