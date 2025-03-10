/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * ASL 2.0 and offer limited warranties, support, maintenance, and commercial
 * database integrations.
 *
 * For more information, please visit: http://www.jooq.org/licenses
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package org.jooq.codegen;

import static org.jooq.codegen.GenerationUtil.convertToIdentifier;
import static org.jooq.codegen.GenerationUtil.escapeWindowsForbiddenNames;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jooq.Record;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.TableImpl;
import org.jooq.impl.TableRecordImpl;
import org.jooq.impl.UDTRecordImpl;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.meta.AttributeDefinition;
import org.jooq.meta.CatalogDefinition;
import org.jooq.meta.ColumnDefinition;
import org.jooq.meta.Definition;
import org.jooq.meta.ForeignKeyDefinition;
import org.jooq.meta.ParameterDefinition;
import org.jooq.meta.RoutineDefinition;
import org.jooq.meta.SchemaDefinition;
import org.jooq.meta.TableDefinition;
import org.jooq.meta.TypedElementDefinition;
import org.jooq.meta.UDTDefinition;
import org.jooq.tools.StringUtils;

/**
 * A wrapper for generator strategies preventing some common compilation errors
 * resulting from badly generated source code
 *
 * @author Lukas Eder
 */
class GeneratorStrategyWrapper extends AbstractGeneratorStrategy {

    private final Map<Class<?>, Map<Integer, Set<String>>> reservedColumns = new HashMap<>();

    final Generator                                        generator;
    final GeneratorStrategy                                delegate;

    GeneratorStrategyWrapper(Generator generator, GeneratorStrategy delegate) {
        this.generator = generator;
        this.delegate = delegate;
    }

    @Override
    public String getTargetDirectory() {
        return delegate.getTargetDirectory();
    }

    @Override
    public void setTargetDirectory(String directory) {
        delegate.setTargetDirectory(directory);
    }

    @Override
    public String getTargetPackage() {
        return delegate.getTargetPackage();
    }

    @Override
    public void setTargetPackage(String packageName) {
        delegate.setTargetPackage(packageName);
    }

    @Override
    public Locale getTargetLocale() {
        return delegate.getTargetLocale();
    }

    @Override
    public void setTargetLocale(Locale targetLocale) {
        delegate.setTargetLocale(targetLocale);
    }

    @Override
    public Language getTargetLanguage() {
        return delegate.getTargetLanguage();
    }

    @Override
    public void setTargetLanguage(Language targetLanguage) {
        delegate.setTargetLanguage(targetLanguage);
    }

    @Override
    public void setInstanceFields(boolean instanceFields) {
        delegate.setInstanceFields(instanceFields);
    }

    @Override
    public boolean getInstanceFields() {
        return delegate.getInstanceFields();
    }

    @Override
    public void setJavaBeansGettersAndSetters(boolean javaBeansGettersAndSetters) {
        delegate.setJavaBeansGettersAndSetters(javaBeansGettersAndSetters);
    }

    @Override
    public boolean getJavaBeansGettersAndSetters() {
        return delegate.getJavaBeansGettersAndSetters();
    }

    @Override
    public String getGlobalReferencesFileHeader(Definition container, Class<? extends Definition> objectType) {
        return delegate.getGlobalReferencesFileHeader(container, objectType);
    }

    @Override
    public String getFileHeader(Definition definition, Mode mode) {
        return delegate.getFileHeader(definition, mode);
    }

    @Override
    public String getJavaIdentifier(Definition definition) {
        String identifier = getFixedJavaIdentifier(definition);

        if (identifier != null)
            return identifier;

        identifier = convertToIdentifier(delegate.getJavaIdentifier(definition), getTargetLanguage());

        // [#1212] Don't trust custom strategies and disambiguate identifiers here
        if (definition instanceof ColumnDefinition ||
            definition instanceof AttributeDefinition) {

            TypedElementDefinition<?> e = (TypedElementDefinition<?>) definition;

            if (identifier.equals(getJavaIdentifier(e.getContainer())))
                return identifier + "_";

            // [#2781] Disambiguate collisions with the leading package name
            if (identifier.equals(getJavaPackageName(e.getContainer()).replaceAll("\\..*", "")))
                return identifier + "_";
        }

        else if (definition instanceof TableDefinition) {
            SchemaDefinition schema = definition.getSchema();

            if (identifier.equals(getJavaIdentifier(schema)))
                return identifier + "_";
        }

        // [#5557] Once more, this causes issues...
        else if (definition instanceof SchemaDefinition) {
            CatalogDefinition catalog = definition.getCatalog();

            if (identifier.equals(getJavaIdentifier(catalog)))
                return identifier + "_";
        }

        identifier = overload(definition, Mode.DEFAULT, identifier);
        return identifier;
    }

    @Override
    public String getJavaSetterName(Definition definition, Mode mode) {
        return fixMethodName(definition, mode, delegate.getJavaSetterName(definition, mode));
    }

    @Override
    public String getJavaGetterName(Definition definition, Mode mode) {
        return fixMethodName(definition, mode, delegate.getJavaGetterName(definition, mode));
    }

    @Override
    public String getJavaMethodName(Definition definition, Mode mode) {
        return fixMethodName(definition, mode, delegate.getJavaMethodName(definition, mode));
    }

    private String fixMethodName(Definition definition, Mode mode, String methodName) {
        methodName = overload(definition, mode, methodName);
        methodName = convertToIdentifier(methodName, getTargetLanguage());

        return disambiguateMethod(definition, methodName);
    }

    /**
     * [#1358] Add an overload suffix if needed
     */
    private String overload(Definition definition, Mode mode, String identifier) {
        if (!StringUtils.isBlank(definition.getOverload()))
            identifier += getOverloadSuffix(definition, mode, definition.getOverload());

        return identifier;
    }

    /**
     * [#182] Method name disambiguation is important to avoid name clashes due
     * to pre-existing getters / setters in super classes
     */
    private String disambiguateMethod(Definition definition, String method) {
        Set<String> reserved = null;

        if (definition instanceof AttributeDefinition) {
            reserved = reservedColumns(UDTRecordImpl.class, 0);
        }
        else if (definition instanceof ColumnDefinition) {
            if (((ColumnDefinition) definition).getContainer().getPrimaryKey() != null)
                reserved = reservedColumns(UpdatableRecordImpl.class, 0);
            else
                reserved = reservedColumns(TableRecordImpl.class, 0);
        }

        // [#1406] Disambiguate also procedure parameters
        else if (definition instanceof ParameterDefinition) {
            reserved = reservedColumns(AbstractRoutine.class, 0);
        }

        // [#9150] Member procedures and functions can collide with UDTRecord methods
        else if (definition instanceof RoutineDefinition) {
            RoutineDefinition routine = (RoutineDefinition) definition;

            if (routine.getPackage() instanceof UDTDefinition
                    && routine.getInParameters().size() > 0
                    && "SELF".equalsIgnoreCase(routine.getInParameters().get(0).getName()))
                reserved = reservedColumns(UDTRecordImpl.class, routine.getInParameters().size() - 1);
        }

        // [#11032] Foreign keys produce implicit join methods that can collide with TableImpl methods
        else if (definition instanceof ForeignKeyDefinition) {
            reserved = reservedColumns(TableImpl.class, 0);
        }

        if (reserved != null) {
            if (reserved.contains(method))
                return method + "_";

            // If this is the setter, check if the getter needed disambiguation
            // This ensures that getters and setters have the same name
            if (method.startsWith("set")) {
                String base = method.substring(3);

                if (reserved.contains("get" + base) || reserved.contains("is" + base))
                    return method + "_";
            }
        }

        return method;
    }

    /**
     * [#182] Find all column names that are reserved because of the extended
     * class hierarchy of a generated class
     */
    private Set<String> reservedColumns(Class<?> clazz, int length) {
        if (clazz == null)
            return Collections.emptySet();

        Map<Integer, Set<String>> map = reservedColumns.computeIfAbsent(clazz, k -> new HashMap<>());

        Set<String> result = map.get(length);
        if (result == null) {
            result = new HashSet<>();
            map.put(length, result);

            // Recurse up in class hierarchy
            result.addAll(reservedColumns(clazz.getSuperclass(), length));
            for (Class<?> c : clazz.getInterfaces())
                result.addAll(reservedColumns(c, length));

            for (Method m : clazz.getDeclaredMethods())
                if (m.getParameterTypes().length == length)
                    result.add(m.getName());

            // [#5457] In Scala, we must not "override" any inherited members, even if they're private
            //         or package private, and thus not visible
            if (getTargetLanguage() == Language.SCALA)
                for (Field f : clazz.getDeclaredFields())
                    result.add(f.getName());
        }

        return result;
    }

    @Override
    public String getGlobalReferencesJavaClassExtends(Definition container, Class<? extends Definition> objectType) {
        return delegate.getGlobalReferencesJavaClassExtends(container, objectType);
    }

    @Override
    public String getJavaClassExtends(Definition definition, Mode mode) {

        // [#1243] Only POJO mode can accept super classes
        return delegate.getJavaClassExtends(definition, mode);
    }

    @Override
    public List<String> getGlobalReferencesJavaClassImplements(Definition container, Class<? extends Definition> objectType) {
        return delegate.getGlobalReferencesJavaClassImplements(container, objectType);
    }

    @Override
    public List<String> getJavaClassImplements(Definition definition, Mode mode) {

        // [#1243] All generation modes can accept interfaces
        Set<String> result = new LinkedHashSet<>(delegate.getJavaClassImplements(definition, mode));

        // [#1528] [#7210] Generated interfaces (implemented by RECORD and POJO) are
        //                 Serializable by default
        if (mode == Mode.INTERFACE
                && generator.generateSerializableInterfaces())
            result.add(Serializable.class.getName());

        // [#1528] [#4888] POJOs only implement Serializable by default if they don't inherit
        //                 Serializable from INTERFACE already
        else if (mode == Mode.POJO
                && generator.generateSerializablePojos()
                && (!generator.generateInterfaces() || !generator.generateSerializableInterfaces()))
            result.add(Serializable.class.getName());

        return new ArrayList<>(result);
    }

    @Override
    public String getGlobalReferencesJavaClassName(Definition container, Class<? extends Definition> objectType) {
        return fixJavaClassName(delegate.getGlobalReferencesJavaClassName(container, objectType));
    }

    @Override
    public String getJavaClassName(Definition definition, Mode mode) {
        String name = getFixedJavaClassName(definition);
        if (name != null)
            return name;

        // [#1150] Intercept Mode.RECORD calls for tables
        if (definition instanceof TableDefinition && !generator.generateRecords() && mode == Mode.RECORD)
            return Record.class.getSimpleName();

        String className;

        className = delegate.getJavaClassName(definition, mode);
        className = overload(definition, mode, className);

        return fixJavaClassName(className);
    }

    private String fixJavaClassName(String className) {
        className = convertToIdentifier(className, getTargetLanguage());
        className = escapeWindowsForbiddenNames(className);

        return className;
    }

    @Override
    public String getGlobalReferencesJavaPackageName(Definition container, Class<? extends Definition> objectType) {
        return fixJavaPackageName(delegate.getGlobalReferencesJavaPackageName(container, objectType));
    }

    @Override
    public String getJavaPackageName(Definition definition, Mode mode) {

        // [#1150] Intercept Mode.RECORD calls for tables
        if (!generator.generateRecords() && mode == Mode.RECORD && definition instanceof TableDefinition)
            return Record.class.getPackage().getName();

        return fixJavaPackageName(delegate.getJavaPackageName(definition, mode));
    }

    private String fixJavaPackageName(String packageName) {
        String[] split = packageName.split("\\.");

        for (int i = 0; i < split.length; i++) {
            split[i] = convertToIdentifier(split[i], getTargetLanguage());
            split[i] = escapeWindowsForbiddenNames(split[i]);
        }

        return StringUtils
            .join(split, ".")
            // [#4168] In JDK 9, _ is no longer allowed as an identifier
            .replaceAll("\\._?\\.", ".");
    }

    @Override
    public String getJavaMemberName(Definition definition, Mode mode) {
        String identifier = convertToIdentifier(delegate.getJavaMemberName(definition, mode), getTargetLanguage());

        // [#2781] Disambiguate collisions with the leading package name
        if (identifier.equals(getJavaPackageName(definition, mode).replaceAll("\\..*", ""))) {
            return identifier + "_";
        }

        return identifier;
    }

    @Override
    public String getOverloadSuffix(Definition definition, Mode mode, String overloadIndex) {
        return delegate.getOverloadSuffix(definition, mode, overloadIndex);
    }
}
