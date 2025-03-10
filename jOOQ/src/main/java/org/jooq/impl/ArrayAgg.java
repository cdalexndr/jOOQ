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

import static org.jooq.impl.Names.N_ARRAY_AGG;

import org.jooq.Context;
import org.jooq.Field;
import org.jooq.Function1;
import org.jooq.impl.QOM.MAggregateFunction;
import org.jooq.impl.QOM.MArrayAgg;
import org.jooq.impl.QOM.MField;

/**
 * @author Lukas Eder
 */
final class ArrayAgg<T> extends AbstractAggregateFunction<T[]> implements MArrayAgg<T> {

    ArrayAgg(boolean distinct, Field<T> arg) {
        super(distinct, N_ARRAY_AGG, arg.getDataType().getArrayDataType(), arg);
    }

    @Override
    public final void accept(Context<?> ctx) {
        switch (ctx.family()) {









            default:
                ctx.visit(N_ARRAY_AGG).sql('(');
                acceptArguments1(ctx, new QueryPartListView<>(arguments.get(0)));
                acceptOrderBy(ctx);
                ctx.sql(')');
                break;
        }

        acceptFilterClause(ctx);
        acceptOverClause(ctx);
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    @Override
    public final MField<T> $arg1() {
        return (MField<T>) getArguments().get(0);
    }

    @Override
    public final Function1<? super MField<T>, ? extends MAggregateFunction<T[]>> constructor() {
        return f -> new ArrayAgg<>(distinct, (Field<T>) f);
    }
}
