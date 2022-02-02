(ns cardungeon.cli-test
  (:require [cardungeon.cli :as sut]
            [clojure.test :refer [are deftest testing]]))

(deftest parse-cmd-test
  (testing "parse-cmd"
    (testing "returns a map with :exit?, :skip or :room-idx, or nil."
      (are [result s] (= result (#'sut/parse-cmd s))
        {:exit? true} "exit"
        {:room-idx 0} "0"
        {:skip? true} "skip"
        {:skip? true} " skip  "
        nil "test 0"
        nil ""
        nil nil))))
