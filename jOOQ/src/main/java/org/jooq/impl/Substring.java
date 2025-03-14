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
 * The <code>SUBSTRING</code> statement.
 */
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
final class Substring
extends
    AbstractField<String>
implements
    MSubstring
{

    final Field<String>           string;
    final Field<? extends Number> startingPosition;
    final Field<? extends Number> length;

    Substring(
        Field<String> string,
        Field<? extends Number> startingPosition
    ) {
        super(
            N_SUBSTRING,
            allNotNull(VARCHAR, string, startingPosition)
        );

        this.string = nullSafeNotNull(string, VARCHAR);
        this.startingPosition = nullSafeNotNull(startingPosition, INTEGER);
        this.length = null;
    }

    Substring(
        Field<String> string,
        Field<? extends Number> startingPosition,
        Field<? extends Number> length
    ) {
        super(
            N_SUBSTRING,
            allNotNull(VARCHAR, string, startingPosition, length)
        );

        this.string = nullSafeNotNull(string, VARCHAR);
        this.startingPosition = nullSafeNotNull(startingPosition, INTEGER);
        this.length = nullSafeNotNull(length, INTEGER);
    }

    // -------------------------------------------------------------------------
    // XXX: QueryPart API
    // -------------------------------------------------------------------------



    @Override
    public final void accept(Context<?> ctx) {
        Name functionName = N_SUBSTRING;

        switch (ctx.family()) {






            // [#430] These databases use SQL standard syntax

            case FIREBIRD: {
                if (length == null)
                    ctx.visit(N_SUBSTRING).sql('(').visit(string).sql(' ').visit(K_FROM).sql(' ').visit(startingPosition).sql(')');
                else
                    ctx.visit(N_SUBSTRING).sql('(').visit(string).sql(' ').visit(K_FROM).sql(' ').visit(startingPosition).sql(' ').visit(K_FOR).sql(' ').visit(length).sql(')');

                return;
            }

































            case DERBY:
            case SQLITE:
                functionName = N_SUBSTR;
                break;
        }

        if (length == null)
            ctx.visit(function(functionName, getDataType(), string, startingPosition));
        else
            ctx.visit(function(functionName, getDataType(), string, startingPosition, length));
    }

















    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Field<String> $string() {
        return string;
    }

    @Override
    public final Field<? extends Number> $startingPosition() {
        return startingPosition;
    }

    @Override
    public final Field<? extends Number> $length() {
        return length;
    }

    @Override
    public final MSubstring $string(MField<String> newValue) {
        return constructor().apply(newValue, $startingPosition(), $length());
    }

    @Override
    public final MSubstring $startingPosition(MField<? extends Number> newValue) {
        return constructor().apply($string(), newValue, $length());
    }

    @Override
    public final MSubstring $length(MField<? extends Number> newValue) {
        return constructor().apply($string(), $startingPosition(), newValue);
    }

    public final Function3<? super MField<String>, ? super MField<? extends Number>, ? super MField<? extends Number>, ? extends MSubstring> constructor() {
        return (a1, a2, a3) -> new Substring((Field<String>) a1, (Field<? extends Number>) a2, (Field<? extends Number>) a3);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(
            this,
            $string(),
            $startingPosition(),
            $length(),
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
            $string(),
            $startingPosition(),
            $length()
        );
    }

    // -------------------------------------------------------------------------
    // XXX: The Object API
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object that) {
        if (that instanceof Substring) {
            return
                StringUtils.equals($string(), ((Substring) that).$string()) &&
                StringUtils.equals($startingPosition(), ((Substring) that).$startingPosition()) &&
                StringUtils.equals($length(), ((Substring) that).$length())
            ;
        }
        else
            return super.equals(that);
    }
}
