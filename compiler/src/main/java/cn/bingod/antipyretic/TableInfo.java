package cn.bingod.antipyretic;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.lang.model.element.Modifier;

final class TableInfo {

    private List<UriTable> uriTables = new ArrayList<>();

    ClassName className;
    String packageName;

    void addUriTable(UriTable uriTable) {
        if (uriTables != null) uriTables.add(uriTable);
    }

    JavaFile brewJava() {
        if (className != null) {
            TypeSpec.Builder builder = TypeSpec.classBuilder(className.simpleName())
                    .addModifiers(Modifier.PUBLIC).addModifiers(Modifier.FINAL);

            //ClassName componentName = ClassName.get("android.content", "ComponentName");
            ClassName string = ClassName.get("java.lang", "String");
            ClassName hashMap = ClassName.get("java.util", "HashMap");

            //TypeName componentNameMap = ParameterizedTypeName.get(hashMap, string, componentName);
            TypeName componentNameMap = ParameterizedTypeName.get(hashMap, string, string);
            FieldSpec map = FieldSpec.builder(componentNameMap, "map")
                    .initializer("new $T<>()", HashMap.class)
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                    .build();

            MethodSpec getMap = MethodSpec.methodBuilder("get")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(componentNameMap)
                    .addStatement("return map")
                    .build();

            builder.addField(map)
                    .addStaticBlock(createStaticBlock())
                    .addMethod(getMap);
            return JavaFile.builder(packageName, builder.build())
                    .addFileComment("Generated code from Antipyretic. Do not modify!")
                    .build();
        }
        return null;
    }

    private CodeBlock createStaticBlock() {
        CodeBlock.Builder builder = CodeBlock.builder();
        for (UriTable uriTable : uriTables) {
            if (uriTable.isActivity) {
                builder.add("map.put($S, $S);", uriTable.uri, uriTable.className).add("\n");
            } else {

            }
        }
        return builder.build();
    }
}