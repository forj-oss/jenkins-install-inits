import jenkins.model.*
import hudson.*

def instance = Jenkins.getInstance()
def proxy    = instance.proxy
def env      = System.getenv()
proxy_env    = env['http_proxy']

if (proxy_env && proxy_env ==~ /^http:\/\/.*/) {
   proxy_name = proxy_env.substring(7).split(/:/)[0]
   proxy_port = proxy_env.substring(7).split(/:/)[1].toInteger()
   println("== proxy.groovy - Setting Proxy to ${proxy_name}:${proxy_port}")

   if ( proxy == null ) {
      proxy = new ProxyConfiguration(proxy_name, proxy_port)
      instance.proxy = proxy
   }
   else {
      proxy.name = proxy_name
      proxy.port = proxy_port
   }

}
else {
   if (proxy)
     {
      proxy.name = null
      proxy.port = null
     }
   println("== proxy.groovy - No Proxy set")
}

instance.save()
