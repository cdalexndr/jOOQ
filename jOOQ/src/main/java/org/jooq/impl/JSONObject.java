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
 * The <code>JSON OBJECT</code> statement.
 */
@SuppressWarnings({ "hiding", "rawtypes", "unchecked", "unused" })
final class JSONObject<T>
extends
    AbstractField<T>
implements
    MJSONObject<T>,
    JSONObjectNullStep<T>,
    JSONObjectReturningStep<T>
{

    final DataType<T>                               type;
    final QueryPartListView<? extends JSONEntry<?>> entries;
          JSONOnNull                                onNull;
          DataType<?>                               returning;

    JSONObject(
        DataType<T> type,
        Collection<? extends JSONEntry<?>> entries
    ) {
        this(
            type,
            entries,
            null,
            null
        );
    }

    JSONObject(
        DataType<T> type,
        Collection<? extends JSONEntry<?>> entries,
        JSONOnNull onNull,
        DataType<?> returning
    ) {
        super(
            N_JSON_OBJECT,
            type
        );

        this.type = type;
        this.entries = new QueryPartList<>(entries);
        this.onNull = onNull;
        this.returning = returning;
    }

    // -------------------------------------------------------------------------
    // XXX: DSL API
    // -------------------------------------------------------------------------

    @Override
    public final JSONObject<T> nullOnNull() {
        this.onNull = JSONOnNull.NULL_ON_NULL;
        return this;
    }

    @Override
    public final JSONObject<T> absentOnNull() {
        this.onNull = JSONOnNull.ABSENT_ON_NULL;
        return this;
    }

    @Override
    public final JSONObject<T> returning(DataType<?> returning) {
        this.returning = returning;
        return this;
    }

    // -------------------------------------------------------------------------
    // XXX: QueryPart API
    // -------------------------------------------------------------------------



    @Override
    public final void accept(Context<?> ctx) {
        switch (ctx.family()) {


            case POSTGRES:
            case YUGABYTE:
                if (onNull == JSONOnNull.ABSENT_ON_NULL)
                    ctx.visit(unquotedName(getDataType().getType() == JSONB.class ? "jsonb_strip_nulls" : "json_strip_nulls")).sql('(');

                ctx.visit(unquotedName(getDataType().getType() == JSONB.class ? "jsonb_build_object" : "json_build_object")).sql('(').visit(QueryPartCollectionView.wrap(entries)).sql(')');

                if (onNull == JSONOnNull.ABSENT_ON_NULL)
                    ctx.sql(')');

                break;





































            case MARIADB: {
                JSONEntry<?> first;

                // Workaround for https://jira.mariadb.org/browse/MDEV-13701
                if (entries.size() > 1) {
                    ctx.visit(JSONEntryImpl.jsonMerge(ctx, "{}", Tools.map(entries, e -> jsonObject(e), Field[]::new)));
                }
                else if (!entries.isEmpty() && isJSONArray((first = entries.iterator().next()).value())) {
                    ctx.visit(jsonObject(
                        key(first.key()).value(JSONEntryImpl.jsonMerge(ctx, "[]", first.value()))
                    ));
                }
                else
                    acceptStandard(ctx);

                break;
            }

            default:
                acceptStandard(ctx);
                break;
        }
    }

    private static final boolean isJSONArray(Field<?> field) {
        return field instanceof JSONArray
            || field instanceof JSONArrayAgg
            || field instanceof ScalarSubquery && isJSONArray(((ScalarSubquery<?>) field).query.getSelect().get(0));
    }

    private final void acceptStandard(Context<?> ctx) {
        JSONNull jsonNull;
        JSONReturning jsonReturning = new JSONReturning(returning);

        // Workaround for https://github.com/h2database/h2database/issues/2496
        if (entries.isEmpty() && ctx.family() == H2)
            jsonNull = new JSONNull(JSONOnNull.NULL_ON_NULL);





        else
            jsonNull = new JSONNull(onNull);

        ctx.visit(N_JSON_OBJECT).sql('(').visit(QueryPartListView.wrap(QueryPartCollectionView.wrap(entries), jsonNull, jsonReturning).separator("")).sql(')');
    }


















    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final DataType<T> $type() {
        return type;
    }

    @Override
    public final MList<? extends JSONEntry<?>> $entries() {
        return entries;
    }

    @Override
    public final JSONOnNull $onNull() {
        return onNull;
    }

    @Override
    public final DataType<?> $returning() {
        return returning;
    }

    @Override
    public final MJSONObject<T> $type(MDataType<T> newValue) {
        return constructor().apply(newValue, $entries(), $onNull(), $returning());
    }

    @Override
    public final MJSONObject<T> $entries(MList<? extends JSONEntry<?>> newValue) {
        return constructor().apply($type(), newValue, $onNull(), $returning());
    }

    @Override
    public final MJSONObject<T> $onNull(JSONOnNull newValue) {
        return constructor().apply($type(), $entries(), newValue, $returning());
    }

    @Override
    public final MJSONObject<T> $returning(MDataType<?> newValue) {
        return constructor().apply($type(), $entries(), $onNull(), newValue);
    }

    public final Function4<? super MDataType<T>, ? super MList<? extends JSONEntry<?>>, ? super JSONOnNull, ? super MDataType<?>, ? extends MJSONObject<T>> constructor() {
        return (a1, a2, a3, a4) -> new JSONObject((DataType<T>) a1, (Collection<? extends JSONEntry<?>>) a2, a3, (DataType<?>) a4);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(
            this,
            $type(),
            $entries(),
            $onNull(),
            $returning(),
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
            $type(),
            $entries(),
            $returning()
        );
    }

    // -------------------------------------------------------------------------
    // XXX: The Object API
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object that) {
        if (that instanceof JSONObject) {
            return
                StringUtils.equals($type(), ((JSONObject) that).$type()) &&
                StringUtils.equals($entries(), ((JSONObject) that).$entries()) &&
                StringUtils.equals($onNull(), ((JSONObject) that).$onNull()) &&
                StringUtils.equals($returning(), ((JSONObject) that).$returning())
            ;
        }
        else
            return super.equals(that);
    }
}
