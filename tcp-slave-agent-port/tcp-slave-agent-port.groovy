import hudson.model.*;
import jenkins.model.*;


Thread.start {
      sleep 10000
      println "== tcp-slave-agent-port -> setting agent port for jnlp"
      def env = System.getenv()
      if ( env['JENKINS_SLAVE_AGENT_PORT'] ) {
         int port = env['JENKINS_SLAVE_AGENT_PORT'].toInteger()
         Jenkins.instance.setSlaveAgentPort(port)
         println "== tcp-slave-agent-port -> setting agent port for jnlp... done"
      }
     else
        println "== tcp-slave-agent-port -> setting agent port for jnlp... aborted. JENKINS_SLAVE_AGENT_PORT not set."
}
