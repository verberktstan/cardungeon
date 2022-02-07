(ns cardungeon.player
  (:require [cardungeon.card :as card]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public data

(def BASE {::health 20 ::max-health 20})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public functions

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
  (when (card/equipable? card)
    (cond-> (assoc player ::equipped card)
      equipped (update :to-discard conj equipped))))

(defn remember-last-slain [player card]
  (assoc player ::last-slain card))

(defn forget-last-slain [player]
  (dissoc player ::last-slain))

(def equipped-weapon-value (comp card/value card/weapon? ::equipped))

(def equipped-shield-value (comp card/value card/shield? ::equipped))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Regarding texts for printing

(defn health-text [{::keys [health max-health]}]
  (str "Health: " health (when max-health (str "/" max-health))))

(defn equipped-text [{::keys [equipped last-slain]}]
  (str (when equipped
         (str "Equipped: " (card/->str equipped)))
       (when (and equipped last-slain)
         (str " - last slain: " (card/->str last-slain)))))
