(defproject org.clojure.clr/tools.analyzer.clr "1.2.4"
  :description "Port of clojure.org/tools.analyzer.clr to ClojureCLR"
  :url "https://github.com/clojure/tools.analyzer.clr"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/main/clojure" "src/main/lib"]
  :test-paths ["src/test/clojure"]			
  :dependencies [[org.clojure/tools.analyzer "1.1.1"]
                 [org.clojure.clr/core.memoize "1.0.257"]
				 [org.clojure.clr/tools.reader "1.3.7"]]
  :deploy-repositories [["clojars" {:url "https://clojars.org/repo/"
                                    :sign-releases  false}]]  
  :min-lein-version "2.0.0"
  :plugins [[lein-clr "0.2.0"]]
  :clr {:cmd-templates  {:clj-exe   [#_"mono" [CLJCLR15_40 %1]]
                         :clj-dep   [#_"mono" ["target/clr/clj/Debug 4.0" %1]]
                         :clj-url   "https://github.com/downloads/clojure/clojure-clr/clojure-clr-1.4.1-Debug-4.0.zip"
                         :clj-zip   "clojure-clr-1.4.1-Debug-4.0.zip"
                         :curl      ["curl" "--insecure" "-f" "-L" "-o" %1 %2]
                         :nuget-ver [#_"mono" [*PATH "nuget.exe"] "install" %1 "-Version" %2]
                         :nuget-any [#_"mono" [*PATH "nuget.exe"] "install" %1]
                         :unzip     ["unzip" "-d" %1 %2]
                         :wget      ["wget" "--no-check-certificate" "--no-clobber" "-O" %1 %2]}
        ;; for automatic download/unzip of ClojureCLR,
        ;; 1. make sure you have curl or wget installed and on PATH,
        ;; 2. uncomment deps in :deps-cmds, and
        ;; 3. use :clj-dep instead of :clj-exe in :main-cmd and :compile-cmd
        :deps-cmds      [ ;[:wget  :clj-zip :clj-url] ; edit to use :curl instead of :wget
                          ;[:unzip "../clj" :clj-zip]
                         ]
        :main-cmd      [:clj-exe "Clojure.Main.exe"]
        :compile-cmd   [:clj-exe "Clojure.Compile.exe"]})