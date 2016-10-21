package com.mf.mockito.remote;

import java.io.Serializable;
import java.lang.reflect.Method;

class SerialisableMethod implements Serializable {

    private static final long serialVersionUID = 9080760508871304411L;

    private String name;
    private Class<?>[] realParameterType;

    SerialisableMethod(Method method) {
        this.name = method.getName();
        realParameterType = method.getParameterTypes();
    }

    Method getJavaMethodFrom(Object mock) {
        try {
            return mock.getClass().getMethod(name, realParameterType);
        } catch (NoSuchMethodException exception) {
            throw new RuntimeException(exception);
        }
    }
}
