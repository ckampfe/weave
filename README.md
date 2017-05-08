# weave

I wanted to see if I could reimplement the graph bits of https://github.com/plumatic/plumbing


## Use

```clojure
(require '[weave.core :refer [fnk compile-eager compile-lazy view-dependency-graph!]])
(require '[clojure.pprint])


(def example {:a (fnk [] 1)
              :b (fnk [a] (+ a 1))
              :c (fnk [a] (+ a 3))
              :d (fnk [a c] (+ a c))
              :e (fnk [a b d] (+ a b d))
              :f (fnk [a c] (+ 1 2))})

((compile-eager example))

{:a 1, :b 2, :c 4, :f 3, :d 5, :e 8}

(clojure.pprint/pprint (take 10 ((compile-lazy example))))

({:a 1,
  :b #function[clojure.lang.AFunction/1],
  :c #function[clojure.lang.AFunction/1],
  :d #function[clojure.lang.AFunction/1],
  :e #function[clojure.lang.AFunction/1],
  :f #function[clojure.lang.AFunction/1]}
 {:a 1,
  :b 2,
  :c #function[clojure.lang.AFunction/1],
  :d #function[clojure.lang.AFunction/1],
  :e #function[clojure.lang.AFunction/1],
  :f #function[clojure.lang.AFunction/1]}
 {:a 1,
  :b 2,
  :c 4,
  :d #function[clojure.lang.AFunction/1],
  :e #function[clojure.lang.AFunction/1],
  :f #function[clojure.lang.AFunction/1]}
 {:a 1,
  :b 2,
  :c 4,
  :d #function[clojure.lang.AFunction/1],
  :e #function[clojure.lang.AFunction/1],
  :f 3}
 {:a 1, :b 2, :c 4, :d 5, :e #function[clojure.lang.AFunction/1], :f 3}
 {:a 1, :b 2, :c 4, :d 5, :e 8, :f 3})
nil

(view-dependency-graph! example)
```
<img src="http://i.imgur.com/UkNjS8d.png" width="200">

## License

Copyright © 2017 Clark Kampfe

Distributed under the MIT License
