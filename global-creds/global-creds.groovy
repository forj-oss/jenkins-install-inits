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
STACKATO_USER  = env['STACKATO_USER']
STACKATO_PASS  = env['STACKATO_PASS']

domain = Domain.global()
store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

secretText = new StringCredentialsImpl(
  CredentialsScope.GLOBAL,
  "github",
  "This is the token used for Github Pull Request Builder plugin",
  Secret.fromString(GITHUB_TOKEN)
)

usernameAndPassword = new UsernamePasswordCredentialsImpl(
  CredentialsScope.GLOBAL,
  "stackato-creds",
  "Stackato Credentials",
  STACKATO_USER,
  STACKATO_PASS
)

store.addCredentials(domain, secretText)
store.addCredentials(domain, usernameAndPassword)
