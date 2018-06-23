import jenkins.model.*;
import org.apache.log4j.*

Logger log  =Logger.getInstance('nb-executor.groovy');
log.info('== nb-executor.groovy - Start nb executors configuration');

try {
    String tmp = System.getenv("JENKINS_NB_EXECUTORS") ? System.getenv("JENKINS_NB_EXECUTORS") : "0";
    int NB_EXEC = Integer.parseInt(tmp);
    Jenkins jenkins =  Jenkins.getInstance();
    jenkins.setNumExecutors(NB_EXEC);
    jenkins.save();
    log.info('== nb-executor.groovy - End nb executors configuration');
}
catch (Exception ex){
    log.error('== nb-executor.groovy :' + ex.message + '\n' + ex.getStackTrace());
}