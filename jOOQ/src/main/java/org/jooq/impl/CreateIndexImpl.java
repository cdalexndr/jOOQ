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
 * The <code>CREATE INDEX</code> statement.
 */
@SuppressWarnings({ "hiding", "rawtypes", "unused" })
final class CreateIndexImpl
extends
    AbstractDDLQuery
implements
    MCreateIndex,
    CreateIndexStep,
    CreateIndexIncludeStep,
    CreateIndexWhereStep,
    CreateIndexFinalStep
{

    final Boolean                                    unique;
    final Index                                      index;
    final boolean                                    ifNotExists;
          Table<?>                                   table;
          QueryPartListView<? extends OrderField<?>> on;
          QueryPartListView<? extends Field<?>>      include;
          Condition                                  where;
          boolean                                    excludeNullKeys;

    CreateIndexImpl(
        Configuration configuration,
        Boolean unique,
        Index index,
        boolean ifNotExists
    ) {
        this(
            configuration,
            unique,
            index,
            ifNotExists,
            null,
            null,
            null,
            null,
            false
        );
    }

    CreateIndexImpl(
        Configuration configuration,
        Boolean unique,
        boolean ifNotExists
    ) {
        this(
            configuration,
            unique,
            null,
            ifNotExists
        );
    }

    CreateIndexImpl(
        Configuration configuration,
        Boolean unique,
        Index index,
        boolean ifNotExists,
        Table<?> table,
        Collection<? extends OrderField<?>> on,
        Collection<? extends Field<?>> include,
        Condition where,
        boolean excludeNullKeys
    ) {
        super(configuration);

        this.unique = unique;
        this.index = index;
        this.ifNotExists = ifNotExists;
        this.table = table;
        this.on = new QueryPartList<>(on);
        this.include = new QueryPartList<>(include);
        this.where = where;
        this.excludeNullKeys = excludeNullKeys;
    }

    // -------------------------------------------------------------------------
    // XXX: DSL API
    // -------------------------------------------------------------------------

    @Override
    public final CreateIndexImpl on(String table, String... on) {
        return on(DSL.table(DSL.name(table)), Tools.fieldsByName(on));
    }

    @Override
    public final CreateIndexImpl on(Name table, Name... on) {
        return on(DSL.table(table), Tools.fieldsByName(on));
    }

    @Override
    public final CreateIndexImpl on(Table<?> table, OrderField<?>... on) {
        return on(table, Arrays.asList(on));
    }

    @Override
    public final CreateIndexImpl on(String table, Collection<? extends String> on) {
        return on(DSL.table(DSL.name(table)), Tools.fieldsByName(on.toArray(EMPTY_STRING)));
    }

    @Override
    public final CreateIndexImpl on(Name table, Collection<? extends Name> on) {
        return on(DSL.table(table), Tools.fieldsByName(on.toArray(EMPTY_NAME)));
    }

    @Override
    public final CreateIndexImpl on(Table<?> table, Collection<? extends OrderField<?>> on) {
        this.table = table;
        this.on = new QueryPartList<>(on);
        return this;
    }

    @Override
    public final CreateIndexImpl include(String... include) {
        return include(Tools.fieldsByName(include));
    }

    @Override
    public final CreateIndexImpl include(Name... include) {
        return include(Tools.fieldsByName(include));
    }

    @Override
    public final CreateIndexImpl include(Field<?>... include) {
        return include(Arrays.asList(include));
    }

    @Override
    public final CreateIndexImpl include(Collection<? extends Field<?>> include) {
        this.include = new QueryPartList<>(include);
        return this;
    }

    @Override
    public final CreateIndexImpl where(Field<Boolean> where) {
        return where(DSL.condition(where));
    }

    @Override
    public final CreateIndexImpl where(Condition... where) {
        return where(DSL.condition(Operator.AND, where));
    }

    @Override
    public final CreateIndexImpl where(Collection<? extends Condition> where) {
        return where(DSL.condition(Operator.AND, where));
    }

    @Override
    public final CreateIndexImpl where(Condition where) {
        this.where = where;
        return this;
    }

    @Override
    public final CreateIndexImpl where(String where, QueryPart... parts) {
        return where(DSL.condition(where, parts));
    }

    @Override
    public final CreateIndexImpl where(String where, Object... bindings) {
        return where(DSL.condition(where, bindings));
    }

    @Override
    public final CreateIndexImpl where(String where) {
        return where(DSL.condition(where));
    }

    @Override
    public final CreateIndexImpl where(SQL where) {
        return where(DSL.condition(where));
    }

    @Override
    public final CreateIndexImpl excludeNullKeys() {
        this.excludeNullKeys = true;
        return this;
    }

    // -------------------------------------------------------------------------
    // XXX: QueryPart API
    // -------------------------------------------------------------------------



    private static final Clause[]        CLAUSES                  = { Clause.CREATE_INDEX };
    private static final Set<SQLDialect> NO_SUPPORT_IF_NOT_EXISTS = SQLDialect.supportedBy(DERBY, FIREBIRD);
    private static final Set<SQLDialect> SUPPORT_UNNAMED_INDEX    = SQLDialect.supportedBy(POSTGRES, YUGABYTE);
    private static final Set<SQLDialect> SUPPORT_INCLUDE          = SQLDialect.supportedBy(POSTGRES, YUGABYTE);
    private static final Set<SQLDialect> SUPPORT_UNIQUE_INCLUDE   = SQLDialect.supportedBy(POSTGRES, YUGABYTE);

    private final boolean supportsIfNotExists(Context<?> ctx) {
        return !NO_SUPPORT_IF_NOT_EXISTS.contains(ctx.dialect());
    }

    @Override
    public final void accept(Context<?> ctx) {
        if (ifNotExists && !supportsIfNotExists(ctx))
            tryCatch(ctx, DDLStatementType.CREATE_INDEX, c -> accept0(c));
        else
            accept0(ctx);
    }

    private final void accept0(Context<?> ctx) {
        ctx.visit(K_CREATE);

        if (unique)
            ctx.sql(' ')
               .visit(K_UNIQUE);

        ctx.sql(' ')
           .visit(K_INDEX)
           .sql(' ');

        if (ifNotExists && supportsIfNotExists(ctx))
            ctx.visit(K_IF_NOT_EXISTS)
               .sql(' ');

        if (index != null)
            ctx.visit(index)
               .sql(' ');
        else if (!SUPPORT_UNNAMED_INDEX.contains(ctx.dialect()))
            ctx.visit(generatedName())
               .sql(' ');

        boolean supportsInclude = unique
            ? SUPPORT_UNIQUE_INCLUDE.contains(ctx.dialect())
            : SUPPORT_INCLUDE.contains(ctx.dialect());
        boolean supportsFieldsBeforeTable = false ;

        QueryPartList<QueryPart> list = new QueryPartList<>().qualify(false);
        list.addAll(on);

        // [#11284] Don't emulate the clause for UNIQUE indexes
        if (!supportsInclude && !unique && include != null)
            list.addAll(include);






        ctx.visit(K_ON)
           .sql(' ')
           .visit(table);




            ctx.sql('(').visit(list).sql(')');

        if (supportsInclude && !include.isEmpty()) {
            Keyword keyword = K_INCLUDE;






            ctx.formatSeparator()
               .visit(keyword)
               .sql(" (")
               .visit(QueryPartCollectionView.wrap(include).qualify(false))
               .sql(')');
        }

        Condition condition;

        if (excludeNullKeys && where == null)
            condition = on.size() == 1
                ? field(Tools.first(on)).isNotNull()
                : row(Tools.fields(on)).isNotNull();
        else
            condition = where;

        if (condition != null && ctx.configuration().data("org.jooq.ddl.ignore-storage-clauses") == null)
            ctx.formatSeparator()
               .visit(K_WHERE)
               .sql(' ')
               .qualify(false, c -> c.visit(condition));





    }

    private final Name generatedName() {
        Name t = table.getQualifiedName();

        StringBuilder sb = new StringBuilder(table.getName());
        for (OrderField<?> f : on)
            sb.append('_').append(Tools.field(f).getName());
        sb.append("_idx");

        if (t.qualified())
            return t.qualifier().append(sb.toString());
        else
            return name(sb.toString());
    }

    @Override
    public final Clause[] clauses(Context<?> ctx) {
        return CLAUSES;
    }



    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Boolean $unique() {
        return unique;
    }

    @Override
    public final Index $index() {
        return index;
    }

    @Override
    public final boolean $ifNotExists() {
        return ifNotExists;
    }

    @Override
    public final Table<?> $table() {
        return table;
    }

    @Override
    public final MList<? extends OrderField<?>> $on() {
        return on;
    }

    @Override
    public final MList<? extends Field<?>> $include() {
        return include;
    }

    @Override
    public final Condition $where() {
        return where;
    }

    @Override
    public final boolean $excludeNullKeys() {
        return excludeNullKeys;
    }

    @Override
    public final MCreateIndex $unique(Boolean newValue) {
        return constructor().apply(newValue, $index(), $ifNotExists(), $table(), $on(), $include(), $where(), $excludeNullKeys());
    }

    @Override
    public final MCreateIndex $index(MIndex newValue) {
        return constructor().apply($unique(), newValue, $ifNotExists(), $table(), $on(), $include(), $where(), $excludeNullKeys());
    }

    @Override
    public final MCreateIndex $ifNotExists(boolean newValue) {
        return constructor().apply($unique(), $index(), newValue, $table(), $on(), $include(), $where(), $excludeNullKeys());
    }

    @Override
    public final MCreateIndex $table(MTable<?> newValue) {
        return constructor().apply($unique(), $index(), $ifNotExists(), newValue, $on(), $include(), $where(), $excludeNullKeys());
    }

    @Override
    public final MCreateIndex $on(MList<? extends OrderField<?>> newValue) {
        return constructor().apply($unique(), $index(), $ifNotExists(), $table(), newValue, $include(), $where(), $excludeNullKeys());
    }

    @Override
    public final MCreateIndex $include(MList<? extends Field<?>> newValue) {
        return constructor().apply($unique(), $index(), $ifNotExists(), $table(), $on(), newValue, $where(), $excludeNullKeys());
    }

    @Override
    public final MCreateIndex $where(MCondition newValue) {
        return constructor().apply($unique(), $index(), $ifNotExists(), $table(), $on(), $include(), newValue, $excludeNullKeys());
    }

    @Override
    public final MCreateIndex $excludeNullKeys(boolean newValue) {
        return constructor().apply($unique(), $index(), $ifNotExists(), $table(), $on(), $include(), $where(), newValue);
    }

    public final Function8<? super Boolean, ? super MIndex, ? super Boolean, ? super MTable<?>, ? super MList<? extends OrderField<?>>, ? super MList<? extends Field<?>>, ? super MCondition, ? super Boolean, ? extends MCreateIndex> constructor() {
        return (a1, a2, a3, a4, a5, a6, a7, a8) -> new CreateIndexImpl(configuration(), a1, (Index) a2, a3, (Table<?>) a4, (Collection<? extends OrderField<?>>) a5, (Collection<? extends Field<?>>) a6, (Condition) a7, a8);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(
            this,
            $unique(),
            $index(),
            $ifNotExists(),
            $table(),
            $on(),
            $include(),
            $where(),
            $excludeNullKeys(),
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
            $index(),
            $table(),
            $on(),
            $include(),
            $where()
        );
    }
}
