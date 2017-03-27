# Introduction

This feature implements the 'yet-another-docker-plugin' used for docker/swarm/UCP (docker entreprise clustering).

# Limitation

Currently, this feature configure only:
- 1 cloud, with
  - cloud name
  - container capabilities.
  - container network mode
- 1 client certificate credential for docker servers (optional)
- 1 registry credential (optional)
- 1 docker template, with
  - image name
  - remote FS
- 1 docker JNLP launcher, with
  - different jenkins url to connect to.

It installs yet-another-docker-plugin in latest version. (>= 1.0-rc28)

# To use it

Add in your features.lst

```text
feature:yet-another-docker-plugin
```

## jenkins credentials managed by vault.  
If you want you use vault, you need at runtime to :

- Set `SECRET_PATH`. Ex: secret/hpe4it-jenkins-ci-dev
- Connect to the Vault service properly.

Data will be retrieved from `$SECRET_PATH/yad/`

## Parameters

- `YADP_CLOUD_NAME`
  Optional. Define the cloud name in jenkins. `docker` is set by default.
- `YADP_SERVER_URL`
  **Required**. Define the docker server url (tcp:// or socket)
- `YADP_IMAGE_REGISTRY_CREDENTIALS` or `$SECRET_PATH/yad/docker_registry` (vault)
  Optional. Define the registry credential for images to pull. It must be formatted as `<credID>:<RegistryServer>:<User>:<Password>:<Email>`.
- `YADP_TEMPLATE_DOCKER_IMAGE`
  **Required**
- `YADP_TEMPLATE_REMOTE_FS`
  Optional. Default path when jenkins slaves are started. '/home/jenkins' is set by default.
- `YADP_CREDENTIAL_TYPE`
  Optional. Supports only 'certificate' and will require `YADP_CLIENT_KEY`, `YADP_CLIENT_CERTIFICATE` and `YADP_SERVER_CA_CERTIFICATE` or vault to be set.
- `YADP_CLIENT_KEY` or `$SECRET_PATH/yad/client_key` (vault)
  **Required if `YADP_CREDENTIAL_TYPE == 'certificate'`**. Client key data.
- `YADP_CLIENT_CERTIFICATE` or `$SECRET_PATH/yad/client_certificate` (vault)
  **Required if `YADP_CREDENTIAL_TYPE == 'certificate'`**. Client certificate data.
- `YADP_SERVER_CA_CERTIFICATE` or `$SECRET_PATH/yad/server_ca_certificate` (vault)
  **Required if `YADP_CREDENTIAL_TYPE == 'certificate'`**. Server CA certificate data.
- `YADP_CONTAINER_CAP`
  Optional. The maximum number of containers that this provider is allowed to run. `50` is set by default.
- `YADP_LAUNCH_JNLP_JENKINS_URL`
  Optional. Define any other jenkins url instead of default public one. Useful if public url is hiding jenkins slave ports. If you set this, you force JNLP Launch mode.
- `YADP_TMPL_LABEL` 
  Optional. By default use `docker` as label.
- `YADP_CREATE_NETWORK_MODE`
  Optional. [Set the Network mode for the container](https://docs.docker.com/engine/reference/run/#network-settings):
  - Empty: undefined, docker daemon defaults
  - `bridge`: creates a new network stack for the container on the docker bridge
  - `none`: no networking for this container
  - `container:<name|id>`: reuses another container network stack
  - `host`: use the host network stack inside the container. Note: the host mode gives the container full access to local system services such as D-bus and is therefore considered insecure.

> When scaling in swarm, with overlay network, you should provide `YADP_CREATE_NETWORK_MODE=container:<id>` and `YADP_LAUNCH_JNLP_JENKINS_URL=http://<id>:8080` (assuming your jenkins master web port is 8080)

YADP_TMPL_LABEL
