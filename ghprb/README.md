# Github Pull Request Builder

This feature implements the ghprb (Github Pull Request Builder) jenkins plugin and help to configure it.

# How to use it?

- Add `feature:ghprb` in your features.lst in your project (Dockerfile or mount)
- Add `GITHUB_API_URL` in the jenkins environment startup.

Ex: With Docker:

In your Dockerfile, you can add `ENV GITHUB_API_URL=https://github.{YourOrg}.com/api/v3/`
Or you can add it in your docker run , with `--env GITHUB_API_URL=https://github.{YourOrg}.com/api/v3/`

(For public Github, use `ENV GITHUB_API_URL=https://api.github.com`)

Ex: Without docker:

You can set the `GITHUB_API_URL`  before starting jenkins: (assume you have a jenkins startup script)

`GITHUB_API_URL=https://api.github.com/ start_jenkins.sh`

# !!! Warning !!!

Currently, this code assume you have a `github` credential already installed. This one is created by the seedjob.
As soon as the credential code part is moved out from `seed-job`, this code will be based on the new `credential feature` as a dependency.

# Environment variables

- `GITHUB_API_URL` : Optional. Url of your github entreprise. If not set, https://github.com will be used.

# TODO

Add following feature:

- Add any other extra parameters that can be set, like `jenkinsUrl`, `credentialsId`
