import jenkins.model.*
import com.cloudbees.hudson.plugins.folder.*;
import com.cloudbees.hudson.plugins.folder.properties.*;
import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider.FolderCredentialsProperty;
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever;
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration;
import jenkins.plugins.git.GitSCMSource;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMHeadAuthority;
import org.jenkinsci.plugins.*;
import org.jenkinsci.plugins.github_branch_source.*;
import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.domains.*;
import com.cloudbees.plugins.credentials.impl.*;
import groovy.json.JsonSlurper;
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries;
import org.apache.log4j.*;

public class SharedLib {
    private Object config;
    private Logger log = Logger.getInstance(getClass());

    public SharedLib(String configFile){
          this.config = readConfig(configFile);
    }

    private Object readConfig(String config){
        assert config!= null && config!= "" : "== shared-lib.groovy.readConfig - The config variables can't be null";

        File inputFile;
        try {
            JsonSlurper jsonSlurper = new JsonSlurper();
            inputFile = new File (config);
            return jsonSlurper.parse(inputFile, 'UTF-8');
        }
        catch (Exception ex){         
            if(! inputFile.exists()){
                log.error("== shared-lib.groovy - does not exist : " + config );
            }
            else{
                log.error("== shared-lib.groovy - Can't read configuration  :" + ex.message);
            }
        }
    }

    /**
    * Create a GitHub Enterprise Servers
    * @param apiUri GitHub API endpoint such as https://github.example.com/api/v3/.
    * @param apiName A string used to identify the endpoint
    */
    private void createApiEndpoint(String apiUri, Object apiName){ 
        assert apiUri != null && apiUri != "" : "== shared-lib.groovy.createApiEndpoint - The apiUri parameter can't be null";
        assert apiName != null && apiUri != "" : "== shared-lib.groovy.createApiEndpoint - The apiUri parameter can't be null";

        // For Jenkins Endpoints https://github.company.com/api/v3 == https://github.company.com/api/v3/
        // so we have to check both
        Endpoint tmp = GitHubConfiguration.get().getEndpoints().find { 
            it.getApiUri() == apiUri ||
            it.getApiUri() +'/' == apiUri
            };

        if ( tmp == null ) {
             tmp = GitHubConfiguration.get().getEndpoints().find { it.getApiUri() +'/' == apiUri };
        }

        if ( tmp == null ){
            Endpoint gh = new Endpoint(apiUri,apiName);
            if (GitHubConfiguration.get().addEndpoint(gh)){
                log.info("== shared-lib.groovy - Endpoint created :" + apiUri);
            }
            else {
                log.error("== shared-lib.groovy - Can't create the endpoint :" + apiUri);
            }
        }
        else {
            log.info("== shared-lib.groovy - Endpoint already exists :" + apiUri);
        }
    }

    private void createUsernamePassword ( String id, String username, String password, String folderName = ""){
        assert id != null && id !=  "" : "== shared-lib.groovy.createUsernamePassword - The id can't be null";
        assert username != null && username !=  "" : "== shared-lib.groovy.createUsernamePassword - The username can't be null";
        assert password != null && password !=  "" : "== shared-lib.groovy.createUsernamePassword - The password can't be null";

        StandardCredentials c = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
                                                                                id,
                                                                                username,
                                                                                username,
                                                                                password);
         saveCredentials(c, folderName);  
    }

    private void saveCredentials( StandardCredentials credential, String folderName = ""){
        assert credential != null : "== shared-lib.groovy.saveCredentials - The credential can't be null";

         if(folderName != null && folderName != ""){     
            for(job in Jenkins.instance.getAllItems(ItemGroup)){
                if(job.fullName.contains(folderName)){
                    AbstractItem<?> jobAbs = AbstractItem.class.cast(job)
                    FolderCredentialsProperty property = job.getProperties().get(FolderCredentialsProperty)
                    property.getStore().addCredentials(Domain.global(), credential)
                }
            }
        }
        else {
            SystemCredentialsProvider.getInstance().getStore().addCredentials(Domain.global(), credential);
        }
    }

    private List<SCMSourceTrait> getBehaviours(Object behaviours){
        assert behaviours != null : "== shared-lib.groovy.getBehaviours - behaviours can't be null";
        log.debug("== shared-lib.groovy.getBehaviours - "+ behaviours);

        List<SCMSourceTrait> res = [];

        /*
        * Authorized values are :
        * - 1 : Exclude branches that are also field as PR
        * - 2 : Only branches htat are also field as PR
        * - 3 : All branches
        */
        if(behaviours.branchDiscovery != null && behaviours.branchDiscovery > 0 && behaviours.branchDiscovery < 4) {
          BranchDiscoveryTrait bdt = new BranchDiscoveryTrait(behaviours.branchDiscovery);    
          res.add(bdt);
        }

        /*
        * Authorized values are :
        * - 1 : Merging the pull request with the current target branch revision
        * - 2 : The current pull request revision
        * - 3 : Both the current pull request revision and the pull resquest with the current target revision
        */
        if(behaviours.originPRDiscovery != null && behaviours.originPRDiscovery > 0 && behaviours.originPRDiscovery < 4) {
            OriginPullRequestDiscoveryTrait oprd = new OriginPullRequestDiscoveryTrait(behaviours.originPRDiscovery);
            res.add(oprd);
        }

        /*
        * Strategy authorized values are :
        * - 1 : Merging the pull request with the current target branch revision
        * - 2 : The current pull request revision
        * - 3 : Both the current pull request revision and the pull resquest with the current target revision
        *
        * Trust authorized values are :
        *   - TrustNobody : Nobody
        *   - TrustContributors : Contributors only
        *   - TrustPermission : For users with Admin or write permission
        *   - TrustEveryone : Everyone
        */
        if(behaviours.forkPRDiscovery != null && behaviours.forkPRDiscovery.trust != null &&
           behaviours.forkPRDiscovery.strategyId > 0 && behaviours.forkPRDiscovery.strategyId < 4){

            SCMHeadAuthority<SCMSourceRequest, PullRequestSCMHead, PullRequestSCMRevision> auth;
            switch(behaviours.forkPRDiscovery.trust) {
                case "TrustNobody":
                     auth = new ForkPullRequestDiscoveryTrait.TrustNobody();
                break
                case "TrustContributors":
                     auth = new ForkPullRequestDiscoveryTrait.TrustContributors();
                break
                case "TrustPermission":
                     auth = new ForkPullRequestDiscoveryTrait.TrustPermission();
                break
                case "TrustEveryone":
                     auth = new ForkPullRequestDiscoveryTrait.TrustEveryone();
                break
                default:
                    throw new Exception("Unrecognize SCMSourceTrait in behaviours :" + behaviours.forkPRDiscovery.trust);
                break
            }

            ForkPullRequestDiscoveryTrait fprd = new ForkPullRequestDiscoveryTrait(behaviours.forkPRDiscovery.strategyId,auth);
            res.add(fprd);
        }

        return res;
    } 

    private SCMSourceRetriever createGitHubRetriever (String credentialId,String apiUri, String owner, String repository, Object behaviours){
        assert credentialId != null && credentialId != "" : "== shared-lib.groovy.createGitHubRetriever - CredentialId can't be null";
        assert apiUri != null && apiUri != "" : "== shared-lib.groovy.createGitHubRetriever - apiUri can't be null";
        assert owner != null && owner != "" : "== shared-lib.groovy.createGitHubRetriever - owner can't be null";
        assert repository != null && repository != "" : "== shared-lib.groovy.createGitHubRetriever - repository can't be null";

        GitHubSCMSource src = new GitHubSCMSource(owner, repository);
        src.setCredentialsId(credentialId);
        src.setApiUri(apiUri);

        List<SCMSourceTrait> beh = getBehaviours(behaviours);
        if (beh != null && beh.size > 0){
            src.setTraits(beh);
        }

        return new SCMSourceRetriever(src);    
    }

    private LibraryConfiguration createSharedLib(Object config, SCMSourceRetriever retriever ){
        assert config != null : "== shared-lib.groovy.createSharedLib - The config object can't be null";
        assert retriever != null : "== shared-lib.groovy.createSharedLib -The retriever SCMSourceRetriever can't be null";

        LibraryConfiguration pipeline = new LibraryConfiguration(config.name, retriever);
        pipeline.setDefaultVersion(config.default_version);
        pipeline.setImplicit(config.implicitly_load);
        pipeline.setAllowVersionOverride(config.allow_override_version);   

        assert pipeline != null :"== shared-lib.groovy.createSharedLib - The returned pipeline should not be null";
        return pipeline;
    }

    public void createLibs(){
        try{
            if(this.config != null){
                List<LibraryConfiguration> libs = [];
          
                for(int i=0; i < this.config.size; i++){
                    LibraryConfiguration lib;
                    if(this.config[i].retrieval_method == "modern"){
                        createApiEndpoint(
                                        this.config[i].scm.endpoint.url,
                                        this.config[i].scm.endpoint.name);

                        createUsernamePassword (
                                            this.config[i].scm.credentials.id,
                                            this.config[i].scm.credentials.login,
                                            this.config[i].scm.credentials.password,
                                            "");
                        
                        SCMSourceRetriever gitHub = createGitHubRetriever  (
                                            this.config[i].scm.credentials.id,
                                            this.config[i].scm.endpoint.url,
                                            this.config[i].scm.owner,
                                            this.config[i].scm.repository,
                                            this.config[i].scm.behaviours);
                        lib = createSharedLib(this.config[i], gitHub); 
                    }

                    if(lib != null) {
                        libs.add(lib);
                    }
                }

                Hudson jenkins = Jenkins.getInstance();
                GlobalLibraries globalLibsDesc = jenkins.getDescriptor("org.jenkinsci.plugins.workflow.libs.GlobalLibraries");
                globalLibsDesc.get().setLibraries(libs);
            }
        }
        catch (Exception ex){
            log.error("== shared-lib.groovy - Can't create libs :" + ex.message);
        }
    }
}

Logger log = Logger.getInstance('credentials.groovy');
log.info("== shared-lib.groovy - Start Configuration");
String SHARED_LIB_PATH = System.getenv("SHARED_LIB_PATH");
SharedLib s = new SharedLib(SHARED_LIB_PATH);
s.createLibs();
log.info("== shared-lib.groovy - End Configuration");


/***** GIT SOURCE - LEGACY SCM *****/
/*
GitSCMSource src = new GitSCMSource(
        "jenkins-shared-lib", //id
        "mygitrepo", //remote
        "my id2", //credential id
        "*", //remoteName
        "", //rawRefSpecs
        false);
        */