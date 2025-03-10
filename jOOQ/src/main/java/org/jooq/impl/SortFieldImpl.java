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

// ...
// ...
// ...
// ...
import static org.jooq.SQLDialect.CUBRID;
// ...
// ...
// ...
import static org.jooq.SQLDialect.MARIADB;
// ...
import static org.jooq.SQLDialect.MYSQL;
// ...
// ...
import static org.jooq.SQLDialect.SQLITE;
// ...
// ...
// ...
import static org.jooq.impl.DSL.nvl2;
import static org.jooq.impl.DSL.one;
import static org.jooq.impl.DSL.zero;
import static org.jooq.impl.Keywords.K_NULLS_FIRST;
import static org.jooq.impl.Keywords.K_NULLS_LAST;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.jooq.Context;
import org.jooq.Field;
import org.jooq.Function1;
// ...
import org.jooq.SQLDialect;
import org.jooq.SortField;
import org.jooq.SortOrder;
import org.jooq.impl.QOM.MQueryPart;
import org.jooq.impl.QOM.NullOrdering;


final class SortFieldImpl<T> extends AbstractQueryPart implements SortField<T>, SimpleQueryPart {

    // DB2 supports NULLS FIRST/LAST only in OLAP (window) functions
    private static final Set<SQLDialect> NO_SUPPORT_NULLS = SQLDialect.supportedUntil(CUBRID, MARIADB, MYSQL);

    final Field<T>                       field;
    final SortOrder                      order;
    NullOrdering                         nullOrdering;

    SortFieldImpl(Field<T> field, SortOrder order) {
        this.field = field;
        this.order = order;
    }

    @Override
    public boolean isSimple() {
        return nullOrdering == null && Tools.isSimple(field);
    }

    @Override
    public final String getName() {
        return field.getName();
    }

    @Override
    public final SortOrder getOrder() {
        return order;
    }

    final Field<T> getField() {
        return field;
    }

    @SuppressWarnings("unchecked")
    final <U> SortField<U> transform(Field<U> newField) {
        if (newField == field)
            return (SortFieldImpl<U>) this;

        SortField<U> r = newField.sort(order);
        return nullOrdering == NullOrdering.NULLS_FIRST
             ? r.nullsFirst()
             : nullOrdering == NullOrdering.NULLS_LAST
             ? r.nullsLast()
             : r;
    }

    @Override
    public final SortField<T> nullsFirst() {
        nullOrdering = NullOrdering.NULLS_FIRST;
        return this;
    }

    @Override
    public final SortField<T> nullsLast() {
        nullOrdering = NullOrdering.NULLS_LAST;
        return this;
    }

    @Override
    public final void accept(Context<?> ctx) {







        if (nullOrdering != null) {
            if (NO_SUPPORT_NULLS.contains(ctx.dialect())) {
                Field<Integer> ifNull = nullOrdering == NullOrdering.NULLS_FIRST ? zero() : one();
                Field<Integer> ifNotNull = nullOrdering == NullOrdering.NULLS_FIRST ? one() : zero();

                ctx.visit(nvl2(field, ifNotNull, ifNull))
                   .sql(", ");

                acceptFieldAndOrder(ctx, false);
            }
            else
                acceptFieldAndOrder(ctx, true);
        }
        else
            acceptFieldAndOrder(ctx, false);
    }


















    private final void acceptFieldAndOrder(Context<?> ctx, boolean includeNulls) {
        String separator = "";

        for (Field<?> f : Tools.flatten(field)) {
            ctx.sql(separator).visit(f);

            if (order != SortOrder.DEFAULT)
               ctx.sql(' ')
                  .visit(order.toKeyword());

            if (includeNulls)
                if (nullOrdering == NullOrdering.NULLS_FIRST)
                    ctx.sql(' ').visit(K_NULLS_FIRST);
                else
                    ctx.sql(' ').visit(K_NULLS_LAST);

            separator = ", ";
        }
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Field<T> $field() {
        return field;
    }

    @Override
    public final SortOrder $sortOrder() {
        return order;
    }

    @Override
    public final NullOrdering $nullOrdering() {
        return nullOrdering;
    }

    @Override
    public final <R> R traverse(
        R init,
        Predicate<? super R> abort,
        Predicate<? super MQueryPart> recurse,
        BiFunction<? super R, ? super MQueryPart, ? extends R> accumulate
    ) {
        return QOM.traverse(init, abort, recurse, accumulate, this, field);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(
            this,
            field, order, nullOrdering,
            (f, o, n) -> {
                SortFieldImpl<T> r = new SortFieldImpl<>(f, o);
                r.nullOrdering = n;
                return r;
            },
            replacement
        );
    }
}
