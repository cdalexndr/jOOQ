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
import java.math.BigDecimal;


/**
 * The <code>VAR POP</code> statement.
 */
@SuppressWarnings({ "rawtypes", "unused" })
final class VarPop
extends
    AbstractAggregateFunction<BigDecimal>
implements
    MVarPop
{

    VarPop(
        Field<? extends Number> field
    ) {
        super(
            false,
            N_VAR_POP,
            NUMERIC,
            nullSafeNotNull(field, INTEGER)
        );
    }

    // -------------------------------------------------------------------------
    // XXX: QueryPart API
    // -------------------------------------------------------------------------



    private static final Set<SQLDialect> NO_SUPPORT_NATIVE = SQLDialect.supportedUntil(DERBY, IGNITE, SQLITE);

    @SuppressWarnings("unchecked")
    @Override
    public void accept(Context<?> ctx) {
        if (NO_SUPPORT_NATIVE.contains(ctx.dialect())) {
            Field<? extends Number> x = (Field) getArguments().get(0);

            ctx.visit(fo(DSL.avg(DSL.square(x).cast(d(ctx)))).minus(DSL.square(fo(DSL.avg(x.cast(d(ctx)))))));
        }
        else
            super.accept(ctx);
    }

    @Override
    void acceptFunctionName(Context<?> ctx) {
        switch (ctx.family()) {












            default:
                super.acceptFunctionName(ctx);
                break;
        }
    }



    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    @Override
    public final Field<? extends Number> $field() {
        return (Field<? extends Number>) getArguments().get(0);
    }

    @Override
    public final MVarPop $field(MField<? extends Number> newValue) {
        return constructor().apply(newValue);
    }

    public final Function1<? super MField<? extends Number>, ? extends MVarPop> constructor() {
        return (a1) -> new VarPop((Field<? extends Number>) a1);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(
            this,
            $field(),
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
        return super.traverse(
            QOM.traverse(
                init, abort, recurse, accumulate, this,
                $field()
            ), abort, recurse, accumulate
        );
    }

    // -------------------------------------------------------------------------
    // XXX: The Object API
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object that) {
        if (that instanceof VarPop) {
            return
                StringUtils.equals($field(), ((VarPop) that).$field())
            ;
        }
        else
            return super.equals(that);
    }
}
