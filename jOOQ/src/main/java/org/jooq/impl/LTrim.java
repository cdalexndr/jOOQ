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
 * The <code>LTRIM</code> statement.
 */
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
final class Ltrim
extends
    AbstractField<String>
implements
    MLtrim
{

    final Field<String> string;
    final Field<String> characters;

    Ltrim(
        Field<String> string
    ) {
        super(
            N_LTRIM,
            allNotNull(VARCHAR, string)
        );

        this.string = nullSafeNotNull(string, VARCHAR);
        this.characters = null;
    }

    Ltrim(
        Field<String> string,
        Field<String> characters
    ) {
        super(
            N_LTRIM,
            allNotNull(VARCHAR, string, characters)
        );

        this.string = nullSafeNotNull(string, VARCHAR);
        this.characters = nullSafeNotNull(characters, VARCHAR);
    }

    // -------------------------------------------------------------------------
    // XXX: QueryPart API
    // -------------------------------------------------------------------------



    @Override
    public final void accept(Context<?> ctx) {







        if (characters == null) {
            switch (ctx.family()) {
                case FIREBIRD:
                    ctx.visit(N_TRIM).sql('(').visit(K_LEADING).sql(' ').visit(K_FROM).sql(' ').visit(string).sql(')');
                    break;

                default:
                    ctx.visit(function(N_LTRIM, getDataType(), string));
                    break;
            }
        }
        else {
            switch (ctx.family()) {



                case SQLITE:
                    ctx.visit(function(N_LTRIM, getDataType(), string, characters));
                    break;

                default:
                    ctx.visit(N_TRIM).sql('(').visit(K_LEADING).sql(' ').visit(characters).sql(' ').visit(K_FROM).sql(' ').visit(string).sql(')');
                    break;
            }
        }
    }
















    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Field<String> $string() {
        return string;
    }

    @Override
    public final Field<String> $characters() {
        return characters;
    }

    @Override
    public final MLtrim $string(MField<String> newValue) {
        return constructor().apply(newValue, $characters());
    }

    @Override
    public final MLtrim $characters(MField<String> newValue) {
        return constructor().apply($string(), newValue);
    }

    public final Function2<? super MField<String>, ? super MField<String>, ? extends MLtrim> constructor() {
        return (a1, a2) -> new Ltrim((Field<String>) a1, (Field<String>) a2);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(
            this,
            $string(),
            $characters(),
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
            $characters()
        );
    }

    // -------------------------------------------------------------------------
    // XXX: The Object API
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object that) {
        if (that instanceof Ltrim) {
            return
                StringUtils.equals($string(), ((Ltrim) that).$string()) &&
                StringUtils.equals($characters(), ((Ltrim) that).$characters())
            ;
        }
        else
            return super.equals(that);
    }
}
