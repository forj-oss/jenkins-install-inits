/* This groovy file currently is quite simple.
   It takes care of limited parameters requested by the SonarRunnerInstallation//SonarRunnerInstaller constructors

   It supports only installer from DownloadFromUrl class. 
*/

/* TODO: Support multiple Sonar server declaration
   TODO: Support all Installation mode.
*/

import jenkins.model.Jenkins
import hudson.plugins.sonar.SonarRunnerInstallation
import hudson.plugins.sonar.SonarRunnerInstaller
import hudson.tools.InstallSourceProperty
import hudson.tools.ToolProperty

def env = System.getenv()

sonarRunnerName = env['SONAR_RUNNER_NAME']
sonarRunnerVersion = env['SONAR_RUNNER_VERSION']

if (!sonarRunnerName && !sonarRunnerVersion) {
   println('== sonar-runner.groovy - SONAR_RUNNER_NAME and SONAR_RUNNER_VERSION are missed. "SonarQube Scanner" section not configured.')
}
else {
   println('== sonar-runner.groovy - SONAR_RUNNER_NAME = ' + sonarRunnerName)
   println('== sonar-runner.groovy - SONAR_RUNNER_VERSION = ' + sonarRunnerVersion)

   // Getting the internal GlobalConfiguration derived object.
   def configs = Jenkins.instance.getDescriptor('hudson.plugins.sonar.SonarRunnerInstallation').getInstallations()
   
   if (configs.size() >= 1 &&
       configs[0].name == sonarRunnerName &&
       configs[0].getProperties().size() >=1 && 
       configs[0].getProperties()[0].installers.size() >= 1 &&
       configs[0].getProperties()[0].installers[0].id == sonarRunnerVersion) {
      println('== sonar.groovy - No change detected.')
   } 
   else {
     def SonarRunnerInstallation[] myConfigs = new SonarRunnerInstallation[1];

     def properties = new ArrayList<InstallSourceProperty>();

     def toolProperty = new InstallSourceProperty()

     toolProperty.installers.add(new SonarRunnerInstaller(sonarRunnerVersion))
     properties.add(toolProperty)

     myConfigs[0] = new SonarRunnerInstallation(sonarRunnerName, "", properties)
     Jenkins.instance.getDescriptor('hudson.plugins.sonar.SonarRunnerInstallation').setInstallations(myConfigs)
     println('== sonar-runner.groovy - "' + sonarRunnerName + '" version "'+ sonarRunnerVersion +'" has been configured as SonarQube Scanner.')
   }
}
