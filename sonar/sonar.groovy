/* This groovy file currently is quite simple.
   It takes care of limited parameters requested by the SonarInstallation constructor

   It supports only Sonar version 5.3 (running only with Token)
*/

/* TODO: Support multiple Sonar server declaration
   TODO: Support all fields requested by Sonar.
*/

import jenkins.model.Jenkins
import hudson.plugins.sonar.SonarInstallation
import hudson.plugins.sonar.SonarGlobalConfiguration
import hudson.plugins.sonar.model.TriggersConfig
import hudson.plugins.sonar.utils.SQServerVersions

void newSonarServer(String sonarName,String sonarServer,String sonarToken) {
 // For details see https://github.com/SonarSource/jenkins-sonar-plugin/blob/master/src/main/java/hudson/plugins/sonar/model/TriggersConfig.java#L75
 def triggers = new TriggersConfig(false, false, "")

 // For details see https://github.com/SonarSource/jenkins-sonar-plugin/blob/master/src/main/java/hudson/plugins/sonar/SonarInstallation.java#L99
 def sonarInstallation = new SonarInstallation(sonarName,
                                         sonarServer, SQServerVersions.SQ_5_3_OR_HIGHER, sonarToken,
                                         "", "", "",
                                         "", "", triggers,
                                         "", "", "")
 SonarInstallation[] configs = new SonarInstallation[1];
 configs[0] = sonarInstallation;
 Jenkins.instance.getDescriptor('hudson.plugins.sonar.SonarGlobalConfiguration').setInstallations(configs)
 println('== sonar.groovy - plugin configured.')
}

def env = System.getenv()

sonarName = env['SONAR_NAME']
sonarServer = env['SONAR_URL']
sonarToken = env['SONAR_TOKEN']

if (!sonarName && !sonarServer) {
   println('== sonar.groovy - SONAR_URL or/and SONAR_NAME are missed. Plugin not configured.')
}
else {
   println('== sonar.groovy - SONAR_NAME = ' + sonarName)
   println('== sonar.groovy - SONAR_URL = ' + sonarServer)
   if (sonarToken)
      println('== sonar.groovy - SONAR_TOKEN = "***"')
   else
      println('== sonar.groovy - SONAR_TOKEN is empty')

   // Getting the internal GlobalConfiguration derived object.
   def configs = Jenkins.instance.getDescriptor('hudson.plugins.sonar.SonarGlobalConfiguration').getInstallations()
   
   if (configs.size() >= 1) {
      if ( configs[0].getName() == sonarName && 
           configs[0].getServerUrl() == sonarServer && 
           configs[0].getServerAuthenticationToken() == sonarToken &&
           configs[0].getMojoVersion() == "" &&
           configs[0].getDatabaseUrl() == "" &&
           configs[0].getDatabaseLogin() == "" &&
           configs[0].getDatabasePassword() == "" &&
           configs[0].getAdditionalProperties() == "" &&
           configs[0].getAdditionalAnalysisPropertiesWindows() == "" &&
           configs[0].getSonarLogin() == "" &&
           configs[0].getSonarPassword() == "") {
         println('== sonar.groovy - No change detected.')
      } 
      else {
        newSonarServer(sonarName, sonarServer, sonarToken)
      }
   }
   else {
     newSonarServer(sonarName, sonarServer, sonarToken)
   }
}
