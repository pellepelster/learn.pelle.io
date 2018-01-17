---
title: "Deploying the application"
date: 2017-12-11T21:35:04+01:00
weight: 20
disableToc: true
---

Now that we have a deployable artefact we can finally move forward and start deploying the application. As we currently have have no on-premise hardware at hand we are going to use one of the major cloud providers to deploy the first iteration of our game-changing application.

### Infrastructure as code
The obvious next step is to create an account with the cloud provider of our choiche, spin up a server, upload and run our application on it. But before we start clicking around in the AWS/GCE/Azure console lets step back for a moment and think about what our infrastructure will look like now and in the future.

For now we would propably get away with handcrafting a single server to run our application. But when we think about the future, about things like scaling, disaster recovery, different enviroments, automated deployments, documentation and auditing it becomes quite clear that doing all those things manually we have to give up the flexibility and speed gained by moving into the cloud and would be back in the old days when we installed physical hardware in racks and configured them manually, only without the part of lifting heavy servers over our heads until someone tightens the screws.

With the availbility of tools like [Terraform](https://terraform.io), [Ansible](https://www.ansible.com/overview/how-ansible-works) or [Puppet](https://puppet.com/products/how-puppet-works) we are now able to define all needed resources in the form of a descriptive language that we can handle like any other piece of sourcecode. Being able to create and destroy new server instances any time without any manual intervention gives us several advantages.

#### Scaling
When we are able to spin up new instances without any manual intervention scaling becomes a problem of the past. Spinning up new instances comes at virtually no cost and we are able to create or dispose instances depending on the current load

#### Ephemeral systems (MTTR)
In case of a instance failure we can now afford to just kill the faulty instance and spin up a replacement greatly reducing the time to recovery. In case the outage was introduced by a change we made, we now can also easily roll back our infrastructure by rolling back the change we made.

#### Consistency
Configuration errors are less likely to occur when the configuration comes from source code instead from manual steps performed by humans. Every new server will be an exact replica of the previous one. Special snowflake servers with specific quirks are a thing of the past as well (as long as everyone resists the urge to log into the server and change things by hand)

#### Traceability
Like your sourcecode your infrastructure code is version controlled allowing us to trace configuration changes by looking at version control logs. Basically the version control log is now the infrastructure changelog.

#### Documentation
The infrastructure sourcecode is now also the single source of truth when it comes to infrastructure documentation. No more outdated documents or stale wikis: The sourcecode 100% reflects the current state of your environment.

#### Continuously Delivery
The ability to automatically create whole environments enables us the continously test infrastructure changes. Every change can be picked up by a [deployment pipeline](https://martinfowler.com/bliki/DeploymentPipeline.html) and deployed into a seperate test environment by a CI server before being applied to the production envrionment.
