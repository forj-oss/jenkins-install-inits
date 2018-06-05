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
import groovy.json.JsonSlurper()


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
    sconfig.initScript,
    sconfig.tmpDir,
    sconfig.userData,
    sconfig.numExecutors,
    sconfig.remoteAdmin,
    new UnixData(null, null, null),
    sconfig.jvmopts,
    sconfig.stopOnTerminate,
    sconfig.subnetId,
    [sconfig.tags],
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

def createAmazonEC2Cloud (config, templates) {
    return new AmazonEC2Cloud(
      config.cloudName,
      config.useInstanceProfileForCredentials,
      config.credentialsId,
      config.region,
      config.privateKey, //TODO: need to retrieve the content of a file
      config.instanceCapStr,
      templates
      )
}

// get Jenkins instance
Jenkins jenkins = Jenkins.getInstance()
 
// get credentials domain
def domain = Domain.global()

// get credentials store
def store = jenkins.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

AWSCredentialsImpl aWSCredentialsImpl = new AWSCredentialsImpl(
  CredentialsScope.GLOBAL,
  "aws-credentials",
  System.getenv("AWS_ACCESS_KEY_ID").toString(),
  System.getenv("AWS_SECRET_ACCESS_KEY").toString(),
  "Credentials created by the ec2 groovy configuration script"
)

// add credential to store
store.addCredentials(domain, aWSCredentialsImpl)

// Configure clouds
 def slurper = new groovy.json.JsonSlurper()
 def config = slurper.parse("ec2.json")

for(i=0; i<config.size; i++){
  switch (config[i].cloudType {
    case "amazonEC2Cloud": 
      Array templates = new Array()

      // read slaves configuration and set the templates array
      for(j=0; j<config.slavesTemplate.size; j++){   
        SlaveTemplate template = createSlaveTemplate(config[i].slavesTemplate(j))
        templates.push(template)
      }

      AmazonEC2Cloud amazonEC2Cloud = createAmazonEC2Cloud(config[i], templates)

      // add cloud configuration to Jenkins
      jenkins.clouds.add(amazonEC2Cloud)
      break;
  }
  default: break
}
 
// save current Jenkins state to disk
jenkins.save()