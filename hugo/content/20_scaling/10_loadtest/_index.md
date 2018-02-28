---
title: "Loadtesting"
date: 20187-02-27T10:00:04+01:00
weight: 10
disableToc: true
showHeaderLink: true 
---

We successfully deployed the application and it is humming along just fine. To gain more users the marketing department decided to air some TV commercials which puts us in a delicate position as we have no idea how much users the current deployment can withstand. Before we blindly add some new instances lets gather some information and run a load test to see how many users we currently can cope with.

## Load Generator
There is a sheer endless portfolio of load testing tools available, for starters we will begin with [Gatling](https://gatling.io/) that lets us define the load using a Scala based DSL and is very easy to integrate in a CI environment later on if we want to.

