import com.amazonaws.services.ec2.model.InstanceType
import com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.Domain
import hudson.model.*
import hudson.plugins.ec2.AmazonEC2Cloud
import hudson.plugins.ec2.AMITypeData
import hudson.plugins.ec2.EC2Tag
import hudson.plugins.ec2.SlaveTemplate
import hudson.plugins.ec2.SpotConfiguration
import hudson.plugins.ec2.UnixData
import jenkins.model.Jenkins
import groovy.json.JsonOutput
import groovy.json.JsonSlurper


def createSlaveTemplate(sconfig){
  return new SlaveTemplate(
    sconfig.ami,
    sconfig.zone,
    null,
    sconfig.securityGroups,
    sconfig.remoteFS,
    InstanceType.fromValue(sconfig.type),
    sconfig.ebsOptimized,
    sconfig.labelString,
    Node.Mode.NORMAL,
    sconfig.description,
    sconfig.initScript.join('\n'),
    sconfig.tmpDir,
    sconfig.userData,
    sconfig.numExecutors,
    sconfig.remoteAdmin,
    new UnixData(null, null, null),
    sconfig.jvmopts,
    sconfig.stopOnTerminate,
    sconfig.subnetId,
    createTags(sconfig.tags),
    sconfig.idleTerminationMinutes,
    sconfig.usePrivateDnsName,
    sconfig.instanceCapStr,
    sconfig.iamInstanceProfile,
    sconfig.deleteRootOnTermination,
    sconfig.useEphemeralDevices,
    sconfig.useDedicatedTenancy,
    sconfig.launchTimeoutStr,
    sconfig.associatePublicIp,
    sconfig.customDeviceMapping,
    sconfig.connectBySSHProcess,
    sconfig.connectUsingPublicIp               
  )
}

def createAmazonEC2Cloud (Object config, List<? extends SlaveTemplate>  templates) {
    return new AmazonEC2Cloud(
      config.cloudName,  // String
      config.useInstanceProfileForCredentials, // Boolean
      config.credentialsId, // String
      config.region, // String
      "",//System.getenv("EC2_PRIVATE_KEY").toString(), //String
      config.instanceCapStr, //String
      templates //List<? extends SlaveTemplate> 
      )
}

def createTags (Map tags){
  println(tags)

  try {
    Map map = tags
    def ec2Tags = []
    tags.each { entry ->  ec2Tags.push(new EC2Tag(entry.key, entry.value)) }
    
    println(ec2Tags)
    return ec2Tags
  }
  catch (Exception ex){
    println ("Can't process the tags : " + ex.message)
  }
}

/////////////////////////////////////////// MAIN ///////////////////////////////////////////////
// get Jenkins instance
Jenkins jenkins = Jenkins.getInstance()
 
// get credentials domain
def domain = Domain.global()

// get credentials store
def store = jenkins.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
String AWS_ACCESS_KEY_ID = System.getenv("AWS_ACCESS_KEY_ID").toString()
String AWS_SECRET_ACCESS_KEY = System.getenv("AWS_SECRET_ACCESS_KEY").toString()
AWSCredentialsImpl aWSCredentialsImpl = new AWSCredentialsImpl(
  CredentialsScope.GLOBAL,
  "aws-credentials",
  AWS_ACCESS_KEY_ID,
  AWS_SECRET_ACCESS_KEY,
  "Credentials created by the ec2 groovy configuration script"
)

// add credential to store
store.addCredentials(domain, aWSCredentialsImpl)

// Configure clouds
try {
  
  JsonSlurper jsonSlurper = new JsonSlurper()
  File inputFile = new File("/tmp/ec2.json")
  def config = jsonSlurper.parseFile(inputFile, 'UTF-8')

  if( !config){
    throw new Exception("ec2.groovy : Can't parse the ec2.json file")
  }

  for(i=0; i < config.size; i++){
    
    switch (config[i].cloudType) {
      case "amazonEC2Cloud": 
        def templates = []

        // read slaves configuration and set the templates array         
        for(j=0; j < config[i].slavesTemplate.size; j++){           
          SlaveTemplate template = createSlaveTemplate(config[i].slavesTemplate[j])
          templates.add(template)
        }

        AmazonEC2Cloud amazonEC2Cloud = createAmazonEC2Cloud(config[i], (List<? extends SlaveTemplate>) templates)

        // add cloud configuration to Jenkins
        jenkins.clouds.add(amazonEC2Cloud)
        break;
      default: break
    }
  }

  // save current Jenkins state to disk
  jenkins.save()
}
catch (Exception ex){
  println ("ec2.groovy : " + ex.message)
}
 
