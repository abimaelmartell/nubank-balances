(ns balances.store)

(def accounts (atom {}))

(defn save-operation!
  [account-id operation]
  (swap! accounts update-in [account-id] conj operation))

(defn account-operations
  [account-id]
  (get @accounts account-id))

(defn reset-accounts!
  []
  (reset! accounts {}))
