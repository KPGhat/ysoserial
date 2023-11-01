package ysoserial.bullet.jndi;

import org.apache.naming.ResourceRef;

public interface TomcatRef {
    public ResourceRef getObject(String command) throws Exception;
}
