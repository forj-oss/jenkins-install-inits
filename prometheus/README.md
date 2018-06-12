# Prometheus plugin

This feature is used to send metrics data from Jenkins to Prometheus


## Envionment variables
To configure the plugin, you will have to set the following environment variables :

- PROM_PATH           : Prometheus server path
- PROM_NAMESPACE      : Set the data namespace
- PROM_AUTH_ENDPOINT  : Boolean defining if prometheus use an authenticated endpoint
- PROM_COUNT_SUCCESS  : Boolean defining if the number of successfull build should be gathered
- PROM_COUNT_UNSTABLE : Boolean defining if the number of unstable build should be gathered
- PROM_COUNT_FAILED   : Boolean defining if the number of failed build should be gathered
- PROM_COUNT_NOT_BUILT: Boolean defining if the number of not built build should be gathered
- PROM_COUNT_ABORTED  : Boolean defining if the number of aborted build should be gathered
- PROM_FETCH_TEST_RES : Boolean defining if the test results should be fetch
- PROM_PROC_DISABLE   : ?
