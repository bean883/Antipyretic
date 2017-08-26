package cn.bingod.antipyretic;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

final class BindingSet {

    private List<FieldBinder> fields = new ArrayList<>();
    private TypeName targetTypeName;
    private ClassName binderClassName;
    private boolean isActivity;

    private TypeUtils typeUtils;

    BindingSet(TypeUtils typeUtils, TypeName targetTypeName, ClassName bindingClassName, boolean isActivity) {
        this.typeUtils = typeUtils;
        this.targetTypeName = targetTypeName;
        this.binderClassName = bindingClassName;
        this.isActivity = isActivity;
    }

    void addField(FieldBinder fieldBinder) {
        if (fields != null) fields.add(fieldBinder);
    }

    JavaFile brewJava() {
        TypeName targetType = TypeVariableName.get("T");
        TypeSpec.Builder builder = TypeSpec.classBuilder(binderClassName.simpleName() + AnnotationProcessor.Protocols.SUFFIX)
                .addModifiers(Modifier.PUBLIC);

        builder.addTypeVariable(TypeVariableName.get("T", targetTypeName))
                .addMethod(createInjectMethod(targetType));

        return JavaFile.builder(binderClassName.packageName(), builder.build())
                .addFileComment("Generated code from Antipyretic. Do not modify!")
                .build();
    }

    private MethodSpec createInjectMethod(TypeName targetType) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(targetType, "target");

        for (FieldBinder field : fields) {
            if (AnnotationProcessor.Types.Extra == field.paramType) {
                CodeBlock.Builder codebuilder = CodeBlock.builder();
                String code = buildStatement(field.typeMirror, false);
                if (typeUtils.isSerializable(field.typeMirror)) {
                    if (isActivity) {
                        codebuilder.add("if(target.getIntent().getBundleExtra($S) != null)\n", AnnotationProcessor.Protocols.BUNDLE)
                                .add("target.$L = ", field.name)
                                .add("($T) target.getIntent().getBundleExtra($S).", field.typeMirror, AnnotationProcessor.Protocols.BUNDLE)
                                .add(code, "extra_" + field.paramKey).build();
                    } else {
                        codebuilder.add("target.$L = ", field.name)
                                .add("($T) target.getArguments().getBundle($S).", field.typeMirror, AnnotationProcessor.Protocols.BUNDLE)
                                .add(code, "extra_" + field.paramKey);
                    }
                } else {
                    if (isActivity) {
                        codebuilder.add("if(target.getIntent().getBundleExtra($S) != null)\n", AnnotationProcessor.Protocols.BUNDLE)
                                .add("target.$L = ", field.name)
                                .add("target.getIntent().getBundleExtra($S).", AnnotationProcessor.Protocols.BUNDLE)
                                .add(code, "extra_" + field.paramKey).build();
                    } else {
                        codebuilder.add("target.$L = ", field.name)
                                .add("target.getArguments().getBundle($S).", AnnotationProcessor.Protocols.BUNDLE)
                                .add(code, "extra_" + field.paramKey);
                    }
                }
                builder.addCode(codebuilder.build());
            } else if (AnnotationProcessor.Types.Query == field.paramType) {
                if (typeUtils.isString(field.typeMirror)) {
                    if (isActivity) {
                        CodeBlock codeBlock = CodeBlock.builder()
                                .add("if(target.getIntent().getData() != null)\n")
                                .add("target.$L = ", field.name)
                                .add("target.getIntent().getData().getQueryParameter($S);", field.paramKey)
                                .build();
                        builder.addCode(codeBlock);
                    } else {
                        CodeBlock codeBlock = CodeBlock.builder()
                                .add("if(target.getArguments().getParcelable($S) != null)\n", AnnotationProcessor.Protocols.URI)
                                .add("target.$L = ", field.name)
                                .add("(($T) target.getArguments().getParcelable($S)).getQueryParameter($S);", typeUtils.getUriType(), AnnotationProcessor.Protocols.URI, field.paramKey)
                                .build();
                        builder.addCode(codeBlock);
                    }
                } else {
                    throw new IllegalArgumentException(field.name + " must be a String type");
                }
            } else if (AnnotationProcessor.Types.Path == field.paramType) {
                if (isActivity) {
                    CodeBlock codeBlock = CodeBlock.builder()
                            .add("target.$L = ", field.name)
                            .add("target.getIntent().getStringExtra($S);", "path_" + field.paramKey).build();
                    builder.addCode(codeBlock);
                } else {
                    CodeBlock codeBlock = CodeBlock.builder()
                            .add("if(target.getArguments() != null)\n")
                            .add("target.$L = ", field.name)
                            .add("target.getArguments().", AnnotationProcessor.Protocols.BUNDLE)
                            .add("getString($S);", "extra_" + field.paramKey).build();
                    builder.addCode(codeBlock);
                }
            }
            builder.addCode("\n");
        }
        return builder.build();
    }

    private String buildStatement(TypeMirror typeMirror, boolean isExtra) {
        StringBuilder statement = new StringBuilder();
        if (typeUtils.isInt(typeMirror)) {
            statement.append(isExtra ? "getIntExtra($S, 0);" : "getInt($S, 0);");
        } else if (typeUtils.isBool(typeMirror)) {
            statement.append(isExtra ? "getBooleanExtra($S, false);" : "getBoolean($S, false);");
        } else if (typeUtils.isShort(typeMirror)) {
            statement.append(isExtra ? "getShortExtra($S, (short)0);" : "getShort($S, (short)0);");
        } else if (typeUtils.isByte(typeMirror)) {
            statement.append(isExtra ? "getByteExtra($S, (byte)0);" : "getByte($S, (byte)0);");
        } else if (typeUtils.isBaseType(typeMirror)) {
            statement.append("get").append(getType(typeMirror)).append(isExtra ? "Extra($S, 0);" : "($S, 0);");
        } else if (typeUtils.isIntArray(typeMirror)) {
            statement.append(isExtra ? "getIntArrayExtra($S);" : "getIntArray($S);");
        } else if (typeUtils.isByteArray(typeMirror)) {
            statement.append(isExtra ? "getByteArrayExtra($S);" : "getByteArray($S);");
        } else if (typeUtils.isShortArray(typeMirror)) {
            statement.append(isExtra ? "getShortArrayExtra($S);" : "getShortArray($S);");
        } else if (typeUtils.isLongArray(typeMirror)) {
            statement.append(isExtra ? "getLongArrayExtra($S);" : "getLongArray($S);");
        } else if (typeUtils.isFloatArray(typeMirror)) {
            statement.append(isExtra ? "getFloatArrayExtra($S);" : "getFloatArray($S);");
        } else if (typeUtils.isDoubleArray(typeMirror)) {
            statement.append(isExtra ? "getDoubleArrayExtra($S);" : "getDoubleArray($S);");
        } else if (typeUtils.isBoolArray(typeMirror)) {
            statement.append(isExtra ? "getBooleanArrayExtra($S);" : "getBooleanArray($S);");
        } else if (typeUtils.isIntegerArrayList(typeMirror)) {
            statement.append(isExtra ? "getIntegerArrayListExtra($S);" : "getIntegerArrayList($S);");
        } else if (typeUtils.isStringArrayList(typeMirror)) {
            statement.append(isExtra ? "getStringArrayListExtra($S);" : "getStringArrayList($S);");
        } else if (typeUtils.isString(typeMirror)) {
            statement.append(isExtra ? "getStringExtra($S);" : "getString($S);");
        } else if (typeUtils.isStringArray(typeMirror)) {
            statement.append(isExtra ? "getStringArrayExtra($S);" : "getStringArray($S);");
        } else if (typeUtils.isCharSequence(typeMirror)) {
            statement.append(isExtra ? "getCharSequenceExtra($S);" : "getCharSequence($S);");
        } else if (typeUtils.isCharSequenceArray(typeMirror)) {
            statement.append(isExtra ? "getCharSequenceArrayExtra($S);" : "getCharSequenceArray($S);");
        } else if (typeUtils.isCharSequenceArrayList(typeMirror)) {
            statement.append(isExtra ? "getCharSequenceArrayListExtra($S);" : "getCharSequenceArrayList($S);");
        } else if (typeUtils.isParcelable(typeMirror)) {
            statement.append(isExtra ? "getParcelableExtra($S);" : "getParcelable($S);");
        } else if (typeUtils.isParcelableArray(typeMirror)) {
            statement.append(isExtra ? "getParcelableArrayExtra($S);" : "getParcelableArray($S);");
        } else if (typeUtils.isParcelableArrayList(typeMirror)) {
            statement.append(isExtra ? "getParcelableArrayListExtra($S);" : "getParcelableArrayList($S);");
        } else if (typeUtils.isParcelableImpl(typeMirror)) {
            statement.append(isExtra ? "getParcelableExtra($S);" : "getParcelable($S);");
        } else if (typeUtils.isSerializableImpl(typeMirror)) {
            statement.append(isExtra ? "getSerializableExtra($S);" : "getSerializable($S);");
        }
        return statement.toString();
    }

    private String getType(TypeMirror typeMirror) {
        String[] strs = typeMirror.toString().split("\\.");
        if (strs.length > 0) {
            String str = strs[strs.length - 1];
            String firstChar = str.substring(0, 1);
            return str.replace(firstChar, firstChar.toUpperCase());
        }
        return "";
    }
}