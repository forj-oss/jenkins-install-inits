# jenkins-install-inits

This repository contains a collections of Jenkins features that can be installed and maintained in your Jenkins instance.
It can be implemented in a container context or not.

A feature is a combination of :
- plugins. If a plugin has other plugins dependency, those plugins will be installed automatically.
- groovy scripts. Those script are executed at jenkins startup to pre-initialize jenkins settings and plugins.
  They are also called on Demand (like GitHub merge Pull request) to update your Jenkins instance at runtime.
  You can also call them frequently to ensure Jenkins is always configured the same way!
  This is done by the groovy plugin.
  (See [Groovy plugin page](https://wiki.jenkins-ci.org/display/JENKINS/Groovy+plugin)
- shell scripts. Those scripts can install some Jenkins external tools, like maven, mesos library, etc...

# Why this repository could interest me?

Are you searching for a DevOps way to manage your Jenkins instance???

In short, you can manipulate xml files and install plugins, but those files are active only when you restart Jenkins.
By building a collection of Groovy code combined with plugins and shell scripts, it gives you the ability to update Jenkins dynamically without restarting it and breaking your project build factory.

You can read a more detailled discussion about [how to manage Jenkins from a Code perspective here](CONTRIBUTING.md#discussion-on-how-to-automatestart-upruntime-your-jenkins-installation-from-a-code-perspective)

In a DevOps context, be able to have a Build factory controlled by code is key.
If you have any other ideas to make this paradigm a reality for you, share it!!! The approach given here is not necessarily the only correct one. So feel free to discuss it through issues.

# How to use it?

To use it in your jenkins instances or containers, do the following:

- Download the `jenkins-install.sh` with:

Code example:

    $ curl -sSL -o jenkins-install.sh https://github.com/forj-oss/jenkins-install-inits/raw/master/jenkins-install.sh
    $ chmod +x jenkins-install.sh

- Create a text file `jenkins_features.lst`. Enter a feature name (prefixed by `feature:`) or plugin name (prefixed by `plugin:`) per line.

- Call jenkins-install.sh with your file

## About `jenkins_features.lst`Where is the Controller box? Do we have one?

Each line prefixed by `feature:` or `plugin:` will describe an installation task

### plugin:
When prefixed by `plugins`, the script will download the plugin from `$JENKINS_UC/download/plugins` (JENKINS\_UC=https://updates.jenkins-ci.org) to
`$JENKINS_HOME/ref/plugins`

Plugins dependencies are automatically downloaded.

### feature:
When prefixed by `feature:`, the script with download a feature description file.
This file defines :
- The name of groovy scripts to install under `$JENKINS_HOME/ref/groovy.init.d`. Identified with `groovy:`
- Plugins to install. Plugins dependencies are automatically downloaded. Identified with `plugin:`
- shell scripts to execute some OS/container side installation, like some libraries/files (yum/apt-get/dnf/...). Identified with `shell:`

Example:

    $ jenkins-install.sh jenkins_features.lst

By default, it will install groovy code and plugins from listed script in `$JENKINS_HOME/ref/...`. You can add a different destination path if you need.

    $ jenkins-install.sh jenkins_features.lst /home/jenkins

# Contribute to this repo

If you have developed some groovy/shell scripts that can interest any other jenkins users, I would suggest you to contribute to this repo.

In anyway, if you believe that you can provide something to this project, have a look in the [contribution document](CONTRIBUTING.md) to get help and technics to make your contribution a success! Thank you!


R&D DevOps IT - FORJ Team

