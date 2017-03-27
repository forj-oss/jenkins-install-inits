import jenkins.model.Jenkins
import hudson.model.FreeStyleProject
import hudson.plugins.git.GitSCM
import hudson.plugins.git.UserRemoteConfig
import hudson.plugins.git.BranchSpec
import hudson.plugins.git.SubmoduleConfig
import hudson.plugins.git.extensions.impl.PathRestriction
import hudson.model.Cause
import hudson.model.Cause.UserIdCause

import javaposse.jobdsl.plugin.*

import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*

def env = System.getenv()
seed_jobs_repo = env['SEED_JOBS_REPO']
git_password = env['GIT_PASSWORD']
git_username = env['GIT_USERNAME']
job_dsl_path = env['JOB_DSL_PATH']

if(seed_jobs_repo) {
  def credential_id

  println("== seed-job.groovy --> SEED_JOBS_REPO is set to '" + seed_jobs_repo + "'")
  if(!Jenkins.instance.getItemMap().containsKey("seed-job")) {
    def seedJob = Jenkins.instance.createProject(FreeStyleProject.class, "seed-job")
    println("== seed-job.groovy --> FreestyleProject 'seed-job' created.")

    if (git_username && git_password) {
       println("== seed-job.groovy --> GIT_USERNAME is set to '" + git_username + "'")
       println("== seed-job.groovy --> GIT_PASSWORD is set to '***'")
       username_matcher = CredentialsMatchers.withUsername(git_username)
       available_credentials =
         CredentialsProvider.lookupCredentials(
           StandardUsernameCredentials.class,
           Jenkins.getInstance(),
           hudson.security.ACL.SYSTEM,
           new SchemeRequirement("ssh")
         )

       existing_credentials =
         CredentialsMatchers.firstOrNull(
           available_credentials,
           username_matcher
         )

       if (existing_credentials == null) {
          existing_credentials = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, 'github', 'GitHub pull request integration', 
                                                                  git_username, git_password)
          global_domain = Domain.global()
          credentials_store =
          Jenkins.instance.getExtensionList(
               'com.cloudbees.plugins.credentials.SystemCredentialsProvider'
             )[0].getStore()
          credentials_store.addCredentials(global_domain, existing_credentials)
          println("== seed-job.groovy --> 'github' credential added ")
      }
      // else // TODO: Reset the password of the credential from what is passed.
      credential_id = existing_credentials.id
    }
    else {
       println("== seed-job.groovy --> No credential to create/maintain as GIT_USERNAME or GIT_PASSWORD not set.")
       //existing_credentials.getPassword()
    }

    def userRemoteConfig = new UserRemoteConfig(seed_jobs_repo, null, null, credential_id)

    def scm = new GitSCM(
      Collections.singletonList(userRemoteConfig),
      Collections.singletonList(new BranchSpec("master")),
      false,
      Collections.<SubmoduleConfig>emptyList(),
      null,
      null,
      null)

    if (!job_dsl_path) {
       job_dsl_path = "jobs_dsl/**"
       println("== seed-job.groovy --> JOB_DSL_PATH not set. Using '" + job_dsl_path + "' as default")
    }
    else 
       println("== seed-job.groovy --> Using '" + job_dsl_path + "' as Job DSL path.")

    scm.getExtensions().add(new PathRestriction(job_dsl_path + "/*.*",""))
    seedJob.scm = scm

    def scriptLocation = new ExecuteDslScripts.ScriptLocation("false", job_dsl_path + "/*.groovy", null)
    seedJob.buildersList.add(new ExecuteDslScripts(scriptLocation, false, RemovedJobAction.DELETE, RemovedViewAction.DELETE, LookupStrategy.JENKINS_ROOT))
    
    println("== seed-job.groovy --> 'seed-job' configured. ")

    seedJob.save()
    seedJob.scheduleBuild(new Cause.UserIdCause())
  }
}
else
  println("== seed-job.groovy --> Missing SEED_JOBS_REPO, GIT_USERNAME, GIT_PASSWORD. 'seed-job' initial project NOT verified.")
