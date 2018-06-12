//https://github.com/jenkinsci/prometheus-plugin/blob/master/src/main/java/org/jenkinsci/plugins/prometheus/config/PrometheusConfiguration.java
import org.jenkinsci.plugins.prometheus.*
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration

try{
    println ("== prometheus.groovy - Start configuration");

    // Retrieve environment variables
    String PROM_PATH = System.getenv("PROM_PATH");
    assert PROM_PATH != null && PROM_PATH != "" : "== prometheus.groovy - Prometheus PATH variable is required"
    String PROM_NAMESPACE = System.getenv("PROM_NAMESPACE") != null ? System.getenv("PROM_NAMESPACE") : "Default" ;
    Boolean PROM_AUTH_ENDPOINT = System.getenv("PROM_AUTH_ENDPOINT") != null ? System.getenv("PROM_AUTH_ENDPOINT").toBoolean() : false;
    Boolean PROM_COUNT_SUCCESS = System.getenv("PROM_COUNT_SUCCESS") != null ? System.getenv("PROM_COUNT_SUCCESS").toBoolean() : false;
    Boolean PROM_COUNT_UNSTABLE = System.getenv("PROM_COUNT_UNSTABLE") != null ? System.getenv("PROM_COUNT_UNSTABLE").toBoolean() : false;
    Boolean PROM_COUNT_FAILED = System.getenv("PROM_COUNT_FAILED") != null ? System.getenv("PROM_COUNT_FAILED").toBoolean() : false;
    Boolean PROM_COUNT_NOT_BUILT = System.getenv("PROM_COUNT_NOT_BUILT") != null ? System.getenv("PROM_COUNT_NOT_BUILT").toBoolean() : false;
    Boolean PROM_COUNT_ABORTED = System.getenv("PROM_COUNT_ABORTED") != null ? System.getenv("PROM_COUNT_ABORTED").toBoolean() : false;
    Boolean PROM_FETCH_TEST_RES = System.getenv("PROM_FETCH_TEST_RES") != null ? System.getenv("PROM_FETCH_TEST_RES").toBoolean() : false;
    Boolean PROM_PROC_DISABLE = System.getenv("PROM_PROC_DISABLE") != null ? System.getenv("PROM_PROC_DISABLE").toBoolean() : false;

    // Retrieve the prometheus configuration object
    def p = PrometheusConfiguration.get();
    p.setPath(PROM_PATH);
    p.setDefaultNamespace(PROM_NAMESPACE);
    p.setUseAuthenticatedEndpoint(PROM_AUTH_ENDPOINT);
    p.setCountSuccessfulBuilds(PROM_COUNT_SUCCESS);
    p.setCountUnstableBuilds(PROM_COUNT_UNSTABLE);
    p.setCountFailedBuilds(PROM_COUNT_FAILED);
    p.setCountNotBuiltBuilds(PROM_COUNT_NOT_BUILT);
    p.setCountAbortedBuilds(PROM_COUNT_ABORTED);
    p.setFetchTestResults(PROM_FETCH_TEST_RES);
    p.setProcessingDisabledBuilds(PROM_PROC_DISABLE);

    println ("== prometheus.groovy - End configuration");
}
catch(Exception ex){
    println("== prometheus.groovy - Error in the prometheus plugin configuration : " + ex.message);
}