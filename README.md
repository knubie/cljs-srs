lein is the build tool for clojurescript
The clojurescript files are located in `src/app`

They are compiled to `resources/public/js/main.js`

From there, it is imported by `resources/public/js/index.html`

You can either open `index.html` in a browser, or run it from a server with `lein figwheel`

You can also make electron load that `index.html` file by editing main.js and loading it in the `createWindow` function.

lein is set up to export to resources/

to run figwheel do

```
lein do clean, figwheel
```

to compile do

```
lein do clean, cljsbuild once
```

to compile and watch and recompile do

```
lein do clean, cljsbuild auto
```
