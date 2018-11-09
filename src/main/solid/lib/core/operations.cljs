(ns solid.lib.core.operations)

;; common
(defn upsert
  [table state value id]
  (assoc-in state [table id] value))

(defn delete
  [table state id]
  (update-in state [table] dissoc id))
