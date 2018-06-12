# CSFR - Crosse Site Request Forgery
	
A cross site request forgery (or CSRF/XSRF) is an exploit that enables an unauthorized third party to take actions on a web site as you. In Jenkins, this could allow someone to delete jobs, builds or change Jenkins' configuration.
When this option is enabled, Jenkins will check for a generated nonce value, or "crumb", on any request that may cause a change on the Jenkins server. This includes any form submission and calls to the remote API.

Enabling this option can result in some problems, like the following:

Some Jenkins features (like the remote API) are more difficult to use when this option is enabled.
Some features, especially in plugins not tested with this option enabled, may not work at all.
If you are accessing Jenkins through a reverse proxy, it may strip the CSRF HTTP header, resulting in some protected actions failing.

## Envionment variables

To configure the plugin, you will have to set the following environment variables :

- JENKINS_CSRF : Boolean defining if we should enable or not CSRF. Possible values are (true | false), default is true.