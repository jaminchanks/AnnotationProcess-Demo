package com.rhythm7.lib_compiler;

import com.google.auto.service.AutoService;
import com.rhythm7.lib_annotations.RandomInt;
import com.rhythm7.lib_annotations.RandomString;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by Jaminchanks on 2018-02-10.
 */


@AutoService(Processor.class)
public class RandomProcessor extends AbstractProcessor{

    private static final String RANDOM_SUFFIX = "_Random";
    private static final String TARGET_STATEMENT_FORMAT = "target.%1$s = %2$s";
    private static final String CONST_PARAM_TARGET_NAME = "target";

    private static final char CHAR_DOT = '.';

    private static final List<Class<? extends Annotation>> RANDOM_TYPES = Arrays.asList(RandomInt.class, RandomString.class);

    private Messager messager;
    private Types typesUtil;
    private Elements elementsUtil;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnv.getMessager();
        typesUtil = processingEnv.getTypeUtils();
        elementsUtil = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();

        for (Class<? extends Annotation> annotation : RANDOM_TYPES) {
            annotations.add(annotation.getCanonicalName());
        }
        return annotations;
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, List<AnnotatedRandomElement>> annotatedElementMap = new LinkedHashMap<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(RandomInt.class)) {
            AnnotatedRandomInt randomElement = new AnnotatedRandomInt(element);
            messager.printMessage(Diagnostic.Kind.NOTE, randomElement.toString());
            if (!randomElement.isTypeValid(elementsUtil, typesUtil)) {
                messager.printMessage(Diagnostic.Kind.ERROR, randomElement.getSimpleClassName().toString() + "#"
                        + randomElement.getElementName().toString() + " is not in valid type int");
            }
            addAnnotatedElement(annotatedElementMap, randomElement);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(RandomString.class)) {
            AnnotatedRandomString randomElement = new AnnotatedRandomString(element);
            messager.printMessage(Diagnostic.Kind.NOTE, randomElement.toString());
            if (!randomElement.isTypeValid(elementsUtil, typesUtil)) {
                messager.printMessage(Diagnostic.Kind.ERROR, randomElement.getSimpleClassName().toString() + "#"
                        + element.getSimpleName() + " is not in valid type String");
            }
            addAnnotatedElement(annotatedElementMap, randomElement);
        }

        if (annotatedElementMap.size() == 0) {
            return true;
        }

        try {
            for (Map.Entry<String, List<AnnotatedRandomElement>> entry : annotatedElementMap.entrySet()) {
                MethodSpec constructor = createConstructor(entry.getValue());
                TypeSpec binder = createClass(getClassName(entry.getKey()), constructor);
                JavaFile javaFile = JavaFile.builder(getPackage(entry.getKey()), binder).build();
                javaFile.writeTo(filer);
            }

        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Error on creating java file");
        }

        return true;
    }


    private MethodSpec createConstructor(List<AnnotatedRandomElement> randomElements) {
        AnnotatedRandomElement firstElement = randomElements.get(0);
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(firstElement.getElement().getEnclosingElement().asType()), CONST_PARAM_TARGET_NAME);
        for (int i = 0; i < randomElements.size(); i++) {
            addStatement(builder, randomElements.get(i));

        }
        return builder.build();
    }

    private void addStatement(MethodSpec.Builder builder, AnnotatedRandomElement randomElement) {
        builder.addStatement(String.format(
                TARGET_STATEMENT_FORMAT,
                randomElement.getElementName().toString(),
                randomElement.getRandomValue())
        );
    }

    private TypeSpec createClass(String className, MethodSpec constructor) {
        return TypeSpec.classBuilder(className + RANDOM_SUFFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(constructor)
                .build();
    }

    private String getPackage(String qualifier) {
        return qualifier.substring(0, qualifier.lastIndexOf(CHAR_DOT));
    }

    private String getClassName(String qualifier) {
        return qualifier.substring(qualifier.lastIndexOf(CHAR_DOT) + 1);
    }

    private void addAnnotatedElement(Map<String, List<AnnotatedRandomElement>> map, AnnotatedRandomElement randomElement) {
        String qualifier = randomElement.getQualifiedClassName().toString();
        if (map.get(qualifier) == null) {
            map.put(qualifier, new ArrayList<>());
        }
        map.get(qualifier).add(randomElement);
    }

}
