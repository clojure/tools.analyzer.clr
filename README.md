# tools.analyzer.clr

A port of [clojure/tools.analyzer.jvm](https://github.com/clojure/tools.analyzer.jvm) to ClojureCLR.

From the parent's README:

> An analyzer for Clojure code, written on top of [tools.analyzer](https://github.com/clojure/tools.analyzer), providing additional jvm-specific passes.

Please see the JVM version's [README](https://github.com/clojure/tools.analyzer.jvm/blob/master/README.md) for more details.

# Notes

Fully functional except:

- `analyze-ns` is not yet implemented.  (The JVM version involves classpath searching and related techniques that are not relevant on the CLR.  This needs some thought.


# Releases

Nuget reference:

```
    PM> Install-Package clojure.tools.analyzer.clr -Version 1.2.3.1 
```
	
Leiningen/Clojars reference:

```
   [org.clojure.clr/tools.analyzer.clr "1.2.3.1"]
```
   

## License

Copyright (C) 2022 Rich Hickey, David Miller & ClojureCLR contributors
Distributed under the Eclipse Public License, the same as Clojure.


The parent project has this:

>Copyright © 2013-2021 Nicola Mometto, Rich Hickey & contributors.
>Distributed under the Eclipse Public License, the same as Clojure.

