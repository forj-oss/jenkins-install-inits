#!/bin/bash
#
# Initialize 2 ENV variables for yet-another-docker-plugins feature to support the swarm/UCP mode.
#
# It requires :
# - jenkins master to be started from docker with the overlay network
# - jenkins master port to be 8080 (Usually the case on jenkins images.
# - feature `yet-another-docker-plugins` installed.
#
# variables set :
# - YADP_CREATE_NETWORK_MODE to ontainer:<id>
# - ADP_LAUNCH_JNLP_JENKINS_URL to http://<id>:8080

if [ -n "$JENKINS_DATA_REF" ]
then
   JENKINS_HOME="$JENKINS_DATA_REF"
   JENKINS_START_D_DIR="$JENKINS_HOME/jenkins.start.d"
   echo "--- $0: Using Jenkins reference '$JENKINS_DATA_REF'"
fi

if [ -z "$JENKINS_HOME" ]
then
   echo "--- $0: Missing JENKINS_HOME or JENKINS_DATA_REF. One or the other must be set." >&2
   exit 1
fi

if [ -z "$JENKINS_START_D_DIR" ]
then
   JENKINS_START_D_DIR=$JENKINS_HOME/jenkins.start.d
fi

mkdir -p $JENKINS_START_D_DIR


echo 'export YADP_CREATE_NETWORK_MODE="container:$HOSTNAME"
export YADP_LAUNCH_JNLP_JENKINS_URL="http://${HOSTNAME}:8080"' > $JENKINS_START_D_DIR/swam-scaling-source.sh

