# Github pipeline

This script will configure your jenkins with latest functional pipeline.

It will configure 2 credentials used by github-source-branch plugin:

- Token : Set `GITHUB_TOKEN` to set a secret text credential as `github-token`
- User/Password : Set `GITHUB_USER` & `GITHUB_PASS` to set a
    User/Password credential called `github-user`

# Important information

github-source-branch implements github organization folder (obsoleting
github-organization-folder - 1.6)

The `github-user` credential is used when you created a github
organization project.

The `github-token` credential is normally used by the github server
declaration in jenkins system configuration.

Note that as soon as you configured a web hook, manually on github, the
project is automatically spawned. Som the github section in jenkins system
configuration is not required (except for github enterprise - Not verified)
