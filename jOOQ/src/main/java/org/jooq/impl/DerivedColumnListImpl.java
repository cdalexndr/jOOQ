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

import static org.jooq.impl.Tools.EMPTY_NAME;
import static org.jooq.impl.Tools.map;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.jooq.CommonTableExpression;
import org.jooq.Context;
import org.jooq.DerivedColumnList;
import org.jooq.DerivedColumnList1;
import org.jooq.DerivedColumnList10;
import org.jooq.DerivedColumnList11;
import org.jooq.DerivedColumnList12;
import org.jooq.DerivedColumnList13;
import org.jooq.DerivedColumnList14;
import org.jooq.DerivedColumnList15;
import org.jooq.DerivedColumnList16;
import org.jooq.DerivedColumnList17;
import org.jooq.DerivedColumnList18;
import org.jooq.DerivedColumnList19;
import org.jooq.DerivedColumnList2;
import org.jooq.DerivedColumnList20;
import org.jooq.DerivedColumnList21;
import org.jooq.DerivedColumnList22;
import org.jooq.DerivedColumnList3;
import org.jooq.DerivedColumnList4;
import org.jooq.DerivedColumnList5;
import org.jooq.DerivedColumnList6;
import org.jooq.DerivedColumnList7;
import org.jooq.DerivedColumnList8;
import org.jooq.DerivedColumnList9;
import org.jooq.Field;
import org.jooq.Function1;
import org.jooq.Name;
import org.jooq.ResultQuery;
import org.jooq.impl.QOM.MList;
import org.jooq.impl.QOM.MName;
import org.jooq.impl.QOM.MQueryPart;
import org.jooq.impl.QOM.Materialized;

/**
 * @author Lukas Eder
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
final class DerivedColumnListImpl extends AbstractQueryPart
implements


    DerivedColumnList1,
    DerivedColumnList2,
    DerivedColumnList3,
    DerivedColumnList4,
    DerivedColumnList5,
    DerivedColumnList6,
    DerivedColumnList7,
    DerivedColumnList8,
    DerivedColumnList9,
    DerivedColumnList10,
    DerivedColumnList11,
    DerivedColumnList12,
    DerivedColumnList13,
    DerivedColumnList14,
    DerivedColumnList15,
    DerivedColumnList16,
    DerivedColumnList17,
    DerivedColumnList18,
    DerivedColumnList19,
    DerivedColumnList20,
    DerivedColumnList21,
    DerivedColumnList22,



    DerivedColumnList {

    final Name                                                            name;
    final Name[]                                                          fieldNames;
    final BiFunction<? super Field<?>, ? super Integer, ? extends String> fieldNameFunction;

    DerivedColumnListImpl(Name name, Name[] fieldNames) {
        this.name = name;
        this.fieldNames = fieldNames;
        this.fieldNameFunction = null;
    }

    DerivedColumnListImpl(String name, BiFunction<? super Field<?>, ? super Integer, ? extends String> fieldNameFunction) {
        this.name = DSL.name(name);
        this.fieldNames = null;
        this.fieldNameFunction = fieldNameFunction;
    }

    final CommonTableExpression as0(ResultQuery query, Materialized materialized) {
        ResultQuery<?> q = query;

        if (fieldNameFunction != null)
            return new CommonTableExpressionImpl(
                new DerivedColumnListImpl(name, map(
                    q.fields(),
                    (f, i) -> DSL.name(fieldNameFunction.apply(f, i)),
                    Name[]::new
                )),
                q,
                materialized
            );
        else
            return new CommonTableExpressionImpl(this, q, materialized);
    }

    @Override
    public final CommonTableExpression as(ResultQuery query) {
        return as0(query, null);
    }

    @Override
    public final CommonTableExpression asMaterialized(ResultQuery query) {
        return as0(query, Materialized.MATERIALIZED);
    }

    @Override
    public final CommonTableExpression asNotMaterialized(ResultQuery query) {
        return as0(query, Materialized.NOT_MATERIALIZED);
    }

    @Override
    public final void accept(Context<?> ctx) {
        ctx.visit(name);

        if (fieldNames != null && fieldNames.length > 0) {
            ctx.sql('(');

            for (int i = 0; i < fieldNames.length; i++) {
                if (i > 0)
                    ctx.sql(", ");

                ctx.visit(fieldNames[i]);
            }

            ctx.sql(')');
        }
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final MName $tableName() {
        return name;
    }

    @Override
    public final MList<? extends Name> $columnNames() {
        return QueryPartListView.wrap(fieldNames != null ? fieldNames : EMPTY_NAME);
    }

    @Override
    public final <R> R traverse(
        R init,
        Predicate<? super R> abort,
        Predicate<? super MQueryPart> recurse,
        BiFunction<? super R, ? super MQueryPart, ? extends R> accumulate
    ) {
        return QOM.traverse(init, abort, recurse, accumulate, name, fieldNames);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(this, name, fieldNames, DerivedColumnListImpl::new, replacement);
    }
}
