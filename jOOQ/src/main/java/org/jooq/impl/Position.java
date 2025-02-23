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
 * The <code>POSITION</code> statement.
 */
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
final class Position
extends
    AbstractField<Integer>
implements
    MPosition
{

    final Field<String>           in;
    final Field<String>           search;
    final Field<? extends Number> startIndex;

    Position(
        Field<String> in,
        Field<String> search
    ) {
        super(
            N_POSITION,
            allNotNull(INTEGER, in, search)
        );

        this.in = nullSafeNotNull(in, VARCHAR);
        this.search = nullSafeNotNull(search, VARCHAR);
        this.startIndex = null;
    }

    Position(
        Field<String> in,
        Field<String> search,
        Field<? extends Number> startIndex
    ) {
        super(
            N_POSITION,
            allNotNull(INTEGER, in, search, startIndex)
        );

        this.in = nullSafeNotNull(in, VARCHAR);
        this.search = nullSafeNotNull(search, VARCHAR);
        this.startIndex = nullSafeNotNull(startIndex, INTEGER);
    }

    // -------------------------------------------------------------------------
    // XXX: QueryPart API
    // -------------------------------------------------------------------------



    @Override
    public final void accept(Context<?> ctx) {








        if (startIndex != null) {
            switch (ctx.family()) {

                case DERBY:
                case H2:
                    ctx.visit(N_LOCATE).sql('(').visit(search).sql(", ").visit(in).sql(", ").visit(startIndex).sql(')');
                    break;





















                default:
                    ctx.visit(
                        DSL.case_(DSL.position(DSL.substring(in, startIndex), search))
                           .when(inline(0), inline(0))
                           .else_(iadd(DSL.position(DSL.substring(in, startIndex), search), isub(startIndex, one())))
                    );
                    break;
            }
        }
        else {
            switch (ctx.family()) {

                case DERBY:
                    ctx.visit(N_LOCATE).sql('(').visit(search).sql(", ").visit(in).sql(')');
                    break;


















                case SQLITE:
                    ctx.visit(N_INSTR).sql('(').visit(in).sql(", ").visit(search).sql(')');
                    break;

                default:
                    ctx.visit(N_POSITION).sql('(').visit(search).sql(' ').visit(K_IN).sql(' ').visit(in).sql(')');
                    break;
            }
        }
    }

















    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Field<String> $in() {
        return in;
    }

    @Override
    public final Field<String> $search() {
        return search;
    }

    @Override
    public final Field<? extends Number> $startIndex() {
        return startIndex;
    }

    @Override
    public final MPosition $in(MField<String> newValue) {
        return constructor().apply(newValue, $search(), $startIndex());
    }

    @Override
    public final MPosition $search(MField<String> newValue) {
        return constructor().apply($in(), newValue, $startIndex());
    }

    @Override
    public final MPosition $startIndex(MField<? extends Number> newValue) {
        return constructor().apply($in(), $search(), newValue);
    }

    public final Function3<? super MField<String>, ? super MField<String>, ? super MField<? extends Number>, ? extends MPosition> constructor() {
        return (a1, a2, a3) -> new Position((Field<String>) a1, (Field<String>) a2, (Field<? extends Number>) a3);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(
            this,
            $in(),
            $search(),
            $startIndex(),
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
        return QOM.traverse(
            init, abort, recurse, accumulate, this,
            $in(),
            $search(),
            $startIndex()
        );
    }

    // -------------------------------------------------------------------------
    // XXX: The Object API
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object that) {
        if (that instanceof Position) {
            return
                StringUtils.equals($in(), ((Position) that).$in()) &&
                StringUtils.equals($search(), ((Position) that).$search()) &&
                StringUtils.equals($startIndex(), ((Position) that).$startIndex())
            ;
        }
        else
            return super.equals(that);
    }
}
