---
title: "Welcome"
date: 2017-12-11T21:34:57+01:00
---

# Welcome

This guide will lead you step by step through the process of packaging, deploying, monitoring and operating a state of the art web application within a cloud environment. Each step comes with a complete runnable sample so you can see the code evolve through the different stages. Depending on your interest or prior knowledge you should be able to either follow this guide step by step or directly dive into a topic of your interest.


## Prerequisites

In order to reproduce the examples in this guide you will need the following tools available on your machine.

- Git for checking out the example source code
- Terraform to provision the needed infrastructure 
- A Java SDK is needed to compile the application

All tools should be available by your package manager in case of Linux or by [brew](https://brew.sh/) if you work on a Mac. In case something is missing have a look at the following links for installation instructions:

- [Install Git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)
- [Install Terraform](https://www.terraform.io/intro/getting-started/install.html)
- [Install Java JDK 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)

Additionally you will need to create an account for Amazon Web Services which can be done [here](https://portal.aws.amazon.com/billing/signup#/start).

{{% notice warning %}}
AWS offers a free trial for most of the services for the first 12 months (of course with usage/instance size limits). This guide tries to use as much of this free tier as possible, but please be aware that costs may occur.
{{% /notice %}} 


## Conventions

### Source code
If source code is available for a specific topic a link inside the breadcrumb panel on the top of the page will directly link to it.  

{{< figure src="breadcrumbs_sources_link.png" title="Breadcrumb Source Link" >}}

### Snippets

The source code for each step is available at [https://github.com/pellepelster/learn-artefacts](https://github.com/pellepelster/learn-artefacts). On the top of each source code snippet a link points directly to the source file containing the snippet.

{{% github href="10_basic/10_the_assignment/todo-server/src/main/java/io/pelle/todo/TodoApplication.java" %}}TodoApplication.java{{% /github %}}
{{< highlight go "" >}}
class TodoApplication {
  // [...]
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
