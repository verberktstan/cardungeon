(ns cardungeon.room-test
  (:require [cardungeon.room :as sut]
            [cardungeon.fixtures :as f]
            [cardungeon.card :as card]
            [clojure.test :refer [are deftest is testing]]))

(deftest ->index-test
  (testing "->index returns the index keyword when it can be parsed"
    (are [result input] (= result (sut/->index input))
      ::sut/north "north"
      ::sut/north :north
      nil         "unknown"
      nil         :unknown
      nil         nil)))

(deftest cleared?-test
  (testing "cleared?"
    (testing "returns a truethy value when room has 0 or 1 cards left"
      (are [room] (sut/cleared? room)
        {}
        {::sut/east :card}))
    (testing "returns a falsey value when room has 2 or more cards left"
      (are [room] (-> room sut/cleared? not)
        {::sut/east :card ::sut/south :card}
        {::sut/east :card ::sut/south :card ::sut/west :card}))))

(def ^:private ROOM
  {::sut/east :first-card})

(def ^:private ANOTHER-ROOM
  (assoc ROOM ::sut/north :second-card ::sut/south :third-card
         ::sut/west :fourth-card))

(def ^:private CARDS
  [:first-card])

(def ^:private MORE-CARDS
  [:second-card :third-card :fourth-card])

(deftest merge-test
  (testing "merge"
    (testing "returns nil if room and cards are empty"
      (is (nil? (sut/merge {} []))))
    (testing "merges the supplied cards into the room"
      (are [result room cards] (= result (sut/merge room cards))
        ROOM {} CARDS
        (assoc ROOM ::sut/north :second-card) {} (conj CARDS :second-card)
        (assoc ROOM ::sut/north :second-card) ROOM [:second-card]
        ANOTHER-ROOM ROOM MORE-CARDS
        ANOTHER-ROOM ROOM (conj MORE-CARDS :fifth-card)))))

(deftest prepare-for-print-test
  (testing "prepare-for-print"
    (testing "returns a coll of direction, a colon and the card."
      (is (=
           [(str "east: " (card/->str f/potion2))
            (str "north: " (card/->str f/monster3))]
           (sut/prepare-for-print {::sut/north f/monster3 ::sut/east f/potion2})))
      (is (empty? (sut/prepare-for-print {:other "stuff"}))))))
