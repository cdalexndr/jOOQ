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

import static org.jooq.impl.DSL.arrayAgg;
import static org.jooq.impl.Keywords.K_ARRAY;
import static org.jooq.impl.Names.N_ARRAY;
import static org.jooq.impl.Tools.visitSubquery;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.jooq.Context;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Function1;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.impl.QOM.MArrayQuery;
import org.jooq.impl.QOM.MQueryPart;

/**
 * @author Lukas Eder
 */
final class ArraySelect<T> extends AbstractField<T[]> implements MArrayQuery<T> {

    private final Select<? extends Record1<T>> select;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    ArraySelect(Select<? extends Record1<T>> select) {
        super(N_ARRAY, (DataType) select.getSelect().get(0).getDataType().getArrayDataType());

        this.select = select;
    }

    @Override
    public final void accept(Context<?> ctx) {
        switch (ctx.family()) {







            case H2: {
                Table<?> t = select.asTable("t", "c");
                Field<?> c = t.field("c");

                // [#11053] TODO: Move ORDER BY clause from subquery to ARRAY_AGG
                // See https://github.com/jOOQ/jOOQ/issues/11053#issuecomment-735773248
                visitSubquery(ctx, DSL.select(arrayAgg(c)).from(t));
                break;
            }

            default:
                ctx.visit(K_ARRAY).visitSubquery(select);

                break;
        }
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Select<? extends Record1<T>> $select() {
        return select;
    }

    @Override
    public final <R> R traverse(
        R init,
        Predicate<? super R> abort,
        Predicate<? super MQueryPart> recurse,
        BiFunction<? super R, ? super MQueryPart, ? extends R> accumulate
    ) {
        return QOM.traverse(init, abort, recurse, accumulate, this, select);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(this, select, ArraySelect::new, replacement);
    }
}
