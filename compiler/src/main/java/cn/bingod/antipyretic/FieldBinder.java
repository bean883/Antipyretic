package cn.bingod.antipyretic;

import javax.lang.model.type.TypeMirror;

final class FieldBinder {

    String name;
    TypeMirror typeMirror;
    String paramKey;
    int paramType;

    FieldBinder(String name, TypeMirror typeMirror, String paramKey, int paramType) {
        this.name = name;
        this.typeMirror = typeMirror;
        this.paramKey = paramKey;
        this.paramType = paramType;
    }
}
