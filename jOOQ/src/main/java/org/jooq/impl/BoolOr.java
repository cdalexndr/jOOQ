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
 * The <code>BOOL OR</code> statement.
 */
@SuppressWarnings({ "unused" })
final class BoolOr
extends
    AbstractAggregateFunction<Boolean>
implements
    MBoolOr
{

    BoolOr(
        Condition condition
    ) {
        super(
            false,
            N_BOOL_OR,
            BOOLEAN,
            DSL.field(condition)
        );
    }

    // -------------------------------------------------------------------------
    // XXX: QueryPart API
    // -------------------------------------------------------------------------



    private static final Set<SQLDialect> EMULATE  = SQLDialect.supportedBy(CUBRID, DERBY, FIREBIRD, H2, HSQLDB, IGNITE, MARIADB, MYSQL, SQLITE);

    @Override
    final void acceptFunctionName(Context<?> ctx) {
        switch (ctx.family()) {















            default:
                super.acceptFunctionName(ctx);
                break;
        }
    }

    @SuppressWarnings("unchecked")
    final Condition condition() {
        return DSL.condition((Field<Boolean>) getArguments().get(0));
    }

    @Override
    public final void accept(Context<?> ctx) {
        if (EMULATE.contains(ctx.dialect()))
            ctx.visit(DSL.field(fo(DSL.max(DSL.when(condition(), one()).otherwise(zero()))).eq(one())));
        else
            super.accept(ctx);
    }



    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Condition $condition() {
        return DSL.condition((Field<Boolean>) getArguments().get(0));
    }

    @Override
    public final MBoolOr $condition(MCondition newValue) {
        return constructor().apply(newValue);
    }

    public final Function1<? super MCondition, ? extends MBoolOr> constructor() {
        return (a1) -> new BoolOr((Condition) a1);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(
            this,
            $condition(),
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
                $condition()
            ), abort, recurse, accumulate
        );
    }

    // -------------------------------------------------------------------------
    // XXX: The Object API
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object that) {
        if (that instanceof BoolOr) {
            return
                StringUtils.equals($condition(), ((BoolOr) that).$condition())
            ;
        }
        else
            return super.equals(that);
    }
}
