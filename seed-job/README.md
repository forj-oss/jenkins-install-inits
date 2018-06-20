# Job DSL seed job

## How to define it

This script will initialize your first seed job and execute it.
It currently requires following parameters (environment variables)

- `SEED_JOBS_REPO`          : Required. GIT url.
- `SEED_JOBS_CRED_ID`       : Optional. Jenkins credential ID.
- `SEED_JOBS_PASSWORD`      : Optional. Password required to access the GIT repo.
- `SEED_JOBS_USERNAME`      : Optional. Username required to acces the GIT repo.
- `BUILD_DSL_SCRIPTS`       : Optional. Job DSL collection of groovy scripts to generate/maintain your jobs. By default, it is set to `jobs_dsl/**/*.groovy`
- `JOB_DSL_SCRIPT_SECURITY` : Optional. By default, script security is off. set 'true' to enable it.

It has been designed to do the following:

1. create Freestyle project, called 'seed-job'.
2. if `SEED_JOBS_USERNAME`, `SEED_JOBS_PASSWORD` are set, the credential ID will be created or updated.
3. if `SEED_JOBS_CRED_ID` defined (not empty or set to `none`), The credential existence is tested and created or updated if username/password are set.
4. Attach the SCM to the seed-job with the `SEED_JOBS_REPO` and eventually attach the credential created/found.
5. Configure job-dsl to read all DSL from  `jobs-dsl/**/*.groovy`, or from `BUILD_DSL_SCRIPTS`
6. Run the seed-job

## Security concern

`SEED_JOBS_PASSWORD` is passed in clear text and accessible to any master jobs. To increase security, use the `credentials` feature.

**NOTE**:

If you need to re-use the created/updated credential used by the seedjob in your DSL scripts, the credential ID to use is `seedjob-github`

Ex:

    Job('project_pull_request') {
       scm {
          git {
             remote {
                url('https://github.com/owner/project')
                credentials('seedjob-github') # Use the credential ID 'seedjob-github' created/maintained by seed-job or credentials features
             }
          }
       }
    }

## TODO

- Add support for more credentials (SSH like)
