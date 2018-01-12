---
title: "The Assignment"
date: 2017-12-11T21:35:04+01:00
weight: 1
disableToc: true
---

Congratulations, you just landed your first job at Todo Inc. After endless nights of relentless programming and designing your company has just been founded with millions of venture capital after pitching investors with a shiny protoype. The dilema: It's just a prototype and its your job to turn this pile of hasty assembled source files and workarounds into a production ready setup.

## Project Files

## Backend

Lets start by looking at what we got. The backend is implemented using the Spring Boot framework and luckily the responsible developer wrote a Gradle file so to start the backend we just have to run:

```
cd basic/the_assignment
cd todo-frontend
./gradlew run
```

{{% artifact href="basic/the_assignment" %}}To the sources{{% /artifact %}}

{{% notice tip %}}
The build.gradle file includes the Idea (`apply plugin: 'idea'`) and the Eclipse plugin (`apply plugin: 'eclipse'`) which allows us to create Idea/Eclipse project files by calling `./gradlew idea/eclipse` and then opening the project directly in the IDE creating a hassle free devloper experience.
{{% /notice %}}

## Frontend

The frontend team has gifted is with a a Vue.js based frontend that also contains some basic unit tests. It is built and bundled using webpack, so to start the frontend locally just change into the the frontend project directory, and run npm:

```
$ cd todo-frontend
$ npm run dev
[...]
DONE  Compiled successfully in 6945ms
I  Your application is running here: http://localhost:8080
```

{{% artifact href="basic/the_assignment" %}}To the sources{{% /artifact %}}

The frontend is now available at http://localhost:8080, and it is expecting a running backend at http://localhost:8081.

![Screenshot](http://via.placeholder.com/800x600)

The local dev instance comes with all bells and whistles like live reloading for code and css, to create the real deployable artifacts run the build via npm:

```
$ cd todo-frontend
$ npm run build

> todo-frontend@1.0.0 build todo-frontend
> node build/build.js

Hash: 722667b099318cce6988
Version: webpack 3.10.0
Time: 12917ms
                                                  Asset       Size  Chunks             Chunk Names
               static/js/vendor.bfbe8931f13dc4c86ffe.js     111 kB       0  [emitted]  vendor
                  static/js/app.d37b66e3593c9e271a18.js  910 bytes       1  [emitted]  app
             static/js/manifest.19de70c0b3ebd3fbdf13.js    1.49 kB       2  [emitted]  manifest
    static/css/app.a62596eaa159b3b17c85d0c7afbaab30.css  432 bytes       1  [emitted]  app
static/css/app.a62596eaa159b3b17c85d0c7afbaab30.css.map  828 bytes          [emitted]
           static/js/vendor.bfbe8931f13dc4c86ffe.js.map     546 kB       0  [emitted]  vendor
              static/js/app.d37b66e3593c9e271a18.js.map    7.83 kB       1  [emitted]  app
         static/js/manifest.19de70c0b3ebd3fbdf13.js.map     7.8 kB       2  [emitted]  manifest
                                             index.html  515 bytes          [emitted]

  Build complete.

```
{{% artifact href="basic/the_assignment" %}}To the sources{{% /artifact %}}


The build output is placed in the `dist` folder. These are the files that need to be served by a web server when we deploy the application to a live system.

```
$ cd todo-frontend
$ tree dist
dist
├── index.html
└── static
    ├── css
    │   ├── app.a62596eaa159b3b17c85d0c7afbaab30.css
    │   └── app.a62596eaa159b3b17c85d0c7afbaab30.css.map
    └── js
        ├── app.d37b66e3593c9e271a18.js
        ├── app.d37b66e3593c9e271a18.js.map
        ├── manifest.19de70c0b3ebd3fbdf13.js
        ├── manifest.19de70c0b3ebd3fbdf13.js.map
        ├── vendor.bfbe8931f13dc4c86ffe.js
        └── vendor.bfbe8931f13dc4c86ffe.js.map

3 directories, 9 files
```
