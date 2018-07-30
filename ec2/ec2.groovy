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
import org.apache.log4j.*


/**
* Create an ec2 slave configuration
* @param sconfig contains the slave configuration coming from the ec2.json file
* @return a SlaveTemplate object containing the slave configuration
*/
SlaveTemplate createSlaveTemplate(Object sconfig){
  log.debug('== ec2.groovy - createSlaveTemplate')
  assert sconfig != null : "== ec2.groovy.createSlaveTemplate - The configuration object can't be null"

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
    new UnixData(sconfig.rootCommandPrefix, sconfig.slaveCommandPrefix, sconfig.sshPort),
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
    log.debug('== ec2.groovy - createAmazonEC2Cloud')
    assert config != null : "== ec2.groovy - The configuration object can't be null"
    assert templates !=  null && templates.size > 0 : "The template list can't be null or empty"

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
  log.debug('== ec2.groovy - createTags')
  assert tags != null : "The tags object can't be null"

  try {
    List<EC2Tag> ec2Tags = []
    tags.each { entry ->  ec2Tags.push(new EC2Tag(entry.key, entry.value)) }
    return ec2Tags
  }
  catch (Exception ex){
    log.error ("== ec2.groovy - Can't create tags : " + ex.message)
  }
}

Object readConfig(String config){
    log.debug('== ec2.groovy - readConfig')
    assert config!= null && config!= "" : "== ec2.groovy.readConfig - The config variables can't be null";

    File inputFile;
    try {
        JsonSlurper jsonSlurper = new JsonSlurper();
        inputFile = new File (config);
        return jsonSlurper.parse(inputFile, 'UTF-8');
    }
    catch (Exception ex){         
        if(! inputFile.exists()){
            log.error("== ec2.groovy - does not exist : " + config );
        }
        else{
            log.error("== ec2.groovy - Can't read configuration  :" + ex.message);
        }
    }
}

Boolean validConfig(Object config){
  Boolean res = true;
  
  // Don t duplicate cloud Name
  int size = config.size();
  List<String> cloudNames = []
  for(int i = 0; i < size; i++){
    if (! cloudNames.contains(config[i].cloudName)){
      cloudNames.add(config[i].cloudName);
    }
    else {
      res = false;
      throw new Exception('The cloud name must be unique (' + config[i].cloudName + ')')
    }
  }

  return res;

}

/////////////////////////////////////////// MAIN ///////////////////////////////////////////////
try {
  // In groovy script we declare global variables without specifying type
  log = Logger.getInstance('ec2.groovy');
  log.info('== ec2.groovy - Start ec2 configuration')

  // check aws environment variables
  log.debug('== ec2.groovy - Read environment variables')
  String AWS_ACCESS_KEY_ID = System.getenv("AWS_ACCESS_KEY_ID")
  assert AWS_ACCESS_KEY_ID != null : "The env var AWS_ACCESS_KEY_ID must not be empty"
  String AWS_SECRET_ACCESS_KEY = System.getenv("AWS_SECRET_ACCESS_KEY")
  assert AWS_SECRET_ACCESS_KEY != null: "The env var AWS_SECRET_ACCESS_KEY must not be empty"
  String EC2_CONFIG = System.getenv("EC2_CONFIG")
  assert EC2_CONFIG != null : "The env var EC2_CONFIG must not be empty"

  // get Jenkins instance
  Jenkins jenkins = Jenkins.getInstance()
  
  // get credentials domain
  Domain domain = Domain.global()

  // set the credentials used to access aws
  log.debug('== ec2.groovy - Create AWS credentials')
  def store = jenkins.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

  AWSCredentialsImpl aWSCredentialsImpl = new AWSCredentialsImpl(
    CredentialsScope.GLOBAL,
    "ec2-aws-credentials",
    AWS_ACCESS_KEY_ID,
    AWS_SECRET_ACCESS_KEY,
    "Credentials created by the ec2 groovy configuration script"
  )
  store.addCredentials(domain, aWSCredentialsImpl)

  // configure ec2 plugins clouds
  log.debug('== ec2.groovy - Configure the EC2 plugin')
  Object config = readConfig(EC2_CONFIG)
 
  if( !config){
    throw new Exception("== ec2.groovy : Can't parse the "+ EC2_CONFIG + " file")
  }

  if(validConfig(config)){
    // clean existing cloud before create the new ones
    log.info('== ec2.groovy - Delete existing ec2 clouds')
    jenkins.clouds.removeAll(hudson.plugins.ec2.AmazonEC2Cloud)

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
          jenkins.clouds.add(amazonEC2Cloud)
          break;
        default: break
      }
    }

    // save current Jenkins state to disk
    jenkins.save()
  }
  log.info('== ec2.groovy - End ec2 configuration')

}
catch (Exception ex){
  log.error ("== ec2.groovy - " + ex.message)
}