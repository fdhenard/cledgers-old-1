(require 'cljs.build.api)

(cljs.build.api/build "src-cljs" {:output-to "frankcljsbuildout.js"})
