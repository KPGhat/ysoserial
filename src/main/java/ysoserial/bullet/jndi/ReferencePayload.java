package ysoserial.bullet.jndi;

import org.apache.naming.ResourceRef;
import org.reflections.Reflections;

import javax.naming.Reference;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Set;

public interface ReferencePayload <T> {
    public T getReference(String command) throws Exception;

    public static class Utils {

        // get payload classes by classpath scanning
        public static Set<Class<? extends ReferencePayload>> getPayloadClasses() {
            final Reflections reflections = new Reflections(ReferencePayload.class.getPackage().getName());
            final Set<Class<? extends ReferencePayload>> payloadTypes = reflections.getSubTypesOf(ReferencePayload.class);
            for (Iterator<Class<? extends ReferencePayload>> iterator = payloadTypes.iterator(); iterator.hasNext(); ) {
                Class<? extends ReferencePayload> pc = iterator.next();
                if (pc.isInterface() || Modifier.isAbstract(pc.getModifiers())) {
                    iterator.remove();
                }
            }
            return payloadTypes;
        }

        public static Class<? extends ReferencePayload> getPayloadClass(final String className ) {
            Class<? extends ReferencePayload> clazz = null;
            try {
                clazz = (Class<? extends ReferencePayload>) Class.forName(className);
            }
            catch ( Exception e1 ) {}
            if ( clazz == null ) {
                try {
                    return clazz = (Class<? extends ReferencePayload>) Class
                        .forName(ReferencePayload.class.getPackage().getName() + "." + className);
                }
                catch ( Exception e2 ) {}
            }
            if ( clazz != null && !ReferencePayload.class.isAssignableFrom(clazz) ) {
                clazz = null;
            }
            return clazz;
        }
    }
}
