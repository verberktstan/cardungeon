(ns cardungeon.player)

(def BASE {::health 20})

(defn update-health [player & args]
  (apply update player ::health args))

(defn dead? [{::keys [health]}]
  (< health 1))
