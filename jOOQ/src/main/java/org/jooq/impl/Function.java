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

import static org.jooq.impl.DSL.unquotedName;
import static org.jooq.impl.Tools.EMPTY_FIELD;
import static org.jooq.impl.Tools.camelCase;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.jooq.Context;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Function1;
import org.jooq.Name;
import org.jooq.impl.QOM.MField;
import org.jooq.impl.QOM.MFunction;
import org.jooq.impl.QOM.MList;
import org.jooq.impl.QOM.MQueryPart;

/**
 * @author Lukas Eder
 */
final class Function<T> extends AbstractField<T> implements MFunction<T> {

    private final QueryPartList<Field<?>> arguments;

    Function(String name, DataType<T> type, Field<?>... arguments) {
        this(unquotedName(name), type, arguments);
    }

    Function(Name name, DataType<T> type, Field<?>... arguments) {
        super(name, type);

        this.arguments = new QueryPartList<>(arguments);
    }

    @Override
    public final void accept(Context<?> ctx) {
        switch (ctx.family()) {





            default:
                ctx.visit(getQualifiedName()).sql('(').visit(arguments).sql(')');
                break;
        }
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final MList<? extends Field<?>> $args() {
        return arguments;
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(
            this,
            $args(),
            a -> new Function<>(getQualifiedName(), getDataType(), a.toArray(EMPTY_FIELD)),
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
        return QOM.traverse(
            init,
            abort,
            recurse,
            accumulate,
            this,
            $args()
        );
    }

    // -------------------------------------------------------------------------
    // The Object API
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object that) {
        if (that instanceof Function)
            return getQualifiedName().equals(((Function<?>) that).getQualifiedName())
                && arguments.equals(((Function<?>) that).arguments);
        else
            return super.equals(that);
    }
}
