# Basic security

This script will minimally configure your jenkins security with
- Admin account and a default password
- Anonymous can only read jobs with logs.

# Information about jenkins 2.x

Since Jenkins version 2.x, a new wizard is going to be started. 
This wizard is started if:
- no admin account is defined in jenkins security
  See [source](https://github.com/jenkinsci/jenkins/blob/58ba65cacf743ed070ef0b63ea06521bef5e22d4/core/src/main/java/jenkins/install/SetupWizard.java#L98)
- File jenkins.install.UpgradeWizard.state reports an old version of jenkins for upgrade.
  See [Source](https://github.com/jenkinsci/jenkins/blob/58ba65cacf743ed070ef0b63ea06521bef5e22d4/core/src/main/java/jenkins/install/SetupWizard.java#L266)

This feature creates automatically an `admin` user with a default password. 
As soon as this feature is activated, the wizard is disabled.

# TODO

- Setting our own password from any kind of external services (vault/...)
