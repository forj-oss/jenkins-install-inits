
import jenkins.model.Jenkins

def env = System.getenv()

JENKINS_URL = env['JENKINS_URL']

if (JENKINS_URL) {
  def location = Jenkins.instance.getDescriptor('jenkins.model.JenkinsLocationConfiguration')
  location.setUrl(JENKINS_URL)
  println("== 00_jenkins-init.groovy - Jenkins URL configured to " + Jenkins.instance.getRootUrl())
} else {
  println("== 00_jenkins-init.groovy - Jenkins URL not configured. " + Jenkins.instance.getRootUrl())
}
