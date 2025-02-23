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
package org.jooq.impl;

import static org.jooq.impl.DSL.*;
import static org.jooq.impl.Internal.*;
import static org.jooq.impl.Keywords.*;
import static org.jooq.impl.Names.*;
import static org.jooq.impl.SQLDataType.*;
import static org.jooq.impl.Tools.*;
import static org.jooq.impl.Tools.BooleanDataKey.*;
import static org.jooq.impl.Tools.DataExtendedKey.*;
import static org.jooq.impl.Tools.DataKey.*;
import static org.jooq.SQLDialect.*;

import org.jooq.*;
import org.jooq.Function1;
import org.jooq.Record;
import org.jooq.conf.*;
import org.jooq.impl.*;
import org.jooq.impl.QOM.*;
import org.jooq.tools.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;


/**
 * The <code>TO CHAR</code> statement.
 */
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
final class ToChar
extends
    AbstractField<String>
implements
    MToChar
{

    final Field<?>      value;
    final Field<String> formatMask;

    ToChar(
        Field<?> value
    ) {
        super(
            N_TO_CHAR,
            allNotNull(VARCHAR, value)
        );

        this.value = nullSafeNotNull(value, OTHER);
        this.formatMask = null;
    }

    ToChar(
        Field<?> value,
        Field<String> formatMask
    ) {
        super(
            N_TO_CHAR,
            allNotNull(VARCHAR, value, formatMask)
        );

        this.value = nullSafeNotNull(value, OTHER);
        this.formatMask = nullSafeNotNull(formatMask, VARCHAR);
    }

    // -------------------------------------------------------------------------
    // XXX: QueryPart API
    // -------------------------------------------------------------------------



    private static final Set<SQLDialect> NO_SUPPORT_NATIVE_WITHOUT_MASK = SQLDialect.supportedBy(CUBRID, DERBY, FIREBIRD, HSQLDB, IGNITE, MARIADB, MYSQL, POSTGRES, SQLITE, YUGABYTE);
    private static final Set<SQLDialect> NO_SUPPORT_NATIVE_WITH_MASK    = SQLDialect.supportedBy(CUBRID, DERBY, FIREBIRD, HSQLDB, IGNITE, MARIADB, MYSQL, SQLITE);

    @Override
    public final void accept(Context<?> ctx) {
        if (formatMask == null && NO_SUPPORT_NATIVE_WITHOUT_MASK.contains(ctx.dialect()))
            acceptCast(ctx);
        else if (formatMask != null && NO_SUPPORT_NATIVE_WITH_MASK.contains(ctx.dialect()))
            acceptCast(ctx);




        else
            acceptNative(ctx);
    }

    private final void acceptNative(Context<?> ctx) {
        ctx.visit(N_TO_CHAR).sql('(').visit(value);

        if (formatMask != null)
            ctx.sql(", ").visit(formatMask);

        ctx.sql(')');
    }

    private final void acceptCast(Context<?> ctx) {
        ctx.visit(DSL.cast(value, VARCHAR));
    }
















    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Field<?> $value() {
        return value;
    }

    @Override
    public final Field<String> $formatMask() {
        return formatMask;
    }

    @Override
    public final MToChar $value(MField<?> newValue) {
        return constructor().apply(newValue, $formatMask());
    }

    @Override
    public final MToChar $formatMask(MField<String> newValue) {
        return constructor().apply($value(), newValue);
    }

    public final Function2<? super MField<?>, ? super MField<String>, ? extends MToChar> constructor() {
        return (a1, a2) -> new ToChar((Field<?>) a1, (Field<String>) a2);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(
            this,
            $value(),
            $formatMask(),
            constructor()::apply,
            replacement
        );
    }

    @Override
    public final <R> R traverse(
        R init,
        Predicate<? super R> abort,
        Predicate<? super MQueryPart> recurse,
        BiFunction<? super R, ? super MQueryPart, ? extends R> accumulate
    ) {
        return QOM.traverse(
            init, abort, recurse, accumulate, this,
            $value(),
            $formatMask()
        );
    }

    // -------------------------------------------------------------------------
    // XXX: The Object API
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object that) {
        if (that instanceof ToChar) {
            return
                StringUtils.equals($value(), ((ToChar) that).$value()) &&
                StringUtils.equals($formatMask(), ((ToChar) that).$formatMask())
            ;
        }
        else
            return super.equals(that);
    }
}
