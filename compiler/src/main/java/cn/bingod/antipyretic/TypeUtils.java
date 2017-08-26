package cn.bingod.antipyretic;

import java.io.Serializable;
import java.util.ArrayList;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * @author bin
 * @since 2017/7/20
 */
public class TypeUtils {

    private TypeMirror typeUri;
    private TypeMirror typeActivity;
    private TypeMirror typeParcelable;
    private TypeMirror typeSerializable;

    private Types types;
    private Elements elementUtils;

    TypeUtils(Types types, Elements elementUtils) {
        this.types = types;
        this.elementUtils = elementUtils;

        typeActivity = elementUtils.getTypeElement("android.app.Activity").asType();
        typeUri = elementUtils.getTypeElement("android.net.Uri").asType();
        typeSerializable = elementUtils.getTypeElement(Serializable.class.getCanonicalName()).asType();
        typeParcelable = elementUtils.getTypeElement("android.os.Parcelable").asType();
    }

    TypeMirror getUriType() {
        return typeUri;
    }

    boolean isActivity(TypeMirror typeMirror) {
        return types.isSubtype(typeMirror, typeActivity);
    }

    boolean isUri(TypeMirror typeMirror) {
        return types.isSameType(typeMirror, typeUri);
    }

    boolean isBaseType(TypeMirror typeMirror) {
        return isInt(typeMirror) || isByte(typeMirror) || isShort(typeMirror) || isLong(typeMirror)
                || isFloat(typeMirror) || isDouble(typeMirror) || isBool(typeMirror);
    }

    boolean isBaseArrayType(TypeMirror typeMirror) {
        return isIntArray(typeMirror) || isByteArray(typeMirror) || isShortArray(typeMirror) || isLongArray(typeMirror)
                || isFloatArray(typeMirror) || isDoubleArray(typeMirror) || isBoolArray(typeMirror);
    }

    boolean isCharSequence(TypeMirror typeMirror) {
        return typeMirror.toString().equals(CharSequence.class.getCanonicalName());
    }

    boolean isCharSequenceArray(TypeMirror typeMirror) {
        return typeMirror.toString().equals(CharSequence[].class.getCanonicalName());
    }

    boolean isCharSequenceArrayList(TypeMirror typeMirror) {
        return typeMirror.toString().equals(ArrayList.class.getCanonicalName() + "<" + CharSequence.class.getCanonicalName() + ">");
    }

    boolean isParcelable(TypeMirror typeMirror) {
        return types.isSameType(typeMirror, typeParcelable);
    }

    boolean isParcelableImpl(TypeMirror typeMirror) {
        return types.isSubtype(typeMirror, typeParcelable);
    }

    boolean isParcelableArray(TypeMirror typeMirror) {
        return typeMirror.toString().equals(typeParcelable.toString() + "[]");
    }

    boolean isParcelableArrayList(TypeMirror typeMirror) {
        if (isArrayList(typeMirror)) {
            String type = typeMirror.toString();
            if (type.contains("<") && type.contains(">")) {
                String generic = type.substring(type.indexOf("<") + 1, type.indexOf(">"));
                TypeMirror genericType = elementUtils.getTypeElement(generic).asType();
                return types.isSubtype(genericType, typeParcelable);
            }
        }
        return false;
    }

    boolean isSerializable(TypeMirror typeMirror) {
        return !isBaseType(typeMirror) && !isArrayList(typeMirror) && !isBaseArrayType(typeMirror) &&
                !isString(typeMirror) && !isStringArray(typeMirror) && isSerializableImpl(typeMirror);
    }

    boolean isSerializableImpl(TypeMirror typeMirror) {
        return types.isSubtype(typeMirror, typeSerializable);
    }

    boolean isIntegerArrayList(TypeMirror typeMirror) {
        return typeMirror.toString().equals(ArrayList.class.getCanonicalName() + "<" + Integer.class.getCanonicalName() + ">");
    }

    boolean isStringArrayList(TypeMirror typeMirror) {
        return typeMirror.toString().equals(ArrayList.class.getCanonicalName() + "<" + String.class.getCanonicalName() + ">");
    }

    boolean isStringArray(TypeMirror typeMirror) {
        return typeMirror.toString().equals(String[].class.getCanonicalName());
    }

    boolean isString(TypeMirror typeMirror) {
        return typeMirror.toString().equals(String.class.getCanonicalName());
    }

    boolean isBool(TypeMirror typeMirror) {
        return typeMirror.toString().equals(boolean.class.getCanonicalName()) ||
                typeMirror.toString().equals(Boolean.class.getCanonicalName());
    }

    boolean isInt(TypeMirror typeMirror) {
        return typeMirror.toString().equals(int.class.getCanonicalName()) ||
                typeMirror.toString().equals(Integer.class.getCanonicalName());
    }

    boolean isByte(TypeMirror typeMirror) {
        return typeMirror.toString().equals(byte.class.getCanonicalName()) ||
                typeMirror.toString().equals(Byte.class.getCanonicalName());
    }

    boolean isShort(TypeMirror typeMirror) {
        return typeMirror.toString().equals(short.class.getCanonicalName()) ||
                typeMirror.toString().equals(Short.class.getCanonicalName());
    }

    boolean isLong(TypeMirror typeMirror) {
        return typeMirror.toString().equals(long.class.getCanonicalName()) ||
                typeMirror.toString().equals(Long.class.getCanonicalName());
    }

    boolean isFloat(TypeMirror typeMirror) {
        return typeMirror.toString().equals(float.class.getCanonicalName()) ||
                typeMirror.toString().equals(Float.class.getCanonicalName());
    }

    boolean isDouble(TypeMirror typeMirror) {
        return typeMirror.toString().equals(double.class.getCanonicalName()) ||
                typeMirror.toString().equals(Double.class.getCanonicalName());
    }

    boolean isIntArray(TypeMirror typeMirror) {
        return typeMirror.toString().equals(int[].class.getCanonicalName()) ||
                typeMirror.toString().equals(Integer[].class.getCanonicalName());
    }

    boolean isBoolArray(TypeMirror typeMirror) {
        return typeMirror.toString().equals(boolean[].class.getCanonicalName()) ||
                typeMirror.toString().equals(Boolean[].class.getCanonicalName());
    }

    boolean isByteArray(TypeMirror typeMirror) {
        return typeMirror.toString().equals(byte[].class.getCanonicalName()) ||
                typeMirror.toString().equals(Byte[].class.getCanonicalName());
    }

    boolean isShortArray(TypeMirror typeMirror) {
        return typeMirror.toString().equals(short[].class.getCanonicalName()) ||
                typeMirror.toString().equals(Short[].class.getCanonicalName());
    }

    boolean isLongArray(TypeMirror typeMirror) {
        return typeMirror.toString().equals(long[].class.getCanonicalName()) ||
                typeMirror.toString().equals(Long[].class.getCanonicalName());
    }

    boolean isDoubleArray(TypeMirror typeMirror) {
        return typeMirror.toString().equals(double[].class.getCanonicalName()) ||
                typeMirror.toString().equals(Double[].class.getCanonicalName());
    }

    boolean isFloatArray(TypeMirror typeMirror) {
        return typeMirror.toString().equals(float[].class.getCanonicalName()) ||
                typeMirror.toString().equals(Float[].class.getCanonicalName());
    }

    boolean isArrayList(TypeMirror typeMirror) {
        return types.erasure(typeMirror).toString().equals(ArrayList.class.getCanonicalName());
    }
}
