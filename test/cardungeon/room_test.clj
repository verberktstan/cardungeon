(ns cardungeon.room-test
  (:require [cardungeon.room :as sut]
            [clojure.test :refer [are deftest testing]]))

(deftest cleared?-test
  (testing "cleared?"
    (are [room] (sut/cleared? room)
      {}
      {0 :card})
    (are [room] (-> room sut/cleared? not)
      {0 :card 1 :card}
      {0 :card 1 :card 2 :card})))
