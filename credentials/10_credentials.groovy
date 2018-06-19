import jenkins.model.*
import com.cloudbees.hudson.plugins.folder.*;
import com.cloudbees.hudson.plugins.folder.properties.*;
import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider.FolderCredentialsProperty;
import com.cloudbees.plugins.credentials.impl.*;
import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.domains.*;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*;
import org.jenkinsci.plugins.plaincredentials.impl.*;
import hudson.util.Secret;
import groovy.json.JsonSlurper;
import org.apache.log4j.*;
import java.io.File;


Logger log = Logger.getInstance('credentials.groovy');

public class Credentials {
    private Object config;
    private Logger log = Logger.getInstance(getClass());


    public Credentials (String configFile){
        this.config = readConfig(configFile);
    }

    private Object readConfig(String configFile){
       File inputFile;
        try {
            JsonSlurper jsonSlurper = new JsonSlurper()
            inputFile = new File(configFile)
            return jsonSlurper.parseFile(inputFile, 'UTF-8')
        }
        catch (Exception ex){         
            if(! inputFile.exists()){
                log.error("== credentials.groovy -File does not exist : " + configFile );
            }
            else{
                log.error("== credentials.groovy - Can't read configuration file :" + ex.message);
            }
        }
    }

    public void createCredentials(){
        try{
            if(this.config != null){
                int nbCred = this.config.size;
                for(int i =0; i < nbCred; i++){
                    switch(this.config[i].type) {
                        case "secret_text" :
                            createSecretText(this.config[i].id,
                                            this.config[i].value, 
                                            this.config[i].folder);
                        break
                        case "user_password":
                            createUsernamePassword(this.config[i].id,
                                                this.config[i].key,
                                                this.config[i].value,
                                                this.config[i].folder);
                        break
                        case "aws_credentials":
                            createAWSCred (this.config[i].id,
                                        this.config[i].key,
                                        this.config[i].value,
                                        this.config[i].folder);
                        break
                        case "ssh_credentials":
                            createSSHKey(this.config[i].id,
                                        this.config[i].key,
                                        this.config[i].value,
                                        this.config[i].folder);
                        break
                        default: 
                            throw new Exception("Unsupported credentials type : " + this.config[i].type);
                    }
                }
            }
        }
      catch (Exception ex){
        log.error("== credentials.groovy - Can't create credentials :" + ex.message);
        }
    }

    private void createSecretText(String secretId, String value, String folderName){
        StandardCredentials c = new StringCredentialsImpl(
                        CredentialsScope.GLOBAL,
                        secretId,
                        secretId, //description
                        Secret.fromString(value));
        saveCredentials(c, folderName);     
    }

    private void createUsernamePassword ( String id, String username, String password, String folderName){
        StandardCredentials c = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
                                                                                id,
                                                                                username,//description
                                                                                username,
                                                                                password);
         saveCredentials(c, folderName);  
    }

    private void createAWSCred (String id, String accessKey, String secretKey, String folderName){
        try {
            Class o = this.class.classLoader.loadClass("com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl");
            Object[] args = new Object[5];
            args[0] = CredentialsScope.GLOBAL;
            args[1] = id;
            args[2] = accessKey;
            args[3] = secretKey;
            args[4] = id;
            StandardCredentials c = (StandardCredentials) o.newInstance(args);
            /*
            The line above is replacing the AWSCredentialsImpl constructor : 
            StandardCredentials c = new AWSCredentialsImpl(
                CredentialsScope.GLOBAL,
                id,
                accessKey,
                secretKey,
                id );*/ 
            saveCredentials(c, folderName);
        }
        catch (ClassNotFoundException ex){
            log.error("== credentials.groovy -Can't load the class AWSCredentialsImpl, you should probably activate the plugin aws-credentials :" + ex.message);
        }   
    }

    private void createSSHKey(String id, String userName, String privateKey, String folderName){
        StandardCredentials c = new BasicSSHUserPrivateKey(
          CredentialsScope.GLOBAL, //scope
          id,
          "userName", //username
          new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(privateKey),
          "", //passphrase
          id //description
        );

        saveCredentials(c, folderName);
    }

    private void saveCredentials( StandardCredentials c, String folderName){
         if(folderName != null && folderName != ""){     
            for(job in Jenkins.instance.getAllItems(ItemGroup)){
                if(job.fullName.contains(folderName)){
                    AbstractItem<?> jobAbs = AbstractItem.class.cast(job)
                    FolderCredentialsProperty property = job.getProperties().get(FolderCredentialsProperty)
                    property.getStore().addCredentials(Domain.global(), c)
                }
            }
        }
        else {
            SystemCredentialsProvider.getInstance().getStore().addCredentials(Domain.global(), c)
        }
    }
}

log.info("== credentials.groovy - Start Configuration");
String CRED_PATH = System.getenv("CRED_PATH");
Credentials c = new Credentials(CRED_PATH);
c.createCredentials();

if(new File(CRED_PATH).delete()){
    log.info("== credentials.groovy - Credential file deleted :" + CRED_PATH);
}
else {
    log.error("== credentials.groovy - Can't delete the credential file :" + CRED_PATH);
}

log.info("== credentials.groovy - End Configuration");