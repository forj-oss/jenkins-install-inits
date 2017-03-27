#!/bin/bash
#
# This script will read features text file (like 'features.lst') and download/install groovy code, plugins and prossible execute shell instructions.
#

function do_install
{
 echo "Downloading $1:$2"
 case "$1" in
  plugin)
    do_install_plugin $2 $3
    ;;
  feature)
    do_install_feature $2
 esac
 # If underlying script has exited with an error, the exit has exited the `cat | while do` subprocess but not the entire script process (parent).
 RET=$?
 if [ $RET -ne 0 ]
 then
    exit $RET
 fi
}

function do_install_plugin
{
 set -e
 version=$2
 [[ -z ${2} ]] && version="latest"

 if [ -z "$JENKINS_UC_DOWNLOAD" ]; then
   JENKINS_UC_DOWNLOAD=$JENKINS_UC/download
 fi
 if [ $version != latest ] && [ -f $DEST_PATH/plugins/${1}.hpi ]
 then
    download=false
    download_new_version $1 $2
    if [ $? -eq 1 ]
    then
       download=true
    fi
    set -e
 else
    download=true
 fi

 if [ $download = true ]
 then
    echo "Downloading plugin ${1}:${version}"
    rm -f $DEST_PATH/plugins/${1}.hpi
    mkdir -p $DEST_PATH/plugins
    curl -sSL -f ${JENKINS_UC_DOWNLOAD}/plugins/${1}/$version/${1}.hpi -o $DEST_PATH/plugins/${1}.hpi
    if [ ! -f $DEST_PATH/plugins/${1}.hpi ]
    then
       echo "${1}.hpi not downloaded."
       exit 1
    fi
    unzip -qqt $DEST_PATH/plugins/${1}.hpi
    plugin_deps $1
    set -e
 fi
 set +e
}

# Function to help sort -V to order <Version>-.* or <Version>_.* to be sorted before <Version>
# Ex: 1.2-beta4 must be before 1.2, so sort -V will consider 1.2 as the most recent version.
# Ex: 1.0 vs 1.1 where 1.0 must be before 1.1
# Ex: 1.0.2 vs 1.0 where 1.0 must be before 1.0.2
# Note that 1.2.1 and 1.2_alpha are not ordered as we would like. Assume this _ is not too much used...
function version_order
{
  if [ "$(echo "$1" | grep -e '[1-9.][_-].*')" = "" ]
  then
     echo "$1-zfinal $1"
  else
     echo "$1 $1"
  fi
}

function download_new_version
{
# Return true if the version is newer than already downloaded one.
 unzip -q $DEST_PATH/plugins/$1.hpi META-INF/MANIFEST.MF -d /tmp
 CUR_VERSION="$(awk '$1 ~/Plugin-Version/ { print substr($2, 1, length($2)-1) }' /tmp/META-INF/MANIFEST.MF)"
 rm -fr /tmp/META-INF
 if [ "$CUR_VERSION" = $2 ]
 then
    echo "Plugin $1:$CUR_VERSION already downloaded."
    return
 fi
 if [ "$(printf "$(version_order $CUR_VERSION)\n$(version_order $2)\n" | sort -V | cut -d" " -f 2 | tail -n 1)" = $2 ]
 then
    echo "Updating plugin $1:$CUR_VERSION => $2"
    set +e
    return 1
 else
    echo "Plugin $1 already updated to $CUR_VERSION and not downgraded to $2"
 fi
}

function plugin_deps
{
 DEPENDENCIES="$(plugin_list_deps $1)"
 if [ -z "$DEPENDENCIES" ]
 then
    return
 fi

 echo "Dependencies found: $DEPENDENCIES"
 for plugin in ${DEPENDENCIES//,/ }
 do
   plugin="${plugin/;*/}"
   details=(${plugin//:/ });
   do_install_plugin ${details[0]} ${details[1]}
 done
}

function plugin_list_deps
{
 unzip -q $DEST_PATH/plugins/$1.hpi META-INF/MANIFEST.MF -d /tmp
 awk '$1 ~/Plugin-Dependencies:/ {
   RESULT=substr($0, index($0, ": ")+2, length($0)-2-index($0, ": "));
   getline NEXT_LINE;
   while (substr(NEXT_LINE,0,1) == " ")
     {
      RESULT=sprintf("%s%s", RESULT, substr(NEXT_LINE, 2, length(NEXT_LINE)-2));
      getline NEXT_LINE;
     }
   print RESULT;
  } ' /tmp/META-INF/MANIFEST.MF

 rm -fr /tmp/META-INF
}

function do_install_groovy
{
 set -e
 mkdir -p "$DEST_PATH/init.groovy.d"
 echo "Downloading '$2.groovy' from '$GROOVIES_BASE_URL$1' ..."
 curl -sSL $GROOVIES_BASE_URL$1/$2.groovy -o "$DEST_PATH/init.groovy.d/$2.groovy"
 if [ "$(cat "$DEST_PATH/init.groovy.d/$2.groovy" | grep -e '{"error":"Not Found"}' -e '<!DOCTYPE html>')" != '' ]
 then
    echo "Warning!!! $2.groovy from '$GROOVIES_BASE_URL$1' was not found. File ignored." >&2
    rm -f "$DEST_PATH/init.groovy.d/$2.groovy"
 fi
 set +e
}

function do_install_and_run_shell
{
 echo "Downloading and running '$2' from '$GROOVIES_BASE_URL$1' ..."
 curl -sSL $GROOVIES_BASE_URL$1/$2 -o "/tmp/$2"
 if [ "$(cat "/tmp/$2" | grep -e '{"error":"Not Found"}' -e '<!DOCTYPE html>')" != '' ]
 then
    echo "Warning!!! $2 from '$GROOVIES_BASE_URL$1' was not found. File ignored." >&2
 else
    bash "/tmp/$2"
    RET=$?
 fi
 if [ $RET -ne 0 ]
 then
    echo "$2 return $RET error code. Aborted."
    exit $RET
 fi
 rm -f "/tmp/$2"
}

function do_install_feature
{
 curl -sSL $GROOVIES_BASE_URL$1/${1}.desc -o "$DEST_PATH/${1}.desc"
 if [ "$(cat "$DEST_PATH/$1.desc" | grep -e '{"error":"Not Found"}' -e '<!DOCTYPE html>')" != '' ]
 then
    echo "Warning!!! Feature description file '$1.desc' from '$GROOVIES_BASE_URL$1' was not found. File ignored." >&2
    rm -f "$DEST_PATH/$1.desc"
    return
 fi

 cat "$DEST_PATH/${1}.desc" | while read desc
 do
   feature=(${desc//:/ });
   [[ ${feature[0]} =~ ^# ]] && continue
   [[ ${feature[0]} =~ ^\s*$ ]] && continue
   [[ ${feature[0]} =~ ^(plugin)$ ]] && do_install_plugin ${feature[1]} ${feature[2]} && continue
   [[ ${feature[0]} =~ ^(groovy)$ ]] && do_install_groovy $1 ${feature[1]}  && continue
   [[ ${feature[0]} =~ ^(shell)$ ]] && do_install_and_run_shell $1 ${feature[1]}  && continue
 done
}

if [ "$1" = "" ]
then
   echo "Usage is $0 <feature list file> [pathDest]
where :
- <feature list file>: Required. This is the feature text file to use, which describe the list of feature to install and configure.
- pathDest: Optional - Default to JENKINS_HOME/ref. Path where to put groovy files in Jenkins.

The script refer to https://github.com/forj-oss/jenkins-groovy-inits repository.
You can contribute to any groovy code which can helps other jenkins users.

For any help on Groovy scripts code, check https://github.com/forj-oss/jenkins-groovy-inits."
  exit
fi

if [ "$2" = "" ] && [ "$JENKINS_HOME" = "" ]
then
   echo "At least, JENKINS_HOME or pathDest must be set to download file to the right place."
   exit
fi

if [ -z $2 ]
then
   DEST_PATH="/usr/share/jenkins/ref"
else
   DEST_PATH="$2"
fi

set -e

[[ ! -d "$DEST_PATH" ]] && mkdir -p "$DEST_PATH"


# To get the latest script version, do:
# wget -o groovy_init_download.sh https://github.com/forj-oss/jenkins-groovy-inits/raw/master/groovy_init_download.sh
# or
# curl -sSL -o groovy_init_download.sh https://github.com/forj-oss/jenkins-groovy-inits/raw/master/groovy_init_download.sh

# TODO: Be able to download from a tag or another branch.
GROOVIES_BASE_URL=${JENKINS_INSTALL_INITS_URL:=https://github.com/forj-oss/jenkins-install-inits/raw/master/}

LIST_SCRIPTS="$1"

if [ ! -r "$LIST_SCRIPTS" ]
then
   echo "Unable to find '$LIST_SCRIPTS' from $(pwd)"
   exit 1
fi

cat $LIST_SCRIPTS | while read spec
do
  feature=(${spec//:/ });
  [[ ${feature[0]} =~ ^# ]] && continue
  [[ ${feature[0]} =~ ^\s*$ ]] && continue
  [[ ${feature[0]} =~ ^(plugin|feature)$ ]] && do_install ${feature[0]} ${feature[1]} ${feature[2]}
done
