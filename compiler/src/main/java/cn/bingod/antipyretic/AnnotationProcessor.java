package cn.bingod.antipyretic;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {

    private TypeUtils typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Log.init(processingEnv.getMessager());
        typeUtils = new TypeUtils(processingEnv.getTypeUtils(), processingEnv.getElementUtils());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(Param.class.getCanonicalName());
        types.add(Table.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        createTable(roundEnv);
        parseParams(roundEnv);
        return false;
    }

    private void createTable(RoundEnvironment roundEnv) {
        TableInfo tableInfo = new TableInfo();
        String packageName = getClass().getPackage().getName();
        String suffix = null;
        for (Element element : roundEnv.getElementsAnnotatedWith(Modules.class)) {
            Modules modules = element.getAnnotation(Modules.class);
            packageName = modules.value();
            String[] packageArrays = packageName.split("\\.");
            if (packageArrays.length > 0)
                suffix = packageArrays[packageArrays.length - 1];
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(Table.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            parseTables(element, tableInfo, packageName, suffix);
        }
        JavaFile javaFile = tableInfo.brewJava();
        try {
            if (javaFile != null) javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            print(e.getMessage());
        }
    }

    private void parseTables(Element element, TableInfo tableInfo, String packageName, String suffix) {
        if (tableInfo != null) {
            TypeName targetType = TypeName.get(element.asType());
            if (targetType instanceof ClassName) {
                boolean isActivity = typeUtils.isActivity(element.asType());
                ClassName tableClass = (ClassName) targetType;
                Table rt = element.getAnnotation(Table.class);
                ClassName className = ClassName.get(packageName, "RoutingMap" + (suffix == null ? "" : "_"+suffix));
                tableInfo.packageName = className.packageName();
                tableInfo.className = className;
                String[] path = rt.value();
                for (String aPath : path){
                    tableInfo.addUriTable(new UriTable(aPath, tableClass.reflectionName(), isActivity));
                }
            }
        }
    }

    private void parseParams(RoundEnvironment roundEnv) {
        Map<TypeElement, BindingSet> bmap = findAndParseTargets(roundEnv);
        for (Map.Entry<TypeElement, BindingSet> entry : bmap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            BindingSet bindingSet = entry.getValue();
            JavaFile javaFile = bindingSet.brewJava();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                print(String.format("Unable to write injecting for type %s: %s", typeElement, e.getMessage()));
            }
        }
    }

    private Map<TypeElement, BindingSet> findAndParseTargets(RoundEnvironment roundEnv) {
        Map<TypeElement, BindingSet> map = new LinkedHashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Param.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            parseInjectParam(element, map, Param.class);
        }
        return map;
    }

    private void parseInjectParam(Element element, Map<TypeElement, BindingSet> map, Class<? extends Annotation> clazz) {
        if (clazz == Param.class) {
            boolean hasError = isInaccessibleViaGeneratedCode(clazz, "fields", element)
                    || isInjectingInWrongPackage(clazz, element);
            if (hasError) return;
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            BindingSet bindingSet = getBindingSet(map, enclosingElement);
            String paramKey = "";
            Param rp = element.getAnnotation(Param.class);
            if (clazz.equals(Param.class)) {
                paramKey = rp.value();
            }
            String name = element.getSimpleName().toString();
            if (paramKey.length() == 0) {
                paramKey = name;
            }

            FieldBinder fieldBinder = new FieldBinder(name, element.asType(), paramKey, rp.type());
            bindingSet.addField(fieldBinder);
        }
    }

    private BindingSet getBindingSet(Map<TypeElement, BindingSet> map, TypeElement typeElement) {
        BindingSet bindingSet = map.get(typeElement);
        if (bindingSet == null) {
            TypeName targetTypeName = TypeName.get(typeElement.asType());
            ClassName bindingClassName;
            boolean isActivity = typeUtils.isActivity(typeElement.asType());
            if (targetTypeName instanceof ClassName) {
                bindingClassName = (ClassName) targetTypeName;
            } else {
                String packageName = getPackageName(typeElement);
                String className = getClassName(typeElement, packageName);
                bindingClassName = ClassName.get(packageName, className + Protocols.SUFFIX);
            }
            bindingSet = new BindingSet(typeUtils, targetTypeName, bindingClassName, isActivity);
            map.put(typeElement, bindingSet);
        }
        return bindingSet;
    }

    private String getPackageName(TypeElement type) {
        return processingEnv.getElementUtils().getPackageOf(type).getQualifiedName().toString();
    }

    private String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
    }

    private boolean isInaccessibleViaGeneratedCode(Class<? extends Annotation> annotationClass,
                                                   String targetThing, Element element) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify method modifiers.
        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.STATIC)) {
            print(String.format("@%s %s must not be private or static. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName()));
            hasError = true;
        }

        // Verify containing type.
        if (enclosingElement.getKind() != ElementKind.CLASS) {
            print(String.format("@%s %s may only be contained in classes. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName()));
            hasError = true;
        }

        // Verify containing class visibility is not private.
        if (enclosingElement.getModifiers().contains(Modifier.PRIVATE)) {
            print(String.format("@%s %s may not be contained in private classes. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName()));
            hasError = true;
        }
        return hasError;
    }

    private boolean isInjectingInWrongPackage(Class<? extends Annotation> annotationClass,
                                              Element element) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        String qualifiedName = enclosingElement.getQualifiedName().toString();

        if (qualifiedName.startsWith("android.")) {
            print(String.format("@%s-annotated class incorrectly in Android framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName));
            return true;
        }
        if (qualifiedName.startsWith("java.")) {
            print(String.format("@%s-annotated class incorrectly in Java framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName));
            return true;
        }
        return false;
    }

    interface Protocols {
        String BUNDLE = "Antipyretic_Bundle";
        String URI = "Antipyretic_Uri";
        String SUFFIX = "_Binder";
    }

    interface Types {
        int Query = 0;
        int Path = 1;
        int Extra = 2;
    }

    private void print(CharSequence msg) {
        Log.print(msg);
    }
}
