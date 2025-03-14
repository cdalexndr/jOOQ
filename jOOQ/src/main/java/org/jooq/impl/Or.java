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
 * The <code>OR</code> statement.
 */
@SuppressWarnings({ "unused" })
final class Or
extends
    AbstractCondition
implements
    MOr
{

    final Condition arg1;
    final Condition arg2;

    Or(
        Condition arg1,
        Condition arg2
    ) {

        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    // -------------------------------------------------------------------------
    // XXX: QueryPart API
    // -------------------------------------------------------------------------



    private static final Clause[] CLAUSES = { Clause.CONDITION, Clause.CONDITION_OR };

    @Override
    public final void accept(Context<?> ctx) {














        {
            ctx.sqlIndentStart('(');
            Expression.<Condition, Or>acceptAssociative(
                ctx,
                this,
                q -> new Expression.Expr<>(q.arg1, Operator.OR.toKeyword(), q.arg2),
                Context::formatSeparator
            );
            ctx.sqlIndentEnd(')');
        }
    }

    /**
     * @deprecated - This will be implemented using QOM.replace, instead.
     */
    @Deprecated
    final Condition transform(java.util.function.Function<? super Condition, ? extends Condition> function) {
        Condition t1 = arg1 instanceof And
            ? ((And) arg1).transform(function)
            : arg1 instanceof Or
            ? ((And) arg1).transform(function)
            : function.apply(arg1);
        Condition t2 = arg2 instanceof And
            ? ((And) arg2).transform(function)
            : arg2 instanceof Or
            ? ((Or) arg2).transform(function)
            : function.apply(arg2);

        if (t1 == arg1 && t2 == arg2)
            return this;
        else
            return DSL.or(t1, t2);
    }

    @Override
    final boolean isNullable() {
        return ((AbstractCondition) arg1).isNullable() || ((AbstractCondition) arg2).isNullable();
    }

    @Override
    public final Clause[] clauses(Context<?> ctx) {
        return CLAUSES;
    }












    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Condition $arg1() {
        return arg1;
    }

    @Override
    public final Condition $arg2() {
        return arg2;
    }

    @Override
    public final MOr $arg1(MCondition newValue) {
        return constructor().apply(newValue, $arg2());
    }

    @Override
    public final MOr $arg2(MCondition newValue) {
        return constructor().apply($arg1(), newValue);
    }

    @Override
    public final Function2<? super MCondition, ? super MCondition, ? extends MOr> constructor() {
        return (a1, a2) -> new Or((Condition) a1, (Condition) a2);
    }

    // -------------------------------------------------------------------------
    // XXX: The Object API
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object that) {
        if (that instanceof Or) {
            return
                StringUtils.equals($arg1(), ((Or) that).$arg1()) &&
                StringUtils.equals($arg2(), ((Or) that).$arg2())
            ;
        }
        else
            return super.equals(that);
    }
}
