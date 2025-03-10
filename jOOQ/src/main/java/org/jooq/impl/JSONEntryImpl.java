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

import static java.lang.Boolean.TRUE;
// ...
import static org.jooq.SQLDialect.MARIADB;
// ...
import static org.jooq.SQLDialect.MYSQL;
// ...
import static org.jooq.impl.DSL.NULL;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.function;
import static org.jooq.impl.DSL.iif;
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.inlined;
import static org.jooq.impl.DSL.toChar;
import static org.jooq.impl.Keywords.K_FORMAT;
import static org.jooq.impl.Keywords.K_JSON;
import static org.jooq.impl.Keywords.K_KEY;
import static org.jooq.impl.Keywords.K_VALUE;
import static org.jooq.impl.Names.N_JSON;
import static org.jooq.impl.Names.N_JSON_MERGE;
import static org.jooq.impl.Names.N_JSON_MERGE_PRESERVE;
import static org.jooq.impl.Names.N_JSON_QUERY;
import static org.jooq.impl.SQLDataType.VARCHAR;
import static org.jooq.impl.Tools.combine;
import static org.jooq.impl.Tools.emulateMultiset;
import static org.jooq.impl.Tools.isScalarSubquery;
import static org.jooq.impl.Tools.BooleanDataKey.DATA_MULTISET_CONTENT;

import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.Context;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Function1;
import org.jooq.JSONEntry;
import org.jooq.JSONEntryValueStep;
import org.jooq.Param;
// ...
import org.jooq.Record1;
import org.jooq.SQLDialect;
import org.jooq.Scope;
import org.jooq.Select;
import org.jooq.conf.NestedCollectionEmulation;
import org.jooq.impl.QOM.MField;
import org.jooq.impl.QOM.MQueryPart;

import org.jetbrains.annotations.NotNull;


/**
 * The JSON object entry.
 *
 * @author Lukas Eder
 */
final class JSONEntryImpl<T> extends AbstractQueryPart implements JSONEntry<T>, JSONEntryValueStep {

    static final Set<SQLDialect> REQUIRE_JSON_MERGE          = SQLDialect.supportedBy(MARIADB, MYSQL);
    static final Set<SQLDialect> SUPPORT_JSON_MERGE_PRESERVE = SQLDialect.supportedBy(MARIADB, MYSQL);

    private final Field<String>  key;
    private final Field<T>       value;

    JSONEntryImpl(Field<String> key) {
        this(key, null);
    }

    JSONEntryImpl(Field<String> key, Field<T> value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public final Field<String> key() {
        return key;
    }

    @Override
    public final Field<T> value() {
        return value;
    }

    @Override
    public final <X> JSONEntry<X> value(X newValue) {
        return value(Tools.field(newValue));
    }

    @Override
    public final <X> JSONEntry<X> value(Field<X> newValue) {
        return new JSONEntryImpl<>(key, newValue);
    }

    @Override
    public final <X> JSONEntry<X> value(Select<? extends Record1<X>> newValue) {
        return value(DSL.field(newValue));
    }

    @Override
    public void accept(Context<?> ctx) {
        switch (ctx.family()) {



















            case MARIADB:
            case MYSQL:
            case POSTGRES:
            case SQLITE:
            case YUGABYTE:
                ctx.visit(key).sql(", ").visit(jsonCast(ctx, value));
                break;

            default:
                ctx.visit(K_KEY).sql(' ').visit(key).sql(' ').visit(K_VALUE).sql(' ').visit(jsonCast(ctx, value));
                break;
        }
    }

    static final Function<? super Field<?>, ? extends Field<?>> jsonCastMapper(final Context<?> ctx) {
        return field -> jsonCast(ctx, field);
    }

    @SuppressWarnings("unchecked")
    static final Field<?> jsonCast(Context<?> ctx, Field<?> field) {
        DataType<?> type = field.getDataType();

        switch (ctx.family()) {

            // [#10769] [#12141] Some dialects don't support auto conversions from X to JSON
            case H2:
                if (type.getType() == UUID.class)
                    return field.cast(VARCHAR(36));
                else if (type.isTemporal())
                    return field.cast(VARCHAR);

                break;

            // [#11025] These don't have boolean support outside of JSON
            case MARIADB:
            case MYSQL:
                if (type.getType() == Boolean.class)
                    return inlined(field);

                break;

            case SQLITE:
                if (type.getType() == Boolean.class)
                    return function(N_JSON, SQLDataType.JSON, iif(condition((Field<Boolean>) field), inline("true"), inline("false")));

                break;





















            case POSTGRES:
            case YUGABYTE:
                if (field instanceof Param)
                    if (field.getType() != Object.class)
                        return field.cast(field.getDataType());
                    else
                        return field.cast(VARCHAR);
                else
                    return field;
        }

        return field;
    }

    static final <T> Field<T> unescapeNestedJSON(Context<?> ctx, Field<T> value) {

        // [#12086] Avoid escaping nested JSON
        return
            JSONEntryImpl.isJSON(ctx, value.getDataType())
          ? DSL.function(N_JSON_QUERY, value.getDataType(), value)
          : value;
    }

    static final boolean isJSON(Scope scope, DataType<?> t) {
        if (t instanceof ConvertedDataType)
            t = ((ConvertedDataType<?, ?>) t).delegate();

        return t.isJSON()
            || t.isEmbeddable() && (TRUE.equals(scope.data(DATA_MULTISET_CONTENT)) && emulateMultisetWithJSON(scope))
            || t.isRecord() && (TRUE.equals(scope.data(DATA_MULTISET_CONTENT)) && emulateMultisetWithJSON(scope))
            || t.isMultiset() && emulateMultisetWithJSON(scope);
    }

    private static final boolean emulateMultisetWithJSON(Scope scope) {
        return emulateMultiset(scope.configuration()) == NestedCollectionEmulation.JSON
            || emulateMultiset(scope.configuration()) == NestedCollectionEmulation.JSONB;
    }

    static final Field<?> booleanValAsVarchar(Field<?> field) {
        return field instanceof Val ? ((Val<?>) field).convertTo0(VARCHAR) : field;
    }

    static final Field<?> jsonMerge(Scope scope, String empty, Field<?>... fields) {
        return function(
            SUPPORT_JSON_MERGE_PRESERVE.contains(scope.dialect()) ? N_JSON_MERGE_PRESERVE : N_JSON_MERGE,
            fields[0].getDataType(),
            combine(inline(empty), fields)
        );
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final MField<String> $key() {
        return key;
    }

    @Override
    public final MField<?> $value() {
        return value;
    }

    @Override
    public final <R> R traverse(
        R init,
        Predicate<? super R> abort,
        Predicate<? super MQueryPart> recurse,
        BiFunction<? super R, ? super MQueryPart, ? extends R> accumulate
    ) {
        return QOM.traverse(init, abort, recurse, accumulate, this, key, value);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(this, key, value, JSONEntryImpl::new, replacement);
    }
}
