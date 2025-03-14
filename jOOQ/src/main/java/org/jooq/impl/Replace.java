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
 * The <code>REPLACE</code> statement.
 */
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
final class Replace
extends
    AbstractField<String>
implements
    MReplace
{

    final Field<String> string;
    final Field<String> search;
    final Field<String> replace;

    Replace(
        Field<String> string,
        Field<String> search
    ) {
        super(
            N_REPLACE,
            allNotNull(VARCHAR, string, search)
        );

        this.string = nullSafeNotNull(string, VARCHAR);
        this.search = nullSafeNotNull(search, VARCHAR);
        this.replace = null;
    }

    Replace(
        Field<String> string,
        Field<String> search,
        Field<String> replace
    ) {
        super(
            N_REPLACE,
            allNotNull(VARCHAR, string, search, replace)
        );

        this.string = nullSafeNotNull(string, VARCHAR);
        this.search = nullSafeNotNull(search, VARCHAR);
        this.replace = nullSafeNotNull(replace, VARCHAR);
    }

    // -------------------------------------------------------------------------
    // XXX: QueryPart API
    // -------------------------------------------------------------------------



    @Override
    public final void accept(Context<?> ctx) {

        // [#861] Most dialects don't ship with a two-argument replace function:
        switch (ctx.family()) {
































            case FIREBIRD:
            case HSQLDB:
            case MARIADB:
            case MYSQL:
            case POSTGRES:
            case SQLITE:
            case YUGABYTE:
                if (replace == null)
                    ctx.visit(function(N_REPLACE, VARCHAR, string, search, inline("")));
                else
                    ctx.visit(function(N_REPLACE, VARCHAR, string, search, replace));

                return;

            default:
                if (replace == null)
                    ctx.visit(function(N_REPLACE, VARCHAR, string, search));
                else
                    ctx.visit(function(N_REPLACE, VARCHAR, string, search, replace));

                return;
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
    public final Field<String> $search() {
        return search;
    }

    @Override
    public final Field<String> $replace() {
        return replace;
    }

    @Override
    public final MReplace $string(MField<String> newValue) {
        return constructor().apply(newValue, $search(), $replace());
    }

    @Override
    public final MReplace $search(MField<String> newValue) {
        return constructor().apply($string(), newValue, $replace());
    }

    @Override
    public final MReplace $replace(MField<String> newValue) {
        return constructor().apply($string(), $search(), newValue);
    }

    public final Function3<? super MField<String>, ? super MField<String>, ? super MField<String>, ? extends MReplace> constructor() {
        return (a1, a2, a3) -> new Replace((Field<String>) a1, (Field<String>) a2, (Field<String>) a3);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(
            this,
            $string(),
            $search(),
            $replace(),
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
            $search(),
            $replace()
        );
    }

    // -------------------------------------------------------------------------
    // XXX: The Object API
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object that) {
        if (that instanceof Replace) {
            return
                StringUtils.equals($string(), ((Replace) that).$string()) &&
                StringUtils.equals($search(), ((Replace) that).$search()) &&
                StringUtils.equals($replace(), ((Replace) that).$replace())
            ;
        }
        else
            return super.equals(that);
    }
}
