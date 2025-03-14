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
 * The <code>BIT OR</code> statement.
 */
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
final class BitOr<T extends Number>
extends
    AbstractField<T>
implements
    MBitOr<T>
{

    final Field<T> arg1;
    final Field<T> arg2;

    BitOr(
        Field<T> arg1,
        Field<T> arg2
    ) {
        super(
            N_BIT_OR,
            allNotNull((DataType) dataType(INTEGER, arg1, false), arg1, arg2)
        );

        this.arg1 = nullSafeNotNull(arg1, INTEGER);
        this.arg2 = nullSafeNotNull(arg2, INTEGER);
    }

    // -------------------------------------------------------------------------
    // XXX: QueryPart API
    // -------------------------------------------------------------------------

    @Override
    public final void accept(Context<?> ctx) {
        switch (ctx.family()) {











            case H2:
            case HSQLDB:
                ctx.visit(function(N_BITOR, getDataType(), arg1, arg2));
                break;

            case FIREBIRD:
                ctx.visit(function(N_BIN_OR, getDataType(), arg1, arg2));
                break;







            default:
                ctx.sql('(');
                Expression.<Field<T>, BitOr<T>>acceptAssociative(
                    ctx,
                    this,
                    q -> new Expression.Expr<>(q.arg1, Operators.OP_VERBAR, q.arg2),
                    c -> c.sql(' ')
                );
                ctx.sql(')');
                break;
        }
    }










    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Field<T> $arg1() {
        return arg1;
    }

    @Override
    public final Field<T> $arg2() {
        return arg2;
    }

    @Override
    public final MBitOr<T> $arg1(MField<T> newValue) {
        return constructor().apply(newValue, $arg2());
    }

    @Override
    public final MBitOr<T> $arg2(MField<T> newValue) {
        return constructor().apply($arg1(), newValue);
    }

    @Override
    public final Function2<? super MField<T>, ? super MField<T>, ? extends MBitOr<T>> constructor() {
        return (a1, a2) -> new BitOr<>((Field<T>) a1, (Field<T>) a2);
    }

    // -------------------------------------------------------------------------
    // XXX: The Object API
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object that) {
        if (that instanceof BitOr) {
            return
                StringUtils.equals($arg1(), ((BitOr) that).$arg1()) &&
                StringUtils.equals($arg2(), ((BitOr) that).$arg2())
            ;
        }
        else
            return super.equals(that);
    }
}
