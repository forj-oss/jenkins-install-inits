# Global credentials module

This feature is used to store global jenkins credentials for plugins or jobs use.

# How to use it?

- Add `feature:global-creds` in your features.lst in your project (Dockerfile or mount)
- Add `GITHUB_TOKEN` in the jenkins environment startup.
- Add `STACKATO_USER` in the jenkins environment startup.
- Add `STACKATO_PASS` in the jenkins environment startup.

Example:
```
GITHUB_TOKEN=<GITHUB_USER_TOKEN>
STACKATO_USER="my.name@myorg.com"
STACKATO_PASS=<STACKATO_PASSWORD>
```
