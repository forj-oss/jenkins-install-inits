# Shared libraries

This feature is used to create shared libraries at the Jenkins root level.

## Environment variables

To configure the feature you will need to create a configuration file. The path to that file will be stored in an environment variable named **SHARED_LIB_PATH**.

## Configuration file

```json
[
    {
        "name":"my funny library",
        "default_version": "master",
        "implicitly_load" : true,
        "allow_override_version" : true,
        "inc_lib_changes" : false,
        "retrieval_method" : "modern",
        "scm": {
            "endpoint":{
                "name":"my endpoint",
                "url" : "https://github.company.com/api/v3/"
            },
            "credentials":{
                "id": "id",
                "login": "login",
                "password": "github token"
            },
            "owner" : "Organization or User",
            "repository" : "jenkins-shared-lib",
             "behaviours" :{
                "branchDiscovery" : 1,
                "originPRDiscovery" : 1,
                "forkPRDiscovery" :{
                    "strategyId" : 1,
                    "trust" : "TrustPermission"
                }
            }
        }
    }
]
```

### branchDiscovery

Authorized values are :

- 1 : Exclude branches that are also field as PR
- 2 : Only branches htat are also field as PR
- 3 : All branches

### originPRDiscovery

Authorized values are :

- 1 : Merging the pull request with the current target branch revision
- 2 : The current pull request revision
- 3 : Both the current pull request revision and the pull resquest with the current target revision

### forkPRDiscovery

#### forkPRDiscovery.strategyId 

Authorized values are :

- 1 : Merging the pull request with the current target branch revision
- 2 : The current pull request revision
- 3 : Both the current pull request revision and the pull resquest with the current target revision

##### forkPRDiscovery.trust 
Authorized values are :

- TrustNobody : Nobody
- TrustContributors : Contributors only
- TrustPermission : For users with Admin or write permission
- TrustEveryone : Everyone

## TODO

Currently this plugin only support modern SCM.