import hudson.security.csrf.DefaultCrumbIssuer
import jenkins.model.Jenkins

try {
     println ("== csrf.groovy - Start configuration");
     
    // Retrieve environment variables
    Boolean JENKINS_CSRF = System.getenv("JENKINS_CSRF") != null ? System.getenv("JENKINS_CSRF").toBoolean() : true;
    def j = Jenkins.instance

    if(JENKINS_CSRF) {
        j.setCrumbIssuer(new DefaultCrumbIssuer(true))
        j.save()
        println ("== csrf.groovy - Enabled CSRF Protection with the default crumb issuer.")
    }
    else {
        j.setCrumbIssuer(null)
        j.save()
        println ("== csrf.groovy - Disable CSRF protection.")
    }
    println ("== csrf.groovy - End configuration");
}
catch(Exception ex){
    println ("== csrf.groovy - Error during CSRF configuration : " + ex.message)
}