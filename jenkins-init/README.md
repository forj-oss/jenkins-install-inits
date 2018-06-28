# jenkins_init

This feature defines basic jenkins configuration like jenkins hostname.

To configure basic security, think about [basic-security](../basic-security) feature.

# How to use it?

- Add `jenkins-init` in your features.lst and build your docker image, or run the [jenkins_install.sh](../jenkins_install.sh) on your jenkins server.
- Add one or more variable to predefine your jenkins installation.
  - `JENKINS_URL` : Define the official jenkins URL you want.
  - `JENKINS_NB_EXECUTORS` : Number of executor in the master. Set to 2 by default.
  - `JENKINS_LABELS` : Define the jenkins labels. Set to 'forjj' by default so that forjj can start his own dedicated job on master.

jenkins-init feature is going to be executed at the earliest boot time compare to other features (based on groovy file name sort)

# TODO

This is a first version. If you believe you need another predefined jenkins configuration set from code, consider to [contribute](../CONTRIBUTING.md).

RnD IT & FORJ team
