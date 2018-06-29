import jenkins.model.*;
import org.apache.log4j.*
import hudson.model.Node.Mode

Logger log = Logger.getInstance('nb-executor.groovy');
log.info('== nb-executor.groovy - Start nb executors configuration');

try {
    String tmp = System.getenv("JENKINS_NB_EXECUTORS") ? System.getenv("JENKINS_NB_EXECUTORS") : "2";
    int NB_EXEC = Integer.parseInt(tmp);
    Jenkins jenkins =  Jenkins.getInstance();
    jenkins.setNumExecutors(NB_EXEC);
    jenkins.save();
}
catch (Exception ex){
    log.error('== nb-executor.groovy :' + ex.message + '\n' + ex.getStackTrace());
}

log.info('== nb-executor.groovy - Start master mode configuration');

try {
    String masterMode = System.getenv("JENKINS_MASTER_MODE") ? System.getenv("JENKINS_MASTER_MODE") : "EXCLUSIVE";
    if (masterMode != "EXCLUSIVE" && masterMode != "NORMAL") {
        masterMode="EXCLUSIVE"
        log.info("== nb-executor.groovy - JENKINS_MASTER_MODE = '" + masterMode + "' is invalid. Using default 'EXCLUSIVE'")
    }
    Jenkins jenkins =  Jenkins.getInstance();

    Mode mode = Mode.EXCLUSIVE
    if (masterMode == "NORMAL") {
        mode = Mode.NORMAL
    }
    if (jenkins.getMode() != mode) {
        log.info("== nb-executor.groovy - Updating Master mode to '" + masterMode + "'")
        if (masterMode == "EXCLUSIVE") {
            jenkins.setMode(Mode.EXCLUSIVE)
        } else {
            jenkins.setMode(Mode.NORMAL)
        }
        jenkins.save();
    } else {
        log.info("== nb-executor.groovy - Master mode: Nothing to update.")
    }
}
catch (Exception ex){
    log.error('== nb-executor.groovy :' + ex.message + '\n' + ex.getStackTrace());
}

log.info('== nb-executor.groovy - End nb executors configuration');
