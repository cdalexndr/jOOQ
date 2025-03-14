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
 * The <code>REGR SYY</code> statement.
 */
@SuppressWarnings({ "rawtypes", "unused" })
final class RegrSyy
extends
    AbstractAggregateFunction<BigDecimal>
implements
    MRegrSyy
{

    RegrSyy(
        Field<? extends Number> y,
        Field<? extends Number> x
    ) {
        super(
            false,
            N_REGR_SYY,
            NUMERIC,
            nullSafeNotNull(y, INTEGER),
            nullSafeNotNull(x, INTEGER)
        );
    }

    // -------------------------------------------------------------------------
    // XXX: QueryPart API
    // -------------------------------------------------------------------------



    private static final Set<SQLDialect> NO_SUPPORT_NATIVE        = SQLDialect.supportedUntil(CUBRID, DERBY, FIREBIRD, H2, HSQLDB, IGNITE, MARIADB, MYSQL, SQLITE);





    @Override
    public final void accept(Context<?> ctx) {
        if (NO_SUPPORT_NATIVE.contains(ctx.dialect()))
            acceptEmulation(ctx);




        else
            super.accept(ctx);
    }

    @Override
    void acceptFunctionName(Context<?> ctx) {





        super.acceptFunctionName(ctx);
    }

    @SuppressWarnings("unchecked")
    private final void acceptEmulation(Context<?> ctx) {
        Field<? extends Number> x = (Field) getArguments().get(0);
        Field<? extends Number> y = (Field) getArguments().get(1);

        ctx.visit(fo(DSL.regrCount(x, y)).times(fo(DSL.varPop(x(x, y)))));
    }



    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    @Override
    public final Field<? extends Number> $y() {
        return (Field<? extends Number>) getArguments().get(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Field<? extends Number> $x() {
        return (Field<? extends Number>) getArguments().get(1);
    }

    @Override
    public final MRegrSyy $y(MField<? extends Number> newValue) {
        return constructor().apply(newValue, $x());
    }

    @Override
    public final MRegrSyy $x(MField<? extends Number> newValue) {
        return constructor().apply($y(), newValue);
    }

    public final Function2<? super MField<? extends Number>, ? super MField<? extends Number>, ? extends MRegrSyy> constructor() {
        return (a1, a2) -> new RegrSyy((Field<? extends Number>) a1, (Field<? extends Number>) a2);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(
            this,
            $y(),
            $x(),
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
                $y(),
                $x()
            ), abort, recurse, accumulate
        );
    }

    // -------------------------------------------------------------------------
    // XXX: The Object API
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object that) {
        if (that instanceof RegrSyy) {
            return
                StringUtils.equals($y(), ((RegrSyy) that).$y()) &&
                StringUtils.equals($x(), ((RegrSyy) that).$x())
            ;
        }
        else
            return super.equals(that);
    }
}
