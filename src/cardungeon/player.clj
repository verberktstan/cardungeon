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

(defn equip
  "Returns the player with card equipped and previously equipped card discarded.
  Returns nil if the supplied card is not a weapon."
  [{::keys [equipped] :as player} card]
  (when (card/weapon? card)
    (cond-> (assoc player ::equipped card)
      equipped (update :to-discard conj equipped))))

(defn equipped-weapon-damage [{::keys [equipped]}]
  (and (card/weapon? equipped) (card/value equipped)))

(defn remember-last-slain [player card]
  (assoc player ::last-slain card))

(defn forget-last-slain [player]
  (dissoc player ::last-slain))
