package com.mf.mockito.remote;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class SerialisableMethod implements Serializable {
    private String name;
    private List<String> parameterTypes;

    SerialisableMethod(Method method) {
        this.name = method.getName();
        this.parameterTypes = new ArrayList<>();
        for (Class<?> realParameterType : method.getParameterTypes()) {
            parameterTypes.add(realParameterType.getName());
        }
    }

    Method getJavaMethodFrom(Object mock) {
        try {
            return mock.getClass().getMethod(name, getRealParameterTypes());
        } catch (NoSuchMethodException | ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }

    private Class<?>[] getRealParameterTypes() throws ClassNotFoundException {
        List<Class> realParameterTypes = new ArrayList<>();
        for (String parameterType : parameterTypes) {
            realParameterTypes.add(Class.forName(parameterType));
        }
        return realParameterTypes.toArray(new Class<?>[realParameterTypes.size()]);
    }
}
