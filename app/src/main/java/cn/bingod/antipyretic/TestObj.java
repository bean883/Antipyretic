package cn.bingod.antipyretic;

import java.io.Serializable;

public class TestObj implements Serializable {
    private String foo;

    public TestObj(String foo) {
        this.foo = foo;
    }

    @Override
    public String toString() {
        return "TestObj{" + "foo='" + foo + '\'' + '}';
    }
}
