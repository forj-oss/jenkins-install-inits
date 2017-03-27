# tcp-slave-agent-port

This script will force to use a predefined slave agent port.
It uses `JENKINS_SLAVE_AGENT_PORT` environment variable to set it correctly.

If `JENKINS_SLAVE_AGENT_PORT` is not set, the groovy script do nothing.

# Docker case

This is typically used in the context of docker.
In a Dockerfile, you would write this:

    ENV JENKINS_SLAVE_AGENT_PORT 50000
    EXPOSE 50000

