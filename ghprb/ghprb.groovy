import jenkins.model.Jenkins
import org.jenkinsci.plugins.ghprb.GhprbGitHubAuth

def env = System.getenv()

serverAPIUrl = env['GITHUB_API_URL']

if (serverAPIUrl) {
   println('== ghprb.groovy - GITHUB_API_URL = ' + serverAPIUrl)
   auths = Jenkins.getInstance().getDescriptor("org.jenkinsci.plugins.ghprb.GhprbTrigger").getGithubAuth()

   if (auths.size() > 0) {
      auths.clear()
   }

   /* TODO: Replace following hard coded variables */
   jenkinsUrl = ""
   credentialsId = "github"
   description = "Anonymous connection"
   id = "github-server"
   secret = ""

   new_auth=new GhprbGitHubAuth(serverAPIUrl, jenkinsUrl, credentialsId, description, id, secret)

   auths.add(new_auth)

   Jenkins.getInstance().getDescriptor("org.jenkinsci.plugins.ghprb.GhprbTrigger").getGithubAuth()

   println('== ghprb.groovy - GitHubPullRequestBuilder plugin configured.')
}
else {
   println('== ghprb.groovy - GitHubPullRequestBuilder plugin NOT configured. Missing GITHUB_API_URL')
}
