---
title: "Welcome"
date: 2017-12-11T21:34:57+01:00
---

# Welcome
This guide will lead you step by step through the process of packaging, deploying, monitoring and operating a web web application in a cloud environment. Each step comes with a complete runnable sample so you can see the code evolve through the different stages. Depending on your interest or prior knowledge you should be able to either follow this guide step by step or directly dive into a topic of your interest.

## Conventions

### Sources
At the top of each page inside the breadcrumb panel  

### Snippets
The sourcecode for each step is available at [https://github.com/pellepelster/learn-artefacts](https://github.com/pellepelster/learn-artefacts). On the top of each sourcecode snippet a link points directly to the source file containing the snippet.

{{% github href="hugo/content/basic/packaging/_index.md" %}}snippet{{% /github %}}
{{< highlight go "linenos=table" >}}
class ExampleClass {

}
{{< / highlight >}}

### Shell commands
Lines beginning with a `$` sign are commands that can be executed, where the base directory for each command is always the source directory for the current topic.

```
$ this is a shell command

This is the commands output
```

## Topics
{{%children style="h2" description="true" depth="1" %}}
