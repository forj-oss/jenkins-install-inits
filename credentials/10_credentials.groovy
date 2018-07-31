import jenkins.model.*;
import hudson.*;
import hudson.model.*;
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

    public void deleteAllCredentials(){
        def providers = jenkins.model.Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')
        
        for (p in providers){        
            for (d in p.getDomainCredentials()){
                for (c in d.getCredentials()) {
                    p.removeCredentials(d.getDomain(),c)
                }
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

    public void createSecretText(String secretId, String value, String folderName){
        StandardCredentials c = new StringCredentialsImpl(
                        CredentialsScope.GLOBAL,
                        secretId,
                        secretId, //description
                        Secret.fromString(value));
        saveCredentials(c, folderName);     
    }

    public void createUsernamePassword ( String id, String username, String password, String folderName){
        StandardCredentials c = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
                                                                                id,
                                                                                username,//description
                                                                                username,
                                                                                password);
         saveCredentials(c, folderName);  
    }

    public void createAWSCred (String id, String accessKey, String secretKey, String folderName){
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

    public void createSSHKey(String id, String userName, String privateKey, String folderName){
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

    public StandardCredentials getCredentialById(String id, Domain domain,  CredentialsStore store){
        List<Credentials> creds = store.getCredentials(domain);
        for (c in creds){
            if( c.id == id){
                return (StandardCredentials) c
            }
        }

        return null
    }

    private void saveCredentials( StandardCredentials c, String folderName){
        CredentialsStore store = SystemCredentialsProvider.getInstance().getStore()
        Domain domain = Domain.global()

        if(folderName != null && folderName != ""){     
            for(job in Jenkins.instance.getAllItems(ItemGroup)){
                if(job.fullName.contains(folderName)){
                    AbstractFolder<?> folderAbs  = AbstractFolder.class.cast(job)
                    FolderCredentialsProperty property = folderAbs.getProperties().get(FolderCredentialsProperty.class)

                    if(property != null){
                        store = property.getStore()
                    }
                    else{
                        println('== credentials.groovy - FolderCredentialsProperty object is null')
                        println('== credentials.groovy - Save to the default store')
                    } 
                }
            }
        }
            
        StandardCredentials current = getCredentialById(c.id,domain,store)
        if( current == null){
            log.info("== credentials.groovy - Create credential with id" + c.id)
            store.addCredentials(domain, c)
        }
        else {
            log.info("== credentials.groovy - Update credential with id" + c.id)
            store.updateCredentials(domain,current,c)
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