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
 * The <code>IN</code> statement.
 */
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
final class In<T>
extends
    AbstractCondition
implements
    MIn<T>
{

    final Field<T>                     arg1;
    final Select<? extends Record1<T>> arg2;

    In(
        Field<T> arg1,
        Select<? extends Record1<T>> arg2
    ) {

        this.arg1 = nullSafeNotNull(arg1, (DataType) OTHER);
        this.arg2 = arg2;
    }

    // -------------------------------------------------------------------------
    // XXX: QueryPart API
    // -------------------------------------------------------------------------



    @Override
    public final void accept(Context<?> ctx) {







        ScalarSubquery<T> f = new ScalarSubquery<>(arg2, arg1.getDataType());
        Eq.acceptCompareCondition(ctx, this, arg1, org.jooq.Comparator.IN, f, RowN::in, RowN::in, c -> c.visit(arg1).sql(' ').visit(K_IN).sql(' ').visit(f));
    }












    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Field<T> $arg1() {
        return arg1;
    }

    @Override
    public final Select<? extends Record1<T>> $arg2() {
        return arg2;
    }

    @Override
    public final MIn<T> $arg1(MField<T> newValue) {
        return constructor().apply(newValue, $arg2());
    }

    @Override
    public final MIn<T> $arg2(MSelect<? extends Record1<T>> newValue) {
        return constructor().apply($arg1(), newValue);
    }

    @Override
    public final Function2<? super MField<T>, ? super MSelect<? extends Record1<T>>, ? extends MIn<T>> constructor() {
        return (a1, a2) -> new In<>((Field<T>) a1, (Select<? extends Record1<T>>) a2);
    }

    // -------------------------------------------------------------------------
    // XXX: The Object API
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object that) {
        if (that instanceof In) {
            return
                StringUtils.equals($arg1(), ((In) that).$arg1()) &&
                StringUtils.equals($arg2(), ((In) that).$arg2())
            ;
        }
        else
            return super.equals(that);
    }
}
