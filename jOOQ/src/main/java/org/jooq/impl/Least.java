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

import static org.jooq.impl.DSL.function;
import static org.jooq.impl.Names.N_LEAST;
import static org.jooq.impl.Names.N_MIN;
import static org.jooq.impl.Names.N_MINVALUE;
import static org.jooq.impl.Tools.EMPTY_FIELD;

import org.jooq.Context;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Function1;
import org.jooq.impl.QOM.MField;
import org.jooq.impl.QOM.MLeast;
import org.jooq.impl.QOM.MList;

/**
 * @author Lukas Eder
 */
final class Least<T> extends AbstractField<T> implements MLeast<T> {

    private final QueryPartListView<? extends Field<?>> args;

    Least(Field<?>... args) {
        super(N_LEAST, (DataType<T>) Tools.nullSafeDataType(args[0]));

        this.args = QueryPartListView.wrap(args);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void accept(Context<?> ctx) {

        // In any dialect, a single argument is always the least
        if (args.size() == 1) {
            ctx.visit(args.get(0));
            return;
        }

        switch (ctx.family()) {
            // This implementation has O(2^n) complexity. Better implementations
            // are very welcome







            case DERBY: {
                Field<T> first = (Field<T>) args.get(0);
                Field<T> other = (Field<T>) args.get(1);

                if (args.size() > 2) {
                    Field<?>[] remaining = new Field<?>[args.size() - 2];
                    System.arraycopy(args, 2, remaining, 0, remaining.length);

                    ctx.visit(DSL
                       .when(first.lt(other), DSL.least(first, remaining))
                       .otherwise(DSL.least(other, remaining)));
                }
                else
                    ctx.visit(DSL
                       .when(first.lt(other), first)
                       .otherwise(other));

                return;
            }

            case FIREBIRD:
                ctx.visit(function(N_MINVALUE, getDataType(), args.toArray(EMPTY_FIELD)));
                return;

            case SQLITE:
                ctx.visit(function(N_MIN, getDataType(), args.toArray(EMPTY_FIELD)));
                return;

            default:
                ctx.visit(function(N_LEAST, getDataType(), args.toArray(EMPTY_FIELD)));
                return;
        }
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final MList<? extends Field<T>> $arg1() {
        return (MList<? extends Field<T>>) args;
    }

    @Override
    public final Function1<? super MList<? extends MField<T>>, ? extends MField<T>> constructor() {
        return a -> new Least<>(a.toArray(EMPTY_FIELD));
    }
}
