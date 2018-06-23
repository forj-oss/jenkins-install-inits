import jenkins.model.*;
import org.apache.log4j.*

Logger log  =Logger.getInstance('label.groovy');
log.info('== label.groovy - Start labels configuration');

try {
    String LABELS = System.getenv("JENKINS_LABELS") ? System.getenv("JENKINS_LABELS") : "";
    Jenkins jenkins =  Jenkins.getInstance();
    jenkins.setLabelString(LABELS);
    jenkins.save();
    log.info('== label.groovy - End labels configuration');
}
catch (Exception ex){
    log.error('== label.groovy :' + ex.message + '\n' + ex.getStackTrace());
}