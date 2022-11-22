(ns clojure.tools.analyzer.clr.core-test
  (:refer-clojure :exclude [macroexpand-1])
  (:require [clojure.tools.analyzer :as ana]
            [clojure.tools.analyzer.clr :as ana.clr]
            [clojure.tools.analyzer.env :as env]
            [clojure.tools.analyzer.passes.elide-meta :refer [elides elide-meta]]
            [clojure.tools.analyzer.ast :refer [postwalk]]
            [clojure.test :refer [deftest is]]))

(defprotocol p (f [_]))
(defn f1 [^long x])
(def e (ana.clr/empty-env))

(defmacro ast [form]
  `(binding [ana/macroexpand-1 ana.clr/macroexpand-1
             ana/create-var    ana.clr/create-var
             ana/parse         ana.clr/parse
             ana/var?          var?
             elides            {:all #{:line :column :file}}]
     (env/with-env (ana.clr/global-env)
       (postwalk (ana/analyze '~form e) elide-meta))))

(defmacro ast1 [form]
  `(binding [ana/macroexpand-1 ana.clr/macroexpand-1
             ana/create-var    ana.clr/create-var
             ana/parse         ana.clr/parse
             ana/var?          var?
             elides            {:all #{:line :column :file}}]
     (ana.clr/analyze '~form e)))

(defmacro mexpand [form]
  `(ana.clr/macroexpand-1 '~form e))

(deftest macroexpander-test
  (is (= (list '. (list 'do System.Object) 'ToString)                                     ;;; java.lang.Object toString
         (mexpand (.ToString Object))))                                                   ;;; .toString
  (is (= (list '. System.Int32 '(Parse "2")) (mexpand (Int32/Parse "2")))))               ;;; java.lang.Integer parseInt Integer/parseInt 

(deftest analyzer-test

  (let [v-ast (ast #'+)]
    (is (= :the-var (:op v-ast)))
    (is (= #'+ (:var v-ast))))

  (let [mn-ast (ast (monitor-enter 1))]
    (is (= :monitor-enter (:op mn-ast)))
    (is (= 1 (-> mn-ast :target :form))))

  (let [mx-ast (ast (monitor-exit 1))]
    (is (= :monitor-exit (:op mx-ast)))
    (is (= 1 (-> mx-ast :target :form))))

  (let [i-ast (ast (clojure.core/import* "System.String"))]                                       ;;; "java.lang.String"
    (is (= :import (:op i-ast))) 
    (is (= "System.String" (:class i-ast))))                                                      ;;; "java.lang.String"

  (let [r-ast (ast ^:foo (reify
                           Object (ToString [this] "")                                            ;;; toString
                           System.IServiceProvider (GetService [this ^Type serviceType] this)))]  ;;; Appendable (^Appendable append [this ^char x] this)
    (is (= :with-meta (-> r-ast :op))) ;; line/column info
    (is (= :reify (-> r-ast :expr :op)))
    (is (= #{System.IServiceProvider clojure.lang.IObj} (-> r-ast :expr :interfaces)))            ;;; #{Appendable clojure.lang.IObj}
    (is (= '#{ToString GetService} (->> r-ast :expr :methods (mapv :name) set))))                 ;;; #{toString append}

  (let [dt-ast (ast (deftype* x user.x [a b]
                      :implements [IServiceProvider]                                              ;;; Appendable
                      (GetService [this ^Type serviceType] this)))]                               ;;; (^Appendable append [this ^char x] this)
    (is (= :deftype (-> dt-ast :op)))
    (is (= '[a b] (->> dt-ast :fields (mapv :name))))
    (is (= '[GetService] (->> dt-ast :methods (mapv :name))))                                     ;;; append
    (is (= 'user.x (-> dt-ast :class-name))))

  (let [c-ast (ast (case* 1 0 0 :number {2 [2 :two] 3 [3 :three]} :compact :int))]
    (is (= :number (-> c-ast :default :form)))
    (is (= #{2 3} (->> c-ast :tests (mapv (comp :form :test)) set)))
    (is (= #{:three :two} (->> c-ast :thens (mapv (comp :form :then)) set)))
    (is (= 3 (-> c-ast :high)))
    (is (= :int (-> c-ast :test-type)))
    (is (= :compact (-> c-ast :switch-type)))
    (is (= 2 (-> c-ast :low)))
    (is (= 0 (-> c-ast :shift)))
    (is (= 0 (-> c-ast :mask))))

  (is (= Exception (-> (ast1 (try (catch :default e))) :catches first :class :val)))             ;;; Throwable
  (is (= Exception (-> (ast1 (try (catch Exception e e))) :catches first :body :tag))))

(deftest doseq-chunk-hint
  (let [tree (ast1 (doseq [item (range 10)]
                     (println item)))
        {[_ chunk] :bindings} tree]
    (is (= :loop (:op tree)))
    (is (.StartsWith (name (:name chunk)) "chunk"))                                       ;;; .StartsWith
    (is (= clojure.lang.IChunk (:tag chunk)))))

(def ^:dynamic x)
(deftest set!-dynamic-var
  (is (ast1 (set! x 1))))

(deftest analyze-proxy
  (is (ast1 (proxy [Object] []))))

(deftest analyze-record
  (is (ast1 (defrecord TestRecord [x y]))))

(deftest eq-no-reflection
  (is (:validated? (-> (ast1 (fn [s] (= s \f))) :expr :methods first :body))))         ;;; I had to add the :expr to get this to work.

(deftest analyze+eval-context-test
  (let [do-ast (ana.clr/analyze+eval '(do 1 2 3))]
    (is (= :ctx/statement (-> do-ast :statements first :env :context)))))