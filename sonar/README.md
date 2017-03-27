# sonarQube

This feature install and configure your SonarQube plugin in Jenkins.

# What the feature do?

It installs 
- SonarQube Jenkins Plugin version 2.4
- A collection of groovy files.

# Configuring the feature

This plugin is divided in several pieces:
- Global server Configuration
- MS Runner configuration
- Runner configuration
- Jobs configuration

Currently this feature implements a basic pre-initialized plugin configuration. If some features/data are missing, consider providing a code update and submit a Pull Request.

Following Configution has been implemented:
- Global server Configuration (NAME/URL/TOKEN) For SonarQube 2.6 or higher
- Runner configuration (NAME/VERSION)

To configure them, set following environment variables:
- `SONAR_NAME` : Required. Name of the Sonar Server config in Jenkins. Ex: SonarQube
- `SONAR_URL`  : Required. URL to the SonarQube server. Ex: http://sonar.corp.hpecorp.net:9000
- `SONAR_TOKEN`: Optional. Token string to connect to Sonar server.

- `SONAR_RUNNER_NAME`    : Configuration name of the Sonar runner installation in Jenkins.
- `SONAR_RUNNER_VERSION` : Version of the Sonar runner to install from maven repository.

RnD IT & FORJ Team
