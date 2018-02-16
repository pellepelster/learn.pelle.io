# general style
- we, active voice
- cover theoretical concepts but focus on hands on help
- provide verified links for further reading
- examples ideally copy/paste ready to reuse
- all examples must be tested and runnable
- iterate, start as simple as possible and slowly refine
- several main tracks that cover various aspects of deployment with detailed sub-steps

# example project
- vue.js frontend
- spring boot backend
- db abstraction layer with in-memory db (migrate to real rds persistence at some point)
- packer

# open questions
- figure out where to introduce docker/testing?
- docker would need registry -> overhead maybe to big/use aws ecr registry?
- testing infra vs. docker images

# track basic
- create initial setup, after this track it should be possible to go into any direction
- explain the environment and setting
- tool setup (terraform, aws, gradle, ide)
- first simple single instance deployment

## topic packaging
- use gradle for building front/backend
- single runnable (executable) artifact

## basic deployment
- basic vpc
- single instance with terraform provisioning
- executable project as systemd unit

# track scaling

## topic elb
- initial elb setup
- two static instances (provisoned via terrafornm)
- maybe via terraform modules/loop

## topic launch config
- prepare next step by introducing instance setup via launch config
- artifacts on s3
- two static instances (created via launch config)

## topic scaling (asg)
- autoscaling via elb
- scaling based on instance load
- provide stress test to provoke scaling? (gatling?)

# track project hygiene?

## runfile?
- automate previous tasks in runfile

## linting?
- ?

# track security

# credentials
- get rid of root credentials
- move needed credentials into pass

# multi account
- iam/resources account
- assume roles

# bastion
- setup bastion host

# track reliabilty

## multi az
- distribute soultion do different az

## logging
- logging to cloudwatch

# monitoring
- implement healthcheck (db connection)
- implement custom metrics for instance/application state

## alarms
- create alaerm based on metrics/logfiles
