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

import static org.jooq.SQLDialect.*;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

/**
 * This type is used for the H2-specific variant of the {@link Merge}'s DSL API.
 * <p>
 * Example: <code><pre>
 * using(configuration)
 *       .mergeInto(table, field1)
 *       .key(id)
 *       .values(field1)
 *       .execute();
 * </pre></code>
 *
 * @author Lukas Eder
 * @deprecated - [#10045] - 3.14.0 - Use the standard SQL MERGE API instead, via {@link DSLContext#mergeInto(Table)}
 */
@Deprecated(forRemoval = true, since = "3.14")
public interface MergeValuesStep1<R extends Record, T1> {

    /**
     * Specify a <code>VALUES</code> clause.
     *
     * @deprecated - [#10045] - 3.14.0 - Use the standard SQL MERGE API instead, via {@link DSLContext#mergeInto(Table)}
     */
    @Deprecated(forRemoval = true, since = "3.14")
    @NotNull @CheckReturnValue
    @Support({ CUBRID, DERBY, FIREBIRD, H2, HSQLDB, MARIADB, MYSQL, POSTGRES, YUGABYTE })
    Merge<R> values(T1 value1);

    /**
     * Specify a <code>VALUES</code> clause.
     *
     * @deprecated - [#10045] - 3.14.0 - Use the standard SQL MERGE API instead, via {@link DSLContext#mergeInto(Table)}
     */
    @Deprecated(forRemoval = true, since = "3.14")
    @NotNull @CheckReturnValue
    @Support({ CUBRID, DERBY, FIREBIRD, H2, HSQLDB, MARIADB, MYSQL, POSTGRES, YUGABYTE })
    Merge<R> values(Field<T1> value1);

    /**
     * Specify a <code>VALUES</code> clause.
     *
     * @deprecated - [#10045] - 3.14.0 - Use the standard SQL MERGE API instead, via {@link DSLContext#mergeInto(Table)}
     */
    @Deprecated(forRemoval = true, since = "3.14")
    @NotNull @CheckReturnValue
    @Support({ CUBRID, DERBY, FIREBIRD, H2, HSQLDB, MARIADB, MYSQL, POSTGRES, YUGABYTE })
    Merge<R> values(Collection<?> values);

    /**
     * Use a <code>SELECT</code> statement as the source of values for the
     * <code>MERGE</code> statement
     * <p>
     * This variant of the <code>MERGE .. SELECT</code> statement expects a
     * select returning exactly as many fields as specified previously in the
     * <code>INTO</code> clause:
     * {@link DSLContext#mergeInto(Table, Field)}.
     *
     * @deprecated - [#10045] - 3.14.0 - Use the standard SQL MERGE API instead, via {@link DSLContext#mergeInto(Table)}
     */
    @Deprecated(forRemoval = true, since = "3.14")
    @NotNull @CheckReturnValue
    @Support({ CUBRID, FIREBIRD, H2, HSQLDB })
    Merge<R> select(Select<? extends Record1<T1>> select);
}
