# proxy.groovy

This script configure your jenkins with the your http\_proxy environment variable.

## Jenkins native

If you start jenkins on your box, already configured with http\_proxy, then jenkins will be autoconfigured with this data.

## Jenkins docker

If you start jenkins from docker, you could simply use -e http\_proxy=... at runtime to properly set the proxy in jenkins at startup.

# Using groovy plugin

If you have started your jenkins with http\_proxy in the environment, you could create a job running this proxy.groovy script to 
ensure proxy is already set as defined by the environment.

# TODO

- Integrate better https, and no\_proxy
- Propose a way to read this from a file instead of ENV
