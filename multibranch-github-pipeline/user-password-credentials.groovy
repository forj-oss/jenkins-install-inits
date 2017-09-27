import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import org.jenkinsci.plugins.plaincredentials.*
import org.jenkinsci.plugins.plaincredentials.impl.*
import hudson.util.Secret
import hudson.plugins.sshslaves.*
import org.apache.commons.fileupload.*
import org.apache.commons.fileupload.disk.*
import java.nio.file.Files

def env = System.getenv()

GITHUB_TOKEN  = env['GITHUB_TOKEN']
GITHUB_USER  = env['GITHUB_USER']
GITHUB_PASS  = env['GITHUB_PASS']

domain = Domain.global()
store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

println("== multibranch-github-pipeline.groovy => Starting.")
if ( GITHUB_TOKEN ) {
    println("== multibranch-github-pipeline.groovy => Adding token.")
    secretText = new StringCredentialsImpl(
            CredentialsScope.GLOBAL,
            "github-token",
            "This is the token used for Github source branch plugin",
            Secret.fromString(GITHUB_TOKEN)
    )

    store.addCredentials(domain, secretText)
    println("== multibranch-github-pipeline.groovy => token added.")

}

if ( GITHUB_USER && GITHUB_PASS) {
    println("== multibranch-github-pipeline.groovy => Adding user " + GITHUB_USER )

    usernameAndPassword = new UsernamePasswordCredentialsImpl(
            CredentialsScope.GLOBAL,
            "github-user",
            "Github Credentials",
            GITHUB_USER,
            GITHUB_PASS
    )

    store.addCredentials(domain, usernameAndPassword)
    println("== multibranch-github-pipeline.groovy => user " + GITHUB_USER + " added.")

}

println("== multibranch-github-pipeline.groovy => Done.")
