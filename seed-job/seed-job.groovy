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

import net.sf.json.JSONObject

def env = System.getenv()
seed_jobs_repo = env['SEED_JOBS_REPO']
git_password = env['GIT_PASSWORD']
git_username = env['GIT_USERNAME']
seedJobs_password = env['SEED_JOBS_PASSWORD']
seedJobs_username = env['SEED_JOBS_USERNAME']
seedJobsID = env['SEED_JOBS_CRED_ID']

// Compatibility
if ((git_password || git_username) && (!seedJobs_password && !seedJobs_username)) {
  println("== seed-job.groovy --> GIT_USERNAME or GIT_PASSWORD is obsolete. Use SEED_JOBS_USERNAME and SEED_JOBS_PASSWORD instead.")
} else {
  git_password = seedJobs_password
  git_username = seedJobs_username
}

// Define Credential ID
def seedJobId = ''

if (seedJobsID) {
  if (seedJobsID == "none") {
    seedJobId = ""
  } else {
    seedJobId = seedJobsID 
  }
} 

if (git_username && git_password && !seedJobsID) {
  seedJobId = 'seedjob-github'
  println("== seed-job.groovy --> Using default seed job credential ID '" + seedJobId + "'")
}

jobdsl_security = env['JOB_DSL_SCRIPT_SECURITY']

// A new line separated list of dsl scripts, located in the workspace.
// Can use wildcards...and will be defaulted to jobs/**/*.groovy.
build_dsl_scripts = env['BUILD_DSL_SCRIPTS']

println("== seed-job.groovy --> JOB_DSL_SCRIPT_SECURITY is set to '" + jobdsl_security + "'")
config = Jenkins.instance.getDescriptorByType(GlobalJobDslSecurityConfiguration)

JSONObject json = new JSONObject()
if (jobdsl_security && jobdsl_security == "true") {
  json.put('useScriptSecurity', '')
  config.configure(null, json)
  println("== seed-job.groovy --> Global JobDsl Security is on. Jenkins Administrators are required to approve/deny.")
} else {
  config.configure(null, json)
  println("== seed-job.groovy --> Global JobDsl Security is off. All jobs dsl are by default approved.")
}

def credential_id

if (seedJobId != "" ) {
  println("== seed-job.groovy --> Seed job credential ID is set to '" + seedJobId + "'")
  available_credentials = 
    CredentialsProvider.lookupCredentials(
    StandardUsernameCredentials.class,
    Jenkins.getInstance(),
    hudson.security.ACL.SYSTEM,
    new SchemeRequirement("ssh")
  )
  if (git_username && git_password) {
    println("== seed-job.groovy --> SEED_JOBS_USERNAME is set to '" + git_username + "'")
    println("== seed-job.groovy --> SEED_JOBS_PASSWORD is set to '***'")

    newCredential = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, seedJobId, 'seedJob user/token (github)', git_username, git_password)

    username_matcher = CredentialsMatchers.withUsername(git_username)

    existing_credentials =
      CredentialsMatchers.firstOrNull(
        available_credentials,
        username_matcher
      )

      global_domain = Domain.global()
      credentials_store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

    if (existing_credentials == null) {
      credentials_store.addCredentials(global_domain, newCredential)
      credential_id = newCredential.id

      println("== seed-job.groovy --> '" + seedJobId + "' credential added.")
    } else {
      credentials_store.updateCredentials(global_domain, existing_credentials, newCredential)
      credential_id = existing_credentials.id

      println("== seed-job.groovy --> '" + seedJobId + "' credential updated.")
    }
  }
  else {
    // Search for SEED_JOBS_CRED_ID
    id_matcher = CredentialsMatchers.withId(seedJobId)

    existing_credentials =
      CredentialsMatchers.firstOrNull(
        available_credentials,
        id_matcher
      )
    if (existing_credentials == null) {
      println("== seed-job.groovy --> Warning !!! Credential " + seedJobId + " has not be defined. 'seed-job' won't use it. It has not been added because SEED_JOBS_USERNAME/SEED_JOBS_PASSWORD are not set. You can use credentials feature to set it.")
    } else {
      println("== seed-job.groovy --> 'seed-job' uses the already existing '" + seedJobId + "' credential ID.")
      credential_id = existing_credentials.id
    }
  }
} else {
  println("== seed-job.groovy --> No credential to attach to the seed-job. SEED_JOBS_CRED_ID is set empty.")
}
def seedJobName = "seed-job"

if(seed_jobs_repo) {
  println("== seed-job.groovy --> SEED_JOBS_REPO is set to '" + seed_jobs_repo + "'")

  // Check or create project
  def seedJob = Jenkins.instance.getItem(seedJobName)
  if(!Jenkins.instance.getItemMap().containsKey(seedJobName) || !seedJob instanceof FreeStyleProject ) {
    seedJob = Jenkins.instance.createProject(FreeStyleProject.class, seedJobName)
    println("== seed-job.groovy --> FreestyleProject '"+ seedJobName +"' created.")
  } else {
    println("== seed-job.groovy --> FreestyleProject '"+ seedJobName +"' already exist. Checking...")
  }

  // Check or create SCM config
  def scm 

  if (seedJob.scm instanceof GitSCM) {
    scm = seedJob.scm
    def scmConfigs = scm.getUserRemoteConfigs()
    if (scmConfigs.size() != 1) {
      scm = null
      println("== seed-job.groovy --> GitSCM.UserRemoteConfig has changed. Too many repositories.")
    } else {
      def userRemoteConfig = scmConfigs[0]
      if (userRemoteConfig.getUrl() != seed_jobs_repo || userRemoteConfig.getCredentialsId() != credential_id ) {
        scm = null
        println("== seed-job.groovy --> GitSCM.UserRemoteConfig has changed. repo url and/or credential-id updated.")
      } else {
        println("== seed-job.groovy --> No update on GitSCM.UserRemoteConfig.")
      }
    }
  } 
  
  if ( !scm ) {
    def userRemoteConfig = new UserRemoteConfig(seed_jobs_repo, null, null, credential_id)

    scm = new GitSCM(
      Collections.singletonList(userRemoteConfig),
      Collections.singletonList(new BranchSpec("master")),
      false,
      Collections.<SubmoduleConfig>emptyList(),
      null,
      null,
      null)

    seedJob.scm = scm
    println("== seed-job.groovy --> Set GitSCM to '" + seedJobName + "'.")
  } else {
    println("== seed-job.groovy --> No update on GitSCM.")
  }
      
  if (!build_dsl_scripts) {
      build_dsl_scripts = "jobs_dsl/**/*.groovy"
      println("== seed-job.groovy --> BUILD_DSL_SCRIPTS not set. Using '" + build_dsl_scripts + "' as default")
  }
  else 
      println("== seed-job.groovy --> BUILD_DSL_SCRIPTS is set to '" + build_dsl_scripts + "'. Use it as build dsl scripts.")

  if (seedJob.buildersList.size() > 1) {
    seedJob.buildersList.clear()
    println("== seed-job.groovy --> Too many builders. Recreate it.")
  }

  def scriptLocation
  if (seedJob.buildersList.size() == 1 && seedJob.buildersList[0] instanceof ExecuteDslScripts) {
    scriptLocation = seedJob.buildersList[0]
    def updated = false
    if (scriptLocation.getTargets() != build_dsl_scripts) {
      scriptLocation.setTargets(build_dsl_scripts)
      updated = true
    }
    if (scriptLocation.getRemovedJobAction() != RemovedJobAction.DISABLE) {
      scriptLocation.setRemovedJobAction(RemovedJobAction.DISABLE)
      updated = true
    }

    if (scriptLocation.getRemovedViewAction() != RemovedViewAction.DELETE) {
      scriptLocation.setRemovedViewAction(RemovedViewAction.DELETE)
      updated = true
    }

    if (scriptLocation.getLookupStrategy() != LookupStrategy.JENKINS_ROOT) {
      scriptLocation.setLookupStrategy(LookupStrategy.JENKINS_ROOT)
    }
    if (updated)
      println("== seed-job.groovy --> builder JobDSL updated to '" + seedJobName + "'.")
  } else {
    scriptLocation = new ExecuteDslScripts()
    scriptLocation.setTargets(build_dsl_scripts)
    scriptLocation.setRemovedJobAction(RemovedJobAction.DISABLE)
    scriptLocation.setRemovedViewAction(RemovedViewAction.DELETE)
    scriptLocation.setLookupStrategy(LookupStrategy.JENKINS_ROOT)
    seedJob.buildersList.add(scriptLocation)
    println("== seed-job.groovy --> builder JobDSL added to '" + seedJobName + "'. ")
  }

  seedJob.save()
  seedJob.scheduleBuild(new Cause.UserIdCause())
  println("== seed-job.groovy --> '" + seedJobName + "' scheduled. ")
}
else
  println("== seed-job.groovy --> Missing SEED_JOBS_REPO, SEED_JOBS_USERNAME, SEED_JOBS_PASSWORD. 'seed-job' initial project NOT verified.")

