import jenkins.model.*
import com.cloudbees.hudson.plugins.folder.*;
import com.cloudbees.hudson.plugins.folder.properties.*;
import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider.FolderCredentialsProperty;
import com.cloudbees.plugins.credentials.impl.*;
import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.domains.*;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.*;
import hudson.util.Secret;
import groovy.json.JsonSlurper;
import org.apache.log4j.*;

Logger log = Logger.getInstance('credentials.groovy');

public class Credentials {
    private Object config;
    private Logger log = Logger.getInstance(getClass());


    public Credentials (String configFile){
        this.config = readConfig(configFile);
    }

    private Object readConfig(String configFile){
        try {
            JsonSlurper jsonSlurper = new JsonSlurper()
            File inputFile = new File(configFile)
            return jsonSlurper.parseFile(inputFile, 'UTF-8')
        }
        catch (Exception ex){
            log.error("== credentials.groovy - Can't read configuration file :" + ex.message);
        }
    }

    public void createCredentials(){
        try{
            int nbCred = this.config.size;
            for(int i =0; i < nbCred; i++){
                switch(this.config[i].type) {
                    case "secret_text" :
                        createSecretText(this.config[i].key,
                                        this.config[i].value, 
                                        this.config[i].folder, 
                                        this.config[i].key);
                    break
                    case "user_password":
                        createUsernamePassword(this.config[i].id,
                                               this.config[i].key,
                                               this.config[i].value,
                                               this.config[i].folder);
                    break
                    default: 
                        throw new Exception("Unsupported credentials type : " + this.config[i].type);
                }
            }
        }
      catch (Exception ex){
        log.error("== credentials.groovy - Can't create credentials :" + ex.message);
        }
    }

    private void createSecretText(String secretId, String value, String folderName, String description){
        StandardCredentials c = new StringCredentialsImpl(
                        CredentialsScope.GLOBAL,
                        secretId,
                        description,
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
log.info("== credentials.groovy - End Configuration");