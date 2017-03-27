import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.GithubAuthorizationStrategy

def instance = Jenkins.getInstance()
def env = System.getenv()

GITHUB_ADMIN_USERS  = env['GITHUB_ADMIN_USERS']
GITHUB_ADMIN_ORG = env['GITHUB_ADMIN_ORG']
GITHUB_ANONYMOUS_READ_PERM = env['GITHUB_ANONYMOUS_READ_PERM']

//permissions are ordered similar to web UI
//Admin User Names
String adminUserNames = ""
if (GITHUB_ADMIN_USERS)
   adminUserNames = GITHUB_ADMIN_USERS
//Participant in Organization
String organizationNames = ""
if (GITHUB_ADMIN_ORG)
   organizationNames = GITHUB_ADMIN_ORG
//Use Github repository permissions
boolean useRepositoryPermissions = true
//Grant READ permissions to all Authenticated Users
boolean authenticatedUserReadPermission = true
//Grant CREATE Job permissions to all Authenticated Users
boolean authenticatedUserCreateJobPermission = false
//Grant READ permissions for /github-webhook
boolean allowGithubWebHookPermission = false
//Grant READ permissions for /cc.xml
boolean allowCcTrayPermission = false

//Grant READ permissions for Anonymous Users
boolean allowAnonymousReadPermission = false
if (GITHUB_ANONYMOUS_READ_PERM && GITHUB_ANONYMOUS_READ_PERM == "true") {
  allowAnonymousReadPermission = true
}
//Grant ViewStatus permissions for Anonymous Users
boolean allowAnonymousJobStatusPermission = false


println("== github-authorization.groovy - GITHUB_ADMIN_USERS = " + GITHUB_ADMIN_USERS)
println("== github-authorization.groovy - GITHUB_ADMIN_ORG = " + GITHUB_ADMIN_ORG)
println("== github-authorization.groovy - GITHUB_ANONYMOUS_READ_PERM = " + GITHUB_ANONYMOUS_READ_PERM)
if (GITHUB_ADMIN_USERS || GITHUB_ADMIN_ORG) {
  AuthorizationStrategy github_authorization = new GithubAuthorizationStrategy(adminUserNames,
    authenticatedUserReadPermission,
    useRepositoryPermissions,
    authenticatedUserCreateJobPermission,
    organizationNames,
    allowGithubWebHookPermission,
    allowCcTrayPermission,
    allowAnonymousReadPermission,
    allowAnonymousJobStatusPermission)

  //check for equality, no need to modify the runtime if no settings changed
  if(!github_authorization.equals(instance.getAuthorizationStrategy())) {
    instance.setAuthorizationStrategy(github_authorization)
    instance.save()
    println("== github-authorization.groovy - github authorization saved.")
  } else {
    println("== github-authorization.groovy - github authorization not updated.")
  }
} else {
  println("== github-authorization.groovy - github authorization not configured. Missing at leat one of GITHUB_ADMIN_USERS or GITHUB_ADMIN_ORG setting.")
}
