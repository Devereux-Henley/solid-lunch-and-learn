(ns solid.lib.core.operations)

;; common
(defn upsert
  [table state value id]
  (assoc-in state [table id] value))

(defn update
  [table state value id]
  (update-in state [table id] merge value))

(defn delete
  [table state id]
  (update-in state [table] dissoc id))
