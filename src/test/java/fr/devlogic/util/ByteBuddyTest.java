package fr.devlogic.util;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.List;

public class ByteBuddyTest {
    @Test
    public void proxyList() throws InstantiationException, IllegalAccessException {
        InvocationHandler invocationHandler = (proxy, method, args) -> {
            System.out.println(String.format("proxy %s method %s ", proxy.toString(), method));
            return null;
        };

        Class<? extends List> c = new ByteBuddy()
                .subclass(ArrayList.class)
                .method(ElementMatchers.any())
                .intercept(InvocationHandlerAdapter.of(invocationHandler))
                .make()
                .load(List.class.getClassLoader())
                .getLoaded();

        List list = c.newInstance();
        Assertions.assertNotNull(list);
    }
}
