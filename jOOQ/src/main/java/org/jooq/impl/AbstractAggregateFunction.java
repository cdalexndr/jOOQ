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

import static java.util.function.Function.identity;
// ...
// ...
// ...
// ...
import static org.jooq.SQLDialect.H2;
import static org.jooq.SQLDialect.HSQLDB;
// ...
import static org.jooq.SQLDialect.POSTGRES;
// ...
// ...
import static org.jooq.SQLDialect.SQLITE;
import static org.jooq.SQLDialect.YUGABYTE;
import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.one;
import static org.jooq.impl.DSL.zero;
import static org.jooq.impl.Keywords.K_DENSE_RANK;
import static org.jooq.impl.Keywords.K_DISTINCT;
import static org.jooq.impl.Keywords.K_FILTER;
import static org.jooq.impl.Keywords.K_FIRST;
import static org.jooq.impl.Keywords.K_KEEP;
import static org.jooq.impl.Keywords.K_LAST;
import static org.jooq.impl.Keywords.K_NULL;
import static org.jooq.impl.Keywords.K_ORDER_BY;
import static org.jooq.impl.Keywords.K_WHERE;
import static org.jooq.impl.Keywords.K_WITHIN_GROUP;
import static org.jooq.impl.Names.*;
import static org.jooq.impl.QueryPartCollectionView.wrap;
import static org.jooq.impl.SQLDataType.DOUBLE;
import static org.jooq.impl.SQLDataType.NUMERIC;
import static org.jooq.impl.Tools.camelCase;
import static org.jooq.impl.Tools.isEmpty;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.AggregateFilterStep;
import org.jooq.AggregateFunction;
import org.jooq.ArrayAggOrderByStep;
import org.jooq.Condition;
import org.jooq.Context;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.OrderField;
import org.jooq.OrderedAggregateFunction;
// ...
import org.jooq.QueryPart;
import org.jooq.SQL;
import org.jooq.SQLDialect;
import org.jooq.WindowBeforeOverStep;
import org.jooq.impl.QOM.MAggregateFunction;
import org.jooq.impl.QOM.MQueryPart;

/**
 * @author Lukas Eder
 */
abstract class AbstractAggregateFunction<T>
extends AbstractWindowFunction<T>
implements
    AggregateFunction<T>,
    OrderedAggregateFunction<T>,
    ArrayAggOrderByStep<T>,
    MAggregateFunction<T> {




    static final Set<SQLDialect>  SUPPORT_FILTER       = SQLDialect.supportedBy(H2, HSQLDB, POSTGRES, SQLITE, YUGABYTE);
    static final Set<SQLDialect>  SUPPORT_DISTINCT_RVE = SQLDialect.supportedBy(H2, POSTGRES);

    static final Field<Integer>   ASTERISK             = DSL.field("*", Integer.class);

    // Other attributes
    final QueryPartList<Field<?>> arguments;
    final boolean                 distinct;
    Condition                     filter;

    // Other attributes
    SortFieldList                 withinGroupOrderBy;
    SortFieldList                 keepDenseRankOrderBy;
    boolean                       first;


    AbstractAggregateFunction(String name, DataType<T> type, Field<?>... arguments) {
        this(false, name, type, arguments);
    }

    AbstractAggregateFunction(Name name, DataType<T> type, Field<?>... arguments) {
        this(false, name, type, arguments);
    }

    AbstractAggregateFunction(boolean distinct, String name, DataType<T> type, Field<?>... arguments) {
        this(distinct, DSL.unquotedName(name), type, arguments);
    }

    AbstractAggregateFunction(boolean distinct, Name name, DataType<T> type, Field<?>... arguments) {
        super(name, type);

        this.distinct = distinct;
        this.arguments = new QueryPartList<>(arguments);
    }

    // -------------------------------------------------------------------------
    // XXX QueryPart API
    // -------------------------------------------------------------------------

    @Override
    public /* final */ void accept(Context<?> ctx) {
        toSQLArguments(ctx);
        acceptKeepDenseRankOrderByClause(ctx);
        acceptWithinGroupClause(ctx);
        acceptFilterClause(ctx);
        acceptOverClause(ctx);
    }

    /**
     * Render <code>KEEP (DENSE_RANK [FIRST | LAST] ORDER BY {...})</code> clause
     */
    private final void acceptKeepDenseRankOrderByClause(Context<?> ctx) {
        if (!Tools.isEmpty(keepDenseRankOrderBy)) {

            switch (ctx.family()) {






                default:
                    ctx.sql(' ').visit(K_KEEP)
                       .sql(" (").visit(K_DENSE_RANK)
                       .sql(' ').visit(first ? K_FIRST : K_LAST)
                       .sql(' ').visit(K_ORDER_BY)
                       .sql(' ').visit(keepDenseRankOrderBy)
                       .sql(')');
                    break;
            }
        }
    }

    /**
     * Render <code>WITHIN GROUP (ORDER BY ..)</code> clause
     */
    final void acceptWithinGroupClause(Context<?> ctx) {
        if (withinGroupOrderBy != null) {
            switch (ctx.family()) {






                default:
                    ctx.sql(' ').visit(K_WITHIN_GROUP)
                       .sql(" (").visit(K_ORDER_BY).sql(' ');

                    if (withinGroupOrderBy.isEmpty())
                        ctx.visit(K_NULL);
                    else
                        ctx.visit(withinGroupOrderBy);

                    ctx.sql(')');
                    break;
            }
        }
    }

    /**
     * Render function arguments and argument modifiers
     */
    private final void toSQLArguments(Context<?> ctx) {
        acceptFunctionName(ctx);
        ctx.sql('(');
        acceptArguments0(ctx);
        ctx.sql(')');
    }

    /* non-final */ void acceptFunctionName(Context<?> ctx) {



















        ctx.visit(getQualifiedName());
    }

    final void acceptArguments0(Context<?> ctx) {







        acceptArguments1(ctx, arguments);
    }

    final void acceptArguments1(Context<?> ctx, QueryPartCollectionView<Field<?>> args) {
        if (distinct) {
            ctx.visit(K_DISTINCT).sql(' ');

            // [#2883][#9109] PostgreSQL and H2 can use the DISTINCT keyword with formal row value expressions.
            if (args.size() > 1 && SUPPORT_DISTINCT_RVE.contains(ctx.dialect()))
                ctx.sql('(');
        }

        if (!args.isEmpty())
            acceptArguments2(ctx, args);

        if (distinct)
            if (args.size() > 1 && SUPPORT_DISTINCT_RVE.contains(ctx.dialect()))
                ctx.sql(')');
    }

    final void acceptArguments2(Context<?> ctx, QueryPartCollectionView<Field<?>> args) {
        acceptArguments3(ctx, args, identity());
    }

    final void acceptArguments3(Context<?> ctx, QueryPartCollectionView<Field<?>> args, Function<? super Field<?>, ? extends Field<?>> fun) {
        if (filter == null || SUPPORT_FILTER.contains(ctx.dialect()))
            ctx.visit(wrap(args).map(fun));




        else
            ctx.visit(wrap(args).map(arg -> DSL.when(filter, arg == ASTERISK ? one() : arg)).map(fun));
    }














    final void acceptFilterClause(Context<?> ctx) {
        acceptFilterClause(ctx, filter);
    }

    static final void acceptFilterClause(Context<?> ctx, Condition filter) {
        if (filter != null) {
            switch (ctx.family()) {






                default:
                    if (SUPPORT_FILTER.contains(ctx.dialect()))
                        ctx.sql(' ')
                           .visit(K_FILTER)
                           .sql(" (")
                           .visit(K_WHERE)
                           .sql(' ')
                           .visit(filter)
                           .sql(')');
                    break;
            }
        }
    }

    final void acceptOrderBy(Context<?> ctx) {
        if (!Tools.isEmpty(withinGroupOrderBy)) {
            switch (ctx.family()) {






                default:
                    ctx.sql(' ').visit(K_ORDER_BY).sql(' ').visit(withinGroupOrderBy);
                    break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // XXX Aggregate function API
    // -------------------------------------------------------------------------

    final QueryPartList<Field<?>> getArguments() {
        return arguments;
    }

    @Override
    public final WindowBeforeOverStep<T> filterWhere(Condition c) {
        filter = c;
        return this;
    }

    @Override
    public final WindowBeforeOverStep<T> filterWhere(Condition... conditions) {
        return filterWhere(DSL.and(conditions));
    }

    @Override
    public final WindowBeforeOverStep<T> filterWhere(Collection<? extends Condition> conditions) {
        return filterWhere(DSL.and(conditions));
    }

    @Override
    public final WindowBeforeOverStep<T> filterWhere(Field<Boolean> field) {
        return filterWhere(condition(field));
    }

    @Override
    public final WindowBeforeOverStep<T> filterWhere(SQL sql) {
        return filterWhere(condition(sql));
    }

    @Override
    public final WindowBeforeOverStep<T> filterWhere(String sql) {
        return filterWhere(condition(sql));
    }

    @Override
    public final WindowBeforeOverStep<T> filterWhere(String sql, Object... bindings) {
        return filterWhere(condition(sql, bindings));
    }

    @Override
    public final WindowBeforeOverStep<T> filterWhere(String sql, QueryPart... parts) {
        return filterWhere(condition(sql, parts));
    }


    @Override
    public final AggregateFunction<T> withinGroupOrderBy(OrderField<?>... fields) {
        return withinGroupOrderBy(Arrays.asList(fields));
    }

    @Override
    public final AggregateFunction<T> withinGroupOrderBy(Collection<? extends OrderField<?>> fields) {
        if (withinGroupOrderBy == null)
            withinGroupOrderBy = new SortFieldList();

        withinGroupOrderBy.addAll(Tools.sortFields(fields));
        return this;
    }





































    @Override
    public /* non-final */ AbstractAggregateFunction<T> orderBy(OrderField<?>... fields) {
        if (windowSpecification != null)
            super.orderBy(fields);
        else
            withinGroupOrderBy(fields);

        return this;
    }

    @Override
    public /* non-final */ AbstractAggregateFunction<T> orderBy(Collection<? extends OrderField<?>> fields) {
        if (windowSpecification != null)
            windowSpecification.orderBy(fields);
        else
            withinGroupOrderBy(fields);

        return this;
    }

    final Condition f(Condition c) {
        return filter != null ? filter.and(c) : c;
    }

    @SuppressWarnings("unchecked")
    final <U> Field<U> fon(AggregateFunction<U> function) {
        return DSL.nullif(fo(function), (Field<U>) zero());
    }

    /**
     * Apply this aggregate function's <code>ORDER BY</code>,
     * <code>FILTER</code> and <code>OVER</code> clauses to an argument
     * aggregate function.
     */
    final <U> Field<U> ofo(AbstractAggregateFunction<U> function) {
        return fo(isEmpty(withinGroupOrderBy) ? function : function.orderBy(withinGroupOrderBy));
    }

    /**
     * Apply this aggregate function's <code>FILTER</code> and <code>OVER</code>
     * clauses to an argument aggregate function.
     */
    final <U> Field<U> fo(AggregateFunction<U> function) {
        return o(filter != null ? function.filterWhere(filter) : function);
    }

    /**
     * Apply this aggregate function's <code>FILTER</code> and <code>OVER</code>
     * clauses to an argument aggregate function.
     */
    final <U> Field<U> o(WindowBeforeOverStep<U> function) {
        if (windowSpecification != null)
            return function.over(windowSpecification);
        else if (windowDefinition != null)
            return function.over(windowDefinition);
        else if (windowName != null)
            return function.over(windowName);
        else
            return function;
    }

    /**
     * Type safe <code>NVL2(y, x, null)</code> for statistical function
     * emulations.
     */
    final <U extends Number> Field<U> x(Field<U> x, Field<? extends Number> y) {
        return DSL.nvl2(y, x, DSL.NULL(x.getDataType()));
    }

    /**
     * Type safe <code>NVL2(x, y, null)</code> for statistical function
     * emulations.
     */
    final <U extends Number> Field<U> y(Field<? extends Number> x, Field<U> y) {
        return DSL.nvl2(x, y, DSL.NULL(y.getDataType()));
    }

    /**
     * The data type to use in casts when emulating statistical functions.
     */
    final DataType<? extends Number> d(Context<?> ctx) {
        switch (ctx.family()) {

            // [#11547] These families default to NUMERIC(*, 0) when a scale is
            //          not provided explicitly, hence resort to using floats
            case DERBY:
            case FIREBIRD:
            case HSQLDB:
            case SQLITE:
                return DOUBLE;

            default:
                return NUMERIC;
        }
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    public final boolean $distinct() {
        return distinct;
    }

    @Override
    public final Condition $filterWhere() {
        return filter;
    }

    @Override
    public /* non-final */ <R> R traverse(
        R init,
        Predicate<? super R> abort,
        Predicate<? super MQueryPart> recurse,
        BiFunction<? super R, ? super MQueryPart, ? extends R> accumulate
    ) {
        return super.traverse(
            QOM.traverse(init, abort, recurse, accumulate, this, filter),
            abort, recurse, accumulate
        );
    }
}
