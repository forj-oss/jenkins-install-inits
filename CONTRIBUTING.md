# Introduction

If you have any way to pre-initialize/maintain your jenkins instance, it could be interesting to share it with others.

To do it, just fork, update and create a pull request.

# How to automate (initialization/maintain) your jenkins service from code?

Jenkins offers a lot of plugins to enhance jenkins and your jobs with a lot of tools.
But as of now, it do not provide an easy way to do it completely from a code perspective.

So, that's why this repository and [jenkins-ci repository](https://github.com/forj-oss/jenkins-ci) has been created. It helps to create/maintain a jenkins docker image from code.

## Basics:

Here, I'm listing globally where Jenkins could be interested, for a Jenkins as Code perspective.

- Jenkins stores most of its global configuration in xml files. (config.xml or lot of plugins config xml files)
- Jenkins, at startup time, can execute some groovy code located in `$JENKINS_HOME/init.groovy.d`.
- At runtime, while using the `groovy` plugin, jenkins can execute some groovy code and maintain jenkins global parameters/plugins up to date.
- At runtime, while using the `Jobs-dsl` plugin, we can maintain the list of jobs and install pipelines as usual with Jenkins pipeline.
- Some external project can communicates with jenkins API to update the list of jobs. Equivalent to Jobs-DSL. Ex: JenkinsJobsBuilder

### Discussion on how to automate(start up/runtime) your jenkins Installation from a code Perspective.

Usually, when you start playing with Jenkins, you update it directly from the webpages. Then Jenkins store all configuration in XML configuration files.
So, you will think on managing those configuration files. Ie you create XML templates, to be able to rebuild those configuration files.

Then when you have a data update, like an SSH private key change, you will re-generate your configuration file, even from jenkins itself (dedicated job) and expect to have it reloaded in jenkins.
But jenkins do not reload them, even with Jenkins/Manage Jenkins/Reload Configuration From Disk (or Jenkins.instance.reload())
Only jobs are reloaded.
The only way to reload it is to restart jenkins.

So the XML template case is a great start. It partially answer to the Jenkins as Code requirements:
* Configuration stored in an SCM - ![YES](/images/yes.png)
* Configuration templatizable - ![YES](/images/yes_but.png), <br>
  Everything is feasible, right? **But** not necessarily easily: <br>
  XML is not really easy to read. No documentation to create them from scratch (like a source code).
  All could be done from Jenkins UI and requires to analyze those XML generated files to templatize them.
  So, usually, we must start a Jenkins instance to generate the initial XML config and assume that we did it all correctly to templatize them later.
* Configuration loaded at startup - ![YES](/images/yes.png)
* Configuration loaded at runtime - ![YES](/images/yes.png) and ![NO](/images/no.png).<br>
  Jobs can be reloaded. But global configuration is never reloaded, except if you restart Jenkins.

So, if you need to ensure that Jenkins can be updated at runtime, you must write some java code.
Instead of maintaining a collection of XML files that your java code could ask to reload, mainly because of container context, I used Environment variables.
This is a choice. But we can write other description files to really describe what we need. Yaml is my preference. Not implemented today in my code...
But you can do that way for your code. You must write your feature README.md file to explain this to users.

**So, now, it is your turn!! Create your own Jenkins Feature!**
If so, then you will certainly need to write some java code/groovy code that can be run at start up (init.groovy.d) and runtime (groovy plugin)

Following are some help/hints to develop your Feature!

# Writing a Jenkins Feature

So, The idea is to create a collection of groovy files/plugins files definition/shell scripts. This collection is called a `Jenkins feature`

All features are maintained in this repository and organized to be fully open to contributions.
But you can create your own repository, or even create your feature in your private repository. This will be explained later.

To create a new `Jenkins feature` in this repository, you need to create a directory and a description file.
The name of this directory and the description file name are the name of the `Jenkins feature`.

Ex: For a new `github` feature, I need to create github/github.desc.

The Description file is a text file, where each line can be a combination of:
- `groovy:<Groovy FileName>` -  `Groovy Filename` is the relative path to a groovy file WITHOUT .groovy extension.
- `plugin:<PluginName[:Version]>` - Name of the plugin to install. `Version` is optional
- `shell:<ScriptName>` - shell script to execute to install some libraries/files (yum/apt-get/dnf/...).

## Testing your code

If you want to test your own feature, you can set it with `JENKINS_INSTALL_INITS_URL` to your fork.

**WARNING!** Ensure following:
* your path ends with '/'
* The url should be formed as `<protocol>://<github Server>/<Organization>/<Repository>/raw/<branch>/`

Ex: In a Dockerfile
    ENV JENKINS_INSTALL_INITS_URL=https://github.com/MyFork/jenkins-install-inits/raw/MyBranch/

## Hint about writing Groovy code

Most of the time, groovy code are written for plugins configuration like setting mesos services, docker servers, etc...

If you want to write your plugin groovy setup, here are some hint to determine/debug/write it:

### Start jenkins plugin with. (DRAFT)

As Jenkins-ci is a docker image, it is easy to boot one in your workstation.

* Clone your jenkins-ci repository
* update the features.lst to add your new plugin to add as new feature.
* set the admin password at jenkins container boot time.

    $ docker run -it --rm MyJenkinsCI

* log in
* Open http://localhost:8080/script (update link to your case if needed)

This window gives you the opportunity to query/test your groovy code.

### Using IDE.

I'm not fully using IDE for those code, but that can make sense any way.
I believe for example that Idea could help a lot. I'm using it for other projects...
I'll share any experimentation I'll make. But if you have some experiences
share it! Thank you!

## Clone the plugin source code

### Analyzing the plugin source code

In order to identify how to update the plugin to make it work, usually, you have a **GlobalConfiguration** derived class.

Ex: Sonar : [SonarGlobalConfiguration.java at line 44](https://github.com/SonarSource/jenkins-sonar-plugin/blob/master/src/main/java/hudson/plugins/sonar/SonarGlobalConfiguration.java#L44)

And you could see how this object is getting populated.

In this Sonar example, they load it from a SonarGlobalConfiguration().

Then an interesting function is found just below. `getInstallations()`

So, you could query Jenkins to see how to get information about this.
We get the Package name and the class where resides this function and boom, you have the internal collection of sonar servers declaration.

    println(Jenkins.instance.getDescriptor('hudson.plugins.sonar.SonarGlobalConfiguration').getInstallations())

So, usually, from that point, you may found the appropriate constructor, then add the object to the collection.
It should be something like:

    Jenkins.instance.getDescriptor('hudson.plugins.sonar.SonarGlobalConfiguration').getInstallations().add(MyInstallation)

## Analyzing the XML configuration file

It is not true all the time, but if the plugin generates a plugin.<pluginName>.xml config file, it could give you the name of Class used.
It could help you pointing out the list of object that needs to be updated/created.

## Querying jenkins

* list of plugins installed:

    println(Jenkins.instance.pluginManager.plugins)

* Get the Class name of an object

    println(Jenkins.instance.getClass())

* Get list of accessible Object defined Methods in the Jenkins Instance

    println(Jenkins.instance.getClass().getDeclaredMethods().join('\n'))

* Get list of accessible Methods in the Jenkins Instance

    println(Jenkins.instance.getClass().getMethods().join('\n'))

* Get the Registered Plugin instance registered in Jenkins.

    println(Jenkins.instance.getDescriptor('hudson.plugins.sonar.SonarGlobalConfiguration'))
