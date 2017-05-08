(ns weave.core
  (:require [clojure.pprint :refer [pprint]]
            [loom.alg :as alg]
            [loom.graph :as g]
            [loom.io :as io])
  (:gen-class))

(defmacro fnk [args# & body#]
  `(with-meta
     (fn ~args# ~@body#)
     {:args ~(vec (map keyword args#))}))

(def example {:a (fnk [] 1)
              :b (fnk [a] (+ a 1))
              :c (fnk [a] (+ a 3))
              :d (fnk [a c] (+ a c))
              :e (fnk [a b d] (+ a b d))
              :f (fnk [a c] (+ 1 2))})

(defn reverse-graph [m]
  (->> m
       (reduce (fn [acc [k vs]]
                 (concat
                  acc
                  (mapv #(vector % k) vs)))
               [])
       (apply concat)
       (partition 2)
       (map (fn [[a b]] (assoc {} a #{b})))
       (apply merge-with clojure.set/union)))

(defn fnmap->argmap [m]
  (reduce (fn [acc [k v]]
            (assoc acc
                   k
                   (->> v meta :args)))
          {}
          m))

(defn view-dependency-graph! [m]
  (let [argmap (fnmap->argmap m)
        reversed-argmap (reverse-graph argmap)
        dependency-graph (g/digraph reversed-argmap)]
    (io/view dependency-graph)))

(defn compile-eager [m]
  (let [argmap (fnmap->argmap m)
        reversed-argmap (reverse-graph argmap)
        dependency-graph (g/digraph reversed-argmap)
        ordered-keys (alg/topsort dependency-graph)]
    (fn []
      (reduce (fn [acc k]
                (let [deps (get argmap k)
                      deps-values (map #(get acc %) deps) ;; to preserve order
                      result-for-k (apply (get m k) (when (seq deps-values)
                                                      deps-values))]
                  (assoc acc k result-for-k)))
              {}
              ordered-keys))))

(defn compile-lazy [m]
  (let [argmap (fnmap->argmap m)
        reversed-argmap (reverse-graph argmap)
        dependency-graph (g/digraph reversed-argmap)
        ordered-keys (alg/topsort dependency-graph)]
    (letfn [(compile-lazy* [m ok]
              (when (first ok)
                (let [k (first ok)
                      deps (get argmap k)
                      deps-values (map #(get m %) deps) ;; to preserve order
                      result-for-k (apply (get m k) (when (seq deps-values)
                                                      deps-values))
                      result (assoc m k result-for-k)]

                  (lazy-seq
                   (when (seq ok)
                     (cons result
                           (compile-lazy* result (rest ok))))))))]

      (fn [] (compile-lazy* m ordered-keys)))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (view-dependency-graph! example)
  (println (type ((compile-lazy example))))
  (println ((compile-lazy example)))
  (pprint ((compile-lazy example)))
  (println (type ((compile-eager example))))
  (pprint ((compile-eager example))))
