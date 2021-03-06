---
title: "Packaging the application"
date: 2017-12-11T21:35:04+01:00
weight: 10
disableToc: true
showHeaderLink: true 
---

## Creating a deployable artifact

The first step will be to transform the frontend and backend projects into a single deployable artifact. Our goal here is that all the files needed to run the project are included in a single runnable file. This eases the deployment process as we will always have a consistent state of the project without the danger of mixing different versions of different files.
Also a single artifact makes tracking much easier as we only need the hash of a single file to identify the exact deployed version.

### Converting the project into a Gradle multi project build

As we already know, the backend uses Gradle as build system. We will take advantage of [Gradles multi project](  https://docs.gradle.org/current/userguide/multi_project_builds.html) feature to build the frontend with Gradle as well.

First we move the Gradle wrapper from the `todo-server` project to its parent directory:

```
$ cd todo-server
$ mv gradlew* ../
$ mv gradle ../
```

{{% notice tip %}}
The Gradle wrapper bootstraps the Gradle environment needed for the build and removes the burden to install Gradle from the user. This helps to make the build more portable and less dependent on the local setup.
{{% /notice %}}

In a multi project we need a file `settings.gradle` which tells Gradle what sub-projects are part of the build:

<!-- file:10_basic/20_packaging/settings.gradle -->
{{% github href="/10_basic/20_packaging/settings.gradle" %}}settings.gradle{{% /github %}}
{{< highlight go "" >}}
include 'todo-server', 'todo-frontend'
{{< / highlight >}}
<!-- /file:10_basic/20_packaging/settings.gradle -->

Additionaly we create a `build.gradle` to define everything that is common for all submodules of the build:

<!-- file:10_basic/20_packaging/build.gradle -->
{{% github href="/10_basic/20_packaging/build.gradle" %}}build.gradle{{% /github %}}
{{< highlight go "" >}}
allprojects {

    repositories {
        mavenCentral()
    }

    apply plugin: 'idea'
    apply plugin: 'eclipse'

    group = "io.pelle"
}
{{< / highlight >}}
<!-- /file:10_basic/20_packaging/build.gradle -->


### Package the frontend

As already mentioned it is vitally important for the build not to rely on any local prerequistes on the machine that executes the build. Keep in mind that the build has to work on your machine, your coworkers machine or in a continus integration environment. Luckily for us a [node.js plugin](https://github.com/srs/gradle-node-plugin) for Gradle already exists that creates a node.js environment for us, we just have to apply the plugin from the public Gradle maven repository:

<!-- snippet:frontend_nodejs_plugin_dependency -->
{{% github href="10_basic/20_packaging/todo-frontend/build.gradle#L1-L13" %}}build.gradle{{% /github %}}
{{< highlight go "" >}}
buildscript {
	repositories {
		maven {
			url "https://plugins.gradle.org/m2/"
		}

  	dependencies {
	    classpath 'com.moowork.gradle:gradle-node-plugin:+'
    }
	}
}

apply plugin: 'com.moowork.node'
{{< / highlight >}}
<!-- /snippet:frontend_nodejs_plugin_dependency -->

The plugin can be configured to use a specific node.js/npm version:

<!-- snippet:frontend_nodejs_plugin_configuration -->
{{% github href="10_basic/20_packaging/todo-frontend/build.gradle#L15-L21" %}}build.gradle{{% /github %}}
{{< highlight go "" >}}
node {
  version = '6.3.1'
  npmVersion = '4.0.1'
  distBaseUrl = 'https://nodejs.org/dist'
  download = true
  workDir = file("${project.buildDir}/nodejs")
}
{{< / highlight >}}
<!-- /snippet:frontend_nodejs_plugin_configuration -->

Now we create a new task that executes the npm build:

<!-- snippet:frontend_nodejs_build -->
{{% github href="10_basic/20_packaging/todo-frontend/build.gradle#L23-L27" %}}build.gradle{{% /github %}}
{{< highlight go "" >}}
task frontendBuild(type: NpmTask) {
	args = [ 'run', 'build' ]
}

frontendBuild.dependsOn('npmInstall')
{{< / highlight >}}
<!-- /snippet:frontend_nodejs_build -->

and package the resulting filses from the `dist` folder in a jar file:

<!-- snippet:frontend_nodejs_jar -->
{{% github href="10_basic/20_packaging/todo-frontend/build.gradle#L29-L36" %}}build.gradle{{% /github %}}
{{< highlight go "" >}}
task frontendJar(type: Jar) {
 	appendix = 'frontend'
 	into 'frontend'
 	from fileTree('./dist/')
 	destinationDir file(project.buildDir)
}

frontendJar.dependsOn('frontendBuild')
{{< / highlight >}}
<!-- /snippet:frontend_nodejs_jar -->

The last step is to add the resulting jar to a [Gradle configuration](https://docs.gradle.org/current/userguide/dependency_management.html#sub:configurations) named `frontend` so dependent projects (in our case the frontend-server project) can include the frontend artifacts in their build cycle:

<!-- snippet:frontend_nodejs_gradle_config -->
dsadas
<!-- /sippet:frontend_nodejs_gradle_config -->

### Package the backend
As the backend is already built with Gradle we only need some minor modifications. The `spring-boot-gradle-plugin` we are using already provides a task that creates a fat jar file containing all dependencies needed to run the application. To also serve the static files for the frontend we need to add a dependency to the frontend build we just created:

<!-- snippet:frontend_backend_dependency -->
{{% github href="10_basic/20_packaging/todo-server/build.gradle#L26-L40" %}}build.gradle{{% /github %}}
{{< highlight go "" >}}
dependencies {
    compile('com.google.guava:guava:23.6-jre')
    compile('org.apache.derby:derby:10.14.1.0')
    compile('org.springframework.boot:spring-boot-starter-web:1.5.9.RELEASE')
    compile('org.springframework.boot:spring-boot-starter-hateoas:1.5.9.RELEASE')
    compile('org.springframework.boot:spring-boot-starter-data-jpa:1.5.9.RELEASE')

    testCompile('org.springframework.boot:spring-boot-starter-test:1.5.9.RELEASE')

    testCompile('com.jayway.jsonpath:json-path-assert:2.2.0')
    testCompile('org.springframework:spring-test')
    testCompile('junit:junit')

    runtime project(path: ':todo-frontend', configuration: 'frontend')
}
{{< / highlight >}}
<!-- /snippet:frontend_backend_dependency -->

Now that the static `frontend.jar` is available on the classpath of our application we only have to tell Spring Boot to serve this files by adding a `ResourceHandler` that matches all HTTP request and tries to serve them with the static files from the `frontend-jar` we just added as a dependency:

<!-- file:10_basic/20_packaging/todo-server/src/main/java/io/pelle/todo/configuration/FrontendContent.java -->
{{% github href="/10_basic/20_packaging/todo-server/src/main/java/io/pelle/todo/configuration/FrontendContent.java" %}}FrontendContent.java{{% /github %}}
{{< highlight go "" >}}
package io.pelle.todo.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@ConditionalOnWebApplication
class FrontendContent extends WebMvcConfigurerAdapter {
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**").addResourceLocations("classpath:/frontend/");
  }
}
{{< / highlight >}}
<!-- /file:10_basic/20_packaging/todo-server/src/main/java/io/pelle/todo/configuration/FrontendContent.java -->

As a finishing touch we set the `executable` attribute of the Spring Boot gradle plugin to `true` makes the JAR file directly executable by adding a start script in front of the JAR file.

<!-- snippet:backend_executable -->
{{% github href="10_basic/20_packaging/todo-server/build.gradle#L17-L19" %}}build.gradle{{% /github %}}
{{< highlight go "" >}}
springBoot {
    executable = true
}
{{< / highlight >}}
<!-- /snippet:backend_executable -->


## Final assembly

Now that both front- and backend are build by gradle and have appropriate dependencies we can run `./gradlew assemble` to create a single executable jar out of it:

```
$ ./gradlew assemble
[...]
BUILD SUCCESSFUL in 11s
``` 

The result can directly be executed from command line:

```
$ ./todo-server/build/libs/todo-0.0.1.jar

2018-01-29 21:29:27.684  INFO 32609 --- [           main] io.pelle.todo.TodoApplication            : Starting TodoApplication on fry with PID 32609 (/home/pelle/git/learn.pelle.io/artefacts/10_basic/20_packaging/todo-server/build/libs/todo-0.0.1.jar started by pelle in /home/pelle/git/learn.pelle.io/artefacts/10_basic/20_packaging/todo-server/build/libs)
2018-01-29 21:29:27.690  INFO 32609 --- [           main] io.pelle.todo.TodoApplication            : No active profile set, falling back to default profiles: default
2018-01-29 21:29:27.856  INFO 32609 --- [           main] ationConfigEmbeddedWebApplicationContext : Refreshing org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@5a2e4553: startup date [Mon Jan 29 21:29:27 CET 2018]; root of context hierarchy
2018-01-29 21:29:31.035  INFO 32609 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat initialized with port(s): 9090 (http)

[...]

2018-01-29 21:29:36.776  INFO 32609 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
2018-01-29 21:29:36.879  INFO 32609 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 9090 (http)
2018-01-29 21:29:36.893  INFO 32609 --- [           main] io.pelle.todo.TodoApplication            : Started TodoApplication in 9.827 seconds (JVM running for 10.612)

```

The application is now available at [http://localhost:9090](http://localhost:9090).