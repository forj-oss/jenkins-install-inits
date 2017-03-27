import jenkins.model.Jenkins
import org.jenkinsci.plugins.mesos.*
import org.apache.mesos.Protos.ContainerInfo.DockerInfo.Network;
import hudson.model.Node;

// Initialize Mesos with Docker

def env = System.getenv()

slave_image = env['MESOS_SLAVE_DOCKER_IMAGE']
mesos_master = env['MESOS_MASTER']

if (!slave_image) {
   slave_image = 'clarsonneur/jenkins-single-dind'
   println("== mesos.groovy - MESOS_SLAVE_DOCKER_IMAGE not set. Using predefined example image.")
}

println("== mesos.groovy - Slave image set to '" + slave_image + "'")

if (!mesos_master) {
   println("== mesos.groovy - MESOS_MASTER not set. Unable to properly configure mesos plugin. Configuration aborted.")
}
else {
   println("== mesos.groovy - Configuring mesos plugin with master '" + mesos_master + "'")
   def containerInfo = new MesosSlaveInfo.ContainerInfo(
     "docker",                     // String type,
     slave_image,                  // String dockerImage, 
     Boolean.FALSE,                // Boolean dockerPrivilegedMode,
     Boolean.FALSE,                // Boolean dockerForcePullImage,
     Boolean.FALSE,                // boolean useCustomDockerCommandShell,
     '',                           // String customDockerCommandShell,
     Collections.<MesosSlaveInfo.Volume>emptyList(),       // List<Volume> volumes
     Collections.<MesosSlaveInfo.Parameter>emptyList(),    // List<Parameter> parameters,
     Network.BRIDGE.name(),                                // String networking,
     Collections.<MesosSlaveInfo.PortMapping>emptyList()   // List<PortMapping> portMappings)
     )
  
   println(Network.BRIDGE.name())

   def slaveInfo=new MesosSlaveInfo(
     'mesos',///      labelString,
     Node.Mode.NORMAL, ///      Mode mode,
     '0.1', ///      String slaveCpus,
     '512', ///      String slaveMem,
     '2',   ///      String maxExecutors,
     '0.1', ///      String executorCpus,
     '128', ///      String executorMem,
     'jenkins', ///  String remoteFSRoot,
     '3',   ///      String idleTerminationMinutes,
     '',    ///      String slaveAttributes,
     '',    ///      String jvmArgs,
     '',    ///      String jnlpArgs,
     'false', ///    String defaultSlave,
     containerInfo,//ContainerInfo containerInfo,
     null,   //      List<URI> additionalURIs,
     null)   //      List<? extends NodeProperty<?>> nodeProperties
  
   List<MesosSlaveInfo> slaveInfos = new ArrayList<MesosSlaveInfo> ()
   
   slaveInfos.add(slaveInfo)

   def myCloud= new MesosCloud(
     '/usr/local/lib/libmesos.so', // String nativeLibraryPath,
     mesos_master,                 // String master,
     '',                           // String description,
     'Jenkins Scheduler',          // String frameworkName,
     '*',                          // String role,
     '',                           // String slavesUser,
     '',                           // String credentialsId,
     '',                           // String principal,
     '',                           // String secret,
     slaveInfos,                   // List<MesosSlaveInfo> slaveInfos,
     Boolean.FALSE,                // boolean checkpoint,
     Boolean.FALSE,                // boolean onDemandRegistration,
     '',                           // String jenkinsURL,
     ''                            // String declineOfferDuration
   )

   Jenkins.instance.clouds.add(myCloud)

   println('== mesos.groovy - Mesos plugin configured.')
}
