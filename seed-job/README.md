# Job DSL seed job

This script will initialize your first seed job and execute it.
It currently requires following parameters (environment variables)

- `SEED_JOBS_REPO` : Required. GIT url.
- `GIT_PASSWORD`   : Optional. Password required to access the GIT repo.
- `GIT_USERNAME`   : Optional. Username required to acces the GIT repo.
- `JOB_DSL_PATH`   : Optional. Job DSL path containing groovy code to generate/maintain your jobs. By default, it is set to `jobs_dsl/**`

It has been designed to do the following:

1. create Freestyle project, called 'seed-job'.
2. Create the credential if GIT\_USERNAME & GIT\_PASSWORD are set. The credential ID will be 'github'.
3. Attach the SCM to the seed-job with the SEED\_JOBS\_REPO and evantually attach the credential created.
4. Configure job-dsl to read all DSL from  `jobs-dsl/**/*.groovy`, or from `JOB_DSL_PATH`
5. Run the seed-job

**NOTE**:

If you need to re-use the created/updated credential used by the seedjob in your DSL scripts, the credential ID to use is 'github'

Ex:

    Job('project_pull_request') {
       scm {
          git {
             remote {
                url('https://github.com/project/manager')
                credentials('github') # Use the credential ID 'github' created/maintained by seed-job.groovy
             }
          }
       }
    }


# TODO
- Be able to re-update the seed-job if we re-execute it for groovy plugin jobs case.
- Add support for more credentials (SSH like)

