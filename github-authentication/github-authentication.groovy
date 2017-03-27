import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.GithubSecurityRealm

def instance = Jenkins.getInstance()
def env = System.getenv()

GITHUB_URL = env['GITHUB_URL']
GITHUB_API_URL = env['GITHUB_API_URL']
GITHUB_CLIENT_ID = env['GITHUB_CLIENT_ID']
GITHUB_CLIENT_SECRET = env['GITHUB_CLIENT_SECRET']

println('== github-authentication.groovy - GITHUB_API_URL = ' + GITHUB_API_URL)
println('== github-authentication.groovy - GITHUB_URL = ' + GITHUB_URL)
println('== github-authentication.groovy - GITHUB_CLIENT_ID = ' + GITHUB_CLIENT_ID)
if (GITHUB_CLIENT_SECRET) {
   println('== github-authentication.groovy - GITHUB_CLIENT_SECRET = "***"')
}

if (GITHUB_API_URL && GITHUB_URL && GITHUB_CLIENT_ID && GITHUB_CLIENT_SECRET) {
  String githubWebUri = GITHUB_URL
  String githubApiUri = GITHUB_API_URL
  String clientID = GITHUB_CLIENT_ID 
  String clientSecret = GITHUB_CLIENT_SECRET 
  String oauthScopes = 'read:org,user:email'


  SecurityRealm github_realm = new GithubSecurityRealm(githubWebUri, githubApiUri, clientID, clientSecret, oauthScopes)
//check for equality, no need to modify the runtime if no settings changed
  if(!github_realm.equals(instance.getSecurityRealm())) {
    instance.setSecurityRealm(github_realm)
    instance.save()
    println('== github-authentication.groovy - OAuth plugin configuration saved.')
  } else {
    println('== github-authentication.groovy - No update.')
  }
} else {
println('== github-authentication.groovy - OAuth plugin configuration not done due to missing values')
}
