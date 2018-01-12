---
title: "Packaging the application"
date: 2017-12-11T21:35:04+01:00
weight: 10
disableToc: true
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
The Gradle wrapper bootstraps the Gradle environment needed for the build and remove the burden to install Gradle from the user. This helps to make the build more portable and less dependent on the local setup.
{{% /notice %}}

In a multi project we need a file `settings.gradle` which tells Gradle what subprojects are part of the build:

<!--file:todo_project/settings.gradle-->
{{% github href="hugo/content/basic/packaging/_index.md" %}}settings.gradle{{% /github %}}
{{< highlight go "linenos=table" >}}
include 'todo-server', 'todo-frontend'
{{< / highlight >}}
<!--eof:todo_project/settings.gradle-->

Additionaly we create a `build.gradle` to define everything that is common for all submodules of the build:

<!--file:todo_project/build.gradle-->
{{% github href="hugo/content/basic/packaging/_index.md" %}}build.gradle{{% /github %}}
{{< highlight go "linenos=table" >}}
allprojects {

    repositories {
        mavenCentral()
    }

    apply plugin: 'idea'
    apply plugin: 'eclipse'

    group = "io.pelle"
}
{{< / highlight >}}
<!--eof:todo_project/build.gradle-->


### Package the frontend

As already mentioned it is vitally important for the build not to rely on any local prerequistes on the machine that executes the build. Keep in mind that the build has to work on your machine, your coworkers machine or in a continus integration environment. Luckily for us a [node.js plugin](https://github.com/srs/gradle-node-plugin) for Gradle already exists that creates a node.js environment for us, we just have to apply the plugin from the public Gradle maven repository:

<!--snippet:frontend_nodejs_plugin_dependency-->
{{% github href="basic/packaging/todo-frontend/build.gradle#L1-L9" %}}build.gradle{{% /github %}}
{{< highlight go "linenos=table,linenostart=1,hl_lines=" >}}
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
<!--eos:frontend_nodejs_plugin_dependency-->

The plugin can be configured to a specific node.js/npm version:

<!--snippet:frontend_nodejs_plugin_configuration-->
{{% github href="basic/packaging/todo-frontend/build.gradle#L16-L22" %}}build.gradle{{% /github %}}
{{< highlight go "linenos=table,linenostart=16,hl_lines=" >}}
node { 
  version = '6.3.1'
  npmVersion = '4.0.1'
  distBaseUrl = 'https://nodejs.org/dist'
  download = true
  workDir = file("${project.buildDir}/nodejs")
} 
{{< / highlight >}}
<!--eos:frontend_nodejs_plugin_configuration-->

Now we create a new task that executes the npm build:

<!--snippet:frontend_nodejs_build-->
{{% github href="basic/packaging/todo-frontend/build.gradle#L24-L28" %}}build.gradle{{% /github %}}
{{< highlight go "linenos=table,linenostart=24,hl_lines=" >}}
task frontendBuild(type: NpmTask) { 
	args = [ 'run', 'build' ]
}

frontendBuild.dependsOn('npmInstall') 
{{< / highlight >}}
<!--eos:frontend_nodejs_build-->

and package the resulting file in a jar file:

<!--snippet:frontend_nodejs_jar-->
{{% github href="basic/packaging/todo-frontend/build.gradle#L30-L37" %}}build.gradle{{% /github %}}
{{< highlight go "linenos=table,linenostart=30,hl_lines=" >}}
 task frontendJar(type: Jar) { 
 	appendix = 'frontend'
 	into 'frontend'
 	from fileTree('./dist/')
 	destinationDir file(project.buildDir)
}

frontendJar.dependsOn('frontendBuild')
{{< / highlight >}}
<!--eos:frontend_nodejs_jar-->

The last step is to add the resulting jar to a [Gradle configuration named](https://docs.gradle.org/current/userguide/dependency_management.html#sub:configurations) `frontend` so dependent projects (in our case the frontend-server project) can include the frontend artifacts in their build cycle:

<!--snippet:frontend_nodejs_gradle_config-->
{{% github href="basic/packaging/todo-frontend/build.gradle#L39-L40" %}}build.gradle{{% /github %}}
{{< highlight go "linenos=table,linenostart=39,hl_lines=" >}}
configurations { frontend }
artifacts { frontend frontendJar } 
{{< / highlight >}}
<!--eos:frontend_nodejs_gradle_config-->

### Package the backend

As the backend is already built with Gradle we only need some minor modifications. The `spring-boot-gradle-plugin` we are using already provides a task that creates a fat jar file containing all dependencies needed to run the application. To also  serve that static files for the frontend we need to add a dependency to to frontend build we just created:

<!--snippet:frontend_backend_dependency-->
<!--eos:frontend_backend_dependency-->

Now that the static `frontend.jar` is packaged in our application we only have to tell Spring Boot to serve this files:

<!--file:todo_project/todo-server/src/main/java/io/pelle/todo/FrontendContent.java-->
{{% github href="hugo/content/basic/packaging/_index.md" %}}FrontendContent.java{{% /github %}}
{{< highlight go "linenos=table" >}}
package io.pelle.todo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
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
<!--eof:todo_project/todo-server/src/main/java/io/pelle/todo/FrontendContent.java-->
