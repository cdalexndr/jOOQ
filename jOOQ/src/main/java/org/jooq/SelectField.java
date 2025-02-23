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
package org.jooq;

import java.util.function.Function;

import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import org.jetbrains.annotations.NotNull;

/**
 * A <code>QueryPart</code> to be used exclusively in <code>SELECT</code>
 * clauses.
 * <p>
 * Instances of this type cannot be created directly, only of its subtypes.
 *
 * @author Lukas Eder
 */
public interface SelectField<T> extends SelectFieldOrAsterisk, Named, Typed<T> {

    /**
     * Create an alias for this field.
     * <p>
     * Note that the case-sensitivity of the returned field depends on
     * {@link Settings#getRenderQuotedNames()}. By default, field aliases are
     * quoted, and thus case-sensitive in many SQL dialects!
     *
     * @param alias The alias name
     * @return The field alias
     */
    @NotNull
    @Support
    Field<T> as(String alias);

    /**
     * Create an alias for this field.
     * <p>
     * Note that the case-sensitivity of the returned field depends on
     * {@link Settings#getRenderQuotedNames()} and the {@link Name}. By default,
     * field aliases are quoted, and thus case-sensitive in many SQL dialects -
     * use {@link DSL#unquotedName(String...)} for case-insensitive aliases.
     * <p>
     * If the argument {@link Name#getName()} is qualified, then the
     * {@link Name#last()} part will be used.
     *
     * @param alias The alias name
     * @return The field alias
     */
    @NotNull
    @Support
    Field<T> as(Name alias);

    /**
     * Create an alias for this field based on another field's name.
     *
     * @param otherField The other field whose name this field is aliased with.
     * @return The field alias.
     */
    @NotNull
    @Support
    Field<T> as(Field<?> otherField);

}
