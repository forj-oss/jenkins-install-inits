# mesos-plugin auto-configuration

This version provides basic need to properly configure an initial mesos cloud configuration.

It requires mesos plugin to be installed.

# How to use it?

- Add `mesos` in your groovy list of your project
- Add `MESOS_MASTER` variable to your jenkins ENV startup
> Ex: From jenkins-mesos-dood:0.24.1 docker image, you should provide a `jenkins_credentials.sh` mounted with at least `MESOS_MASTER`
> But you can also provide it as a -e to the docker run command.


# Environment variables

- `MESOS_MASTER`             : Can be any valid mesos master configuration `zk://<Server:Port>[,<Server:Port>[...]]/mesos` or simple url.
- `MESOS_SLAVE_DOCKER_IMAGE` : Can be any docker image, with a java 7 or higher installed.

# TODO

Add following feature:
- Support for updating mesos plugin if something has changed in the configuration - FORCE config from code.
- `MESOS_SLAVE_DOOD`     : Boolean. True if the image is supporting dood. By default, /var/run/docker.sock and /usr/bin/docker are mounted.
- `MESOS_DOCKER_SRC_BIN` : String. Optional. Host path to the docker binary. It must be a valid static binary from docker. Default is /usr/bin/docker
- `MESOS_DOCKER_DST_BIN` : String. Optional. Where the docker binary is mounted in the container. Default is /usr/bin/docker
- `MESOS_DOCKER_SOCK`    : String. Optional. Host Socket path. Will be mounted as /var/run/docker.sock

- Any other parameters found in the plugin.

The FORJ Team
