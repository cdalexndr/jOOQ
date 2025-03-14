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

import static org.jooq.impl.DSL.choose;
import static org.jooq.impl.DSL.function;
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.Names.N_CHOOSE;
import static org.jooq.impl.Tools.EMPTY_FIELD;

import org.jooq.CaseValueStep;
import org.jooq.CaseWhenStep;
import org.jooq.Context;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Function2;
import org.jooq.impl.QOM.MChoose;
import org.jooq.impl.QOM.MField;
import org.jooq.impl.QOM.MList;

/**
 * @author Lukas Eder
 */
final class Choose<T> extends AbstractField<T> implements MChoose<T> {

    private Field<Integer> index;
    private Field<T>[]     values;

    Choose(Field<Integer> index, Field<T>[] values) {
        super(N_CHOOSE, dataType(values));

        this.index = index;
        this.values = values;
    }

    @SuppressWarnings("unchecked")
    private static final <T> DataType<T> dataType(Field<T>[] values) {
        return values == null || values.length == 0 ? (DataType<T>) SQLDataType.OTHER : values[0].getDataType();
    }

    @Override
    public final void accept(Context<?> ctx) {
        switch (ctx.family()) {






            default: {
                CaseValueStep<Integer> s = choose(index);
                CaseWhenStep<Integer, T> when = null;

                for (int i = 0; i < values.length; i++) {
                    when = when == null
                        ? s.when(inline(i + 1), values[i])
                        : when.when(inline(i + 1), values[i]);
                }

                ctx.visit(when);
                break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Field<Integer> $arg1() {
        return index;
    }

    @Override
    public final MList<? extends Field<T>> $arg2() {
        return QueryPartListView.wrap(values);
    }

    @Override
    public final Function2<? super MField<Integer>, ? super MList<? extends MField<T>>, ? extends MField<T>> constructor() {
        return (i, v) -> new Choose<T>((Field<Integer>) i, (Field<T>[]) v.toArray(EMPTY_FIELD));
    }
}
