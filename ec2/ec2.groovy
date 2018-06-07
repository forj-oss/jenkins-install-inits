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

/**
* Create an ec2 slave configuration
* @param sconfig contains the slave configuration coming from the ec2.json file
* @return a SlaveTemplate object containing the slave configuration
*/
SlaveTemplate createSlaveTemplate(Object sconfig){
  String IAM_ARN_SLAVE = System.getenv("IAM_ARN_SLAVE")

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
    IAM_ARN_SLAVE, //sconfig.iamInstanceProfile,
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

/**
* Create an ec2 coud configuration
* @param config the object retrieved from the ec2.json file
* @param templates the list of configuration associated to the cloud
* @return an EC2 cloud configuration
*/
AmazonEC2Cloud createAmazonEC2Cloud (Object config, List<SlaveTemplate>  templates) {
    String EC2_PRIVATE_KEY = System.getenv("EC2_PRIVATE_KEY")? System.getenv("EC2_PRIVATE_KEY"): ""

    return new AmazonEC2Cloud(
      config.cloudName,  // String
      config.useInstanceProfileForCredentials, // Boolean
      "ec2-aws-credentials",//config.credentialsId, // String
      config.region, // String
      EC2_PRIVATE_KEY, //String
      config.instanceCapStr, //String
      templates //List<? extends SlaveTemplate> 
      )
}

/**
* Create a lis of ec2Tags from a Map
* @param tags a map contaning the tag to create
* @return List<EC2Tag> containig the tag associated to a slave
*/
List<EC2Tag> createTags (Map tags){
  try {
    Map map = tags
    List<EC2Tag> ec2Tags = []
    tags.each { entry ->  ec2Tags.push(new EC2Tag(entry.key, entry.value)) }
    return ec2Tags
  }
  catch (Exception ex){
    println ("Can't process the tags : " + ex.message)
  }
}

/////////////////////////////////////////// MAIN ///////////////////////////////////////////////
try {
  // check aws environment variables
  String AWS_ACCESS_KEY_ID = System.getenv("AWS_ACCESS_KEY_ID")
  assert AWS_ACCESS_KEY_ID != null : "Then env var AWS_ACCESS_KEY_ID must not be empty"
  String AWS_SECRET_ACCESS_KEY = System.getenv("AWS_SECRET_ACCESS_KEY")
  assert AWS_SECRET_ACCESS_KEY != null: "Then env var AWS_SECRET_ACCESS_KEY must not be empty"

  // get Jenkins instance
  Jenkins jenkins = Jenkins.getInstance()
  
  // get credentials domain
  Domain domain = Domain.global()

  // get credentials store
  def store = jenkins.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

  // set the credentials used to access aws
  AWSCredentialsImpl aWSCredentialsImpl = new AWSCredentialsImpl(
    CredentialsScope.GLOBAL,
    "ec2-aws-credentials",
    AWS_ACCESS_KEY_ID,
    AWS_SECRET_ACCESS_KEY,
    "Credentials created by the ec2 groovy configuration script"
  )

  // add credentials to store
  store.addCredentials(domain, aWSCredentialsImpl)

  // configure ec2 plugins clouds
  JsonSlurper jsonSlurper = new JsonSlurper()
  File inputFile = new File("/tmp/ec2.json")
  Object config = jsonSlurper.parseFile(inputFile, 'UTF-8')

  if( !config){
    throw new Exception("ec2.groovy : Can't parse the ec2.json file")
  }

  for(i=0; i < config.size; i++){
    switch (config[i].cloudType) {
      case "amazonEC2Cloud": 
        List<SlaveTemplate> templates = []

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