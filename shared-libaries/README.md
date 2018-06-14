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
            "behaviours" : "Not supported for now"
        }
    }
]

```

## TODO

Currently this plugin only support modern SCM without any additional behaviours.