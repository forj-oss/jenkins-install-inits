// Yet-Another-Plugin-initialization script
//

import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.domains.Domain
import com.github.kostyasha.yad.DockerCloud
import com.github.kostyasha.yad.DockerConnector
import com.github.kostyasha.yad.DockerSlaveTemplate
import com.github.kostyasha.yad.credentials.DockerRegistryAuthCredentials
import com.github.kostyasha.yad.launcher.DockerComputerJNLPLauncher
import jenkins.model.Jenkins
import org.jenkinsci.plugins.docker.commons.credentials.DockerServerCredentials

void log(String text) {
    println('== yadp.groovy - ' + text)
}

String getDockerRegistry() {
    def env = System.getenv()

    SECRETS_PATH = env['SECRETS_PATH']
    if (SECRETS_PATH) {
        def json = VaultUtils.getVaultSecrets("${SECRETS_PATH}/yad")
        return json.data.docker_registry
    } else {
        return env["YADP_IMAGE_REGISTRY_CREDENTIALS"]
    }
}

String getClientKeyCertificate() {
    def env = System.getenv()
    // Check if env is properly set
    SECRETS_PATH = env['SECRETS_PATH']
    if (SECRETS_PATH) {
        def json = VaultUtils.getVaultSecrets("${SECRETS_PATH}/yad")

        return json.data.client_key
    } else {
        return env["YADP_CLIENT_KEY"]
    }

}

String getClientCertificate() {
    def env = System.getenv()
    // Check if env is properly set
    SECRETS_PATH = env['SECRETS_PATH']
    if (SECRETS_PATH) {
        def json = VaultUtils.getVaultSecrets("${SECRETS_PATH}/yad")

        return json.data.client_certificate
    } else {
        return env["YADP_CLIENT_CERTIFICATE"]
    }

}

String getServerCACertificate() {
    def env = System.getenv()
    // Check if env is properly set
    SECRETS_PATH = env['SECRETS_PATH']
    if (SECRETS_PATH) {
        def json = VaultUtils.getVaultSecrets("${SECRETS_PATH}/yad")

        retrun json.data.server_ca_certificate
    } else {
        return env["YADP_SERVER_CA_CERTIFICATE"]
    }

}

String ensure_registry_auth(String cloud_name) {
    registry_auth_data = getDockerRegistry()
    if (!registry_auth_data) {
        log("No registry credential to manage. YADP_IMAGE_REGISTRY_CREDENTIALS is missing.")
        return (null)
    }

    registry_auth = registry_auth_data.split(":")
    if (registry_auth.size() != 5) {
        log("Unable to create registry credential. YADP_IMAGE_REGISTRY_CREDENTIALS must be formated as <credID>:<RegistryServer>:<User>:<Password>:<Email>")
        return (null)
    }

    cred_id = cloud_name + "-" + registry_auth[0]

    cred_provider = Jenkins.getInstance().getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0]
    creds = cred_provider.getCredentials()

    def auth = new DockerRegistryAuthCredentials(
            CredentialsScope.GLOBAL,
            cred_id,
            "Registry credentials for server '" + registry_auth[1] + "'",
            registry_auth[2],
            registry_auth[3],
            registry_auth[4]
    )

    def foundCred

    // Search for the credential
    List<Credentials> list = cred_provider.getDomainCredentialsMap().get(Domain.global());
    if (list.contains(auth)) {
        foundCred = list.get(list.indexOf(auth))
    }

    def store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
    // compare
    if (foundCred) {
        if (foundCred.getUsername() != auth.getUsername() ||
                foundCred.getPassword() != auth.getPassword() ||
                foundCred.getEmail() != auth.getEmail()) {
            store.updateCredentials(Domain.global(), foundCred, auth)
            log("Credential '" + cred_id + "' updated.")
        } else {
            log("No change on credential '" + cred_id + "'. Nothing done.")
        }
    } else {
        store.addCredentials(Domain.global(), auth)
        log("Credential '" + cred_id + "' added.")
    }

    return (cred_id)
}

String ensure_docker_client_certificate(String cloud_name) {
    clientKey = getClientKeyCertificate()
    clientCertificate = getClientCertificate()
    serverCaCertificate = getServerCACertificate()
    if (!clientKey || !clientCertificate || !serverCaCertificate) {
        log("VAULT: client_key, client_certificate and server_ca_certificate must be set for YADP_CREDENTIAL_TYPE = 'certificate'")
        return (null)
    }

    cred_id = cloud_name + "-yadp-docker-daemon-cred"
    cred_provider = Jenkins.getInstance().getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0]
    def foundCred
    creds = cred_provider.getCredentials()

    def auth = new DockerServerCredentials(
            CredentialsScope.GLOBAL,
            cred_id,
            "Docker daemon host certificate for " + cloud_name,
            clientKey,
            clientCertificate,
            serverCaCertificate
    )

    // Search for the credential
    List<Credentials> list = cred_provider.getDomainCredentialsMap().get(Domain.global());
    if (list.contains(auth)) {
        foundCred = list.get(list.indexOf(auth))
    }

    def store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
    // compare
    if (foundCred) {
        if (foundCred.getClientKey() != auth.getClientKey() ||
                foundCred.getClientCertificate() != auth.getClientCertificate() ||
                foundCred.getServerCaCertificate() != auth.getServerCaCertificate()) {
            store.updateCredentials(Domain.global(), foundCred, auth)
            log("Credential '" + cred_id + "' updated.")
        } else {
            log("No change on credential '" + cred_id + "'. Nothing done.")
        }
    } else {
        store.addCredentials(Domain.global(), auth)
        log("Credential '" + cred_id + "' added.")
    }
    return (cred_id)
}

def env = System.getenv()

// Getting vault json for hpe4it_jenkins path

def cloud_name = env["YADP_CLOUD_NAME"]
def containerCap = env["YADP_CONTAINER_CAP"]
def serverUrl = env["YADP_SERVER_URL"]
def dockerAuthType = env["YADP_CREDENTIAL_TYPE"]
def image = env["YADP_TEMPLATE_DOCKER_IMAGE"]
def other_jenkins_url = env["YADP_LAUNCH_JNLP_JENKINS_URL"]
def docker_network_mode = env["YADP_CREATE_NETWORK_MODE"]
def docker_tmpl_label = env["YADP_TMPL_LABEL"]
if (!docker_tmpl_label) {
    docker_tmpl_label = "docker"
}
def ccap = 50
if (containerCap) {
    ccap = Integer.parseInt(containerCap)
}

if (ccap <= 0) {
    ccap = 50
    log('YADP_CONTAINER_CAP is invalid. Must be an integer > 0. Use 50 as default.')
}

if (!cloud_name) {
    cloud_name = "docker"
    log('YADP_CLOUD_NAME missing. Use "docker" as default.')
}

if (serverUrl && image) {
    // Defining credential if needed.

    def creds_id

    if (dockerAuthType) {
        // Currently supports only Docker Host Certificate Authentication


        if (dockerAuthType == "certificate") {
            creds_id = ensure_docker_client_certificate(cloud_name)
        }
    } else {
        log("No credentials managed. Missing YADP_CREDENTIAL_TYPE")
    }

    def FoundDockerCloud = Jenkins.getInstance().clouds.getByName(cloud_name)

    DockerConnector connector = new DockerConnector(serverUrl)
    if (creds_id)
        connector.setCredentialsId(creds_id)
    List<DockerSlaveTemplate> templates = new ArrayList<DockerSlaveTemplate>()

    RemoteFS = env["YADP_TEMPLATE_REMOTE_FS"]
    DockerSlaveTemplate tmpl = new DockerSlaveTemplate()

    tmpl.setLabelString(docker_tmpl_label);

    if (RemoteFS) {
        tmpl.setRemoteFs(RemoteFS)
    }

    if (other_jenkins_url) {
        launcher = tmpl.getLauncher()
        if (launcher instanceof DockerComputerJNLPLauncher) {
            launcher = (DockerComputerJNLPLauncher) launcher
        } else {
            launcher = new DockerComputerJNLPLauncher();
        }

        launcher.setJenkinsUrl(other_jenkins_url)
        tmpl.setLauncher(launcher)
    }

    dockerContainer = tmpl.getDockerContainerLifecycle()
    dockerContainer.setImage(image)

    dockerCreateContainer = dockerContainer.getCreateContainer()
    dockerCreateContainer.setNetworkMode(docker_network_mode)

    creds_id = ensure_registry_auth(cloud_name)
    if (creds_id)
        dockerContainer.getPullImage().setCredentialsId(creds_id)

    templates.add(tmpl)

    dockerCloud = new DockerCloud(cloud_name, templates, ccap, connector)

    if (!FoundDockerCloud) {
        Jenkins.getInstance().clouds.add(dockerCloud)
        log('Cloud configured.')
    } else {
        if (!Jenkins.getInstance().clouds.contains(dockerCloud)) {
            // Jenkins.getInstance().clouds.set(dockerCloudIndex, dockerCloud)
            Jenkins.getInstance().clouds.remove(FoundDockerCloud)
            Jenkins.getInstance().clouds.add(dockerCloud)
            log('Cloud updated.')
        } else {
            log("No change to provide.")
        }
    }

} else {
    log('Yet-another-docker-plugin NOT configured. Missing YADP_SERVER_URL and YADP_TEMPLATE_DOCKER_IMAGE.')
}
