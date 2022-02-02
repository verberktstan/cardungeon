(ns cardungeon.player
  (:require [cardungeon.card :as card]))

(def BASE {::health 20 ::max-health 20})

(defn update-health
  "Updates player's health by applying args. Health is limited to 0 < max-health.
  `(update-health {::health 15} + 6) => {::health 21}`
  `(update-health {::health 15 ::max-health 20} + 6) => {::health 20 ::max-health 20}`"
  [{::keys [max-health] :as player} & args]
  (cond-> (apply update player ::health args)
    max-health (update ::health (partial min max-health))
    :always (update ::health (partial max 0))))

(defn dead? [{::keys [health]}]
  (zero? health))

(defn equip [{::keys [equipped] :as player} card]
  (cond-> player
    card (assoc ::equipped card)
    (and card equipped) (update :to-discard conj equipped)))

(defn equipped-weapon-damage [{::keys [equipped]}]
  (::card/weapon equipped))

(defn remember-last-slain [player card]
  (assoc player ::last-slain card))

(defn forget-last-slain [player]
  (dissoc player ::last-slain))
