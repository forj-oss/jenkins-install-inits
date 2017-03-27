#!/bin/bash -e
#
# Script to start as root on a CentOS container
#
# TODO: Adapt to several different image type (deb/rpm/apk)
#

MESOS_VERSION=0.24.1

rpm -i http://repos.mesosphere.io/el/7/noarch/RPMS/mesosphere-el-repo-7-1.noarch.rpm
yum -y install mesos-$MESOS_VERSION
yum clean all
