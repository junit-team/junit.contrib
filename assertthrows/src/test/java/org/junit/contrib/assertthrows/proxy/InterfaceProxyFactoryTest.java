package org.junit.contrib.assertthrows.proxy;

import static org.junit.Assert.assertEquals;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.contrib.assertthrows.AssertThrows;

/**
 * Test creating proxies using the interface proxy factory.
 *
 * @author Thomas Mueller
 */
public class InterfaceProxyFactoryTest {

    StringBuilder buff = new StringBuilder();
    int methodCallCount;

    @Test
    public void testObject() {
        new AssertThrows(new IllegalArgumentException(
                "Can not create a proxy using the InterfaceProxyFactory, " +
                "because the class java.lang.Object does not implement any interfaces")) {
            public void test() {
                createProxy(new Object()).equals(null);
        }};
    }

    @Test
    public void testJavaUtilList() {
        List<String> list = new ArrayList<String>();
        createProxy(list).size();
        assertEquals("size = 0", buff.toString());
    }

    <T> T createProxy(final T obj) {
        return InterfaceProxyFactory.getInstance().createProxy(obj, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
                buff.append(method.getName());
                Object o = method.invoke(obj, args);
                buff.append(" = ").append(o);
                methodCallCount++;
                return o;
            }
        });
    }

}
