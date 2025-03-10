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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
// ...
// ...
// ...
import static org.jooq.SQLDialect.MYSQL;
import static org.jooq.SQLDialect.POSTGRES;
import static org.jooq.SQLDialect.SQLITE;
import static org.jooq.SQLDialect.YUGABYTE;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.Keywords.K_FIRST;
import static org.jooq.impl.Keywords.K_FROM;
import static org.jooq.impl.Keywords.K_IGNORE_NULLS;
import static org.jooq.impl.Keywords.K_LAST;
import static org.jooq.impl.Keywords.K_OVER;
import static org.jooq.impl.Keywords.K_RESPECT_NULLS;
import static org.jooq.impl.SelectQueryImpl.NO_SUPPORT_WINDOW_CLAUSE;
import static org.jooq.impl.Tools.DataKey.DATA_WINDOW_DEFINITIONS;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.jooq.Condition;
import org.jooq.Context;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.OrderField;
// ...
import org.jooq.QueryPart;
import org.jooq.SQLDialect;
import org.jooq.WindowDefinition;
import org.jooq.WindowExcludeStep;
import org.jooq.WindowFinalStep;
import org.jooq.WindowFromFirstLastStep;
import org.jooq.WindowIgnoreNullsStep;
import org.jooq.WindowOrderByStep;
import org.jooq.WindowOverStep;
import org.jooq.WindowPartitionByStep;
import org.jooq.WindowRowsAndStep;
import org.jooq.WindowRowsStep;
import org.jooq.WindowSpecification;
import org.jooq.impl.QOM.FromFirstOrLast;
import org.jooq.impl.QOM.MQueryPart;
import org.jooq.impl.QOM.MWindowDefinition;
import org.jooq.impl.QOM.MWindowFunction;
import org.jooq.impl.QOM.MWindowSpecification;
import org.jooq.impl.QOM.NullTreatment;
import org.jooq.impl.Tools.DataExtendedKey;

/**
 * @author Lukas Eder
 */
abstract class AbstractWindowFunction<T>
extends AbstractField<T>
implements
    WindowFromFirstLastStep<T>,
    WindowPartitionByStep<T>,
    WindowRowsStep<T>,
    WindowRowsAndStep<T>,
    WindowExcludeStep<T>,
    MWindowFunction<T>
{
    private static final Set<SQLDialect> SUPPORT_NO_PARENS_WINDOW_REFERENCE          = SQLDialect.supportedBy(MYSQL, POSTGRES, SQLITE, YUGABYTE);

    // Other attributes
    WindowSpecificationImpl              windowSpecification;
    WindowDefinitionImpl                 windowDefinition;
    Name                                 windowName;

    NullTreatment                        nullTreatment;
    FromFirstOrLast                      fromFirstOrLast;

    AbstractWindowFunction(Name name, DataType<T> type) {
        super(name, type);
    }

    // -------------------------------------------------------------------------
    // XXX QueryPart API
    // -------------------------------------------------------------------------













    @SuppressWarnings("unchecked")
    final QueryPart window(Context<?> ctx) {
        if (windowSpecification != null)
            return DSL.sql("({0})", windowSpecification);

        // [#3727] Referenced WindowDefinitions that contain a frame clause
        // shouldn't be referenced from within parentheses (in MySQL and PostgreSQL)
        if (windowDefinition != null)
            if (SUPPORT_NO_PARENS_WINDOW_REFERENCE.contains(ctx.dialect()))
                return windowDefinition;
            else
                return DSL.sql("({0})", windowDefinition);

        // [#531] Inline window specifications if the WINDOW clause is not supported
        if (windowName != null) {
            if (!NO_SUPPORT_WINDOW_CLAUSE.contains(ctx.dialect()))
                return windowName;

            QueryPartList<WindowDefinition> windows = (QueryPartList<WindowDefinition>) ctx.data(DATA_WINDOW_DEFINITIONS);

            if (windows != null) {
                for (WindowDefinition window : windows)
                    if (((WindowDefinitionImpl) window).getName().equals(windowName))
                        return DSL.sql("({0})", window);
            }

            // [#3162] If a window specification is missing from the query's WINDOW clause,
            // jOOQ should just render the window name regardless of the SQL dialect
            else
                return windowName;
        }

        return null;
    }

    final boolean isWindow() {
        return windowSpecification != null || windowDefinition != null || windowName != null;
    }

    final void acceptOverClause(Context<?> ctx) {
        QueryPart window = window(ctx);

        // Render this clause only if needed
        if (window == null)
            return;

        switch (ctx.family()) {












            default:
                ctx.sql(' ')
                   .visit(K_OVER)
                   .sql(' ');

                ctx.data(DataExtendedKey.DATA_WINDOW_FUNCTION, this, c -> c.visit(window));
                break;
        }
    }























    final void acceptNullTreatment(Context<?> ctx) {
        switch (ctx.family()) {














            default:
                if (nullTreatment == NullTreatment.IGNORE_NULLS)
                    ctx.sql(' ').visit(K_IGNORE_NULLS);
                else if (nullTreatment == NullTreatment.RESPECT_NULLS)
                    ctx.sql(' ').visit(K_RESPECT_NULLS);

                break;
        }
    }

    final void acceptFromFirstOrLast(Context<?> ctx) {
        switch (ctx.family()) {










            default:
                if (fromFirstOrLast == FromFirstOrLast.FROM_LAST)
                    ctx.sql(' ').visit(K_FROM).sql(' ').visit(K_LAST);
                else if (fromFirstOrLast == FromFirstOrLast.FROM_FIRST)
                    ctx.sql(' ').visit(K_FROM).sql(' ').visit(K_FIRST);

                break;
        }
    }

    // -------------------------------------------------------------------------
    // XXX Window function fluent API methods
    // -------------------------------------------------------------------------

    @Override
    public final WindowOverStep<T> ignoreNulls() {
        nullTreatment = NullTreatment.IGNORE_NULLS;
        return this;
    }

    @Override
    public final WindowOverStep<T> respectNulls() {
        nullTreatment = NullTreatment.RESPECT_NULLS;
        return this;
    }

    @Override
    public final WindowIgnoreNullsStep<T> fromFirst() {
        fromFirstOrLast = FromFirstOrLast.FROM_FIRST;
        return this;
    }

    @Override
    public final WindowIgnoreNullsStep<T> fromLast() {
        fromFirstOrLast = FromFirstOrLast.FROM_LAST;
        return this;
    }

    @Override
    public final WindowPartitionByStep<T> over() {
        windowSpecification = new WindowSpecificationImpl();
        return this;
    }

    @Override
    public final WindowFinalStep<T> over(WindowSpecification specification) {
        this.windowSpecification = specification instanceof WindowSpecificationImpl
            ? (WindowSpecificationImpl) specification
            : new WindowSpecificationImpl((WindowDefinitionImpl) specification);

        return this;
    }

    @Override
    public final WindowFinalStep<T> over(WindowDefinition definition) {
        this.windowDefinition = (WindowDefinitionImpl) definition;
        return this;
    }

    @Override
    public final WindowFinalStep<T> over(String n) {
        return over(name(n));
    }

    @Override
    public final WindowFinalStep<T> over(Name n) {
        this.windowName = n;
        return this;
    }

    @Override
    public final WindowOrderByStep<T> partitionBy(Field<?>... fields) {
        windowSpecification.partitionBy(fields);
        return this;
    }

    @Override
    public final WindowOrderByStep<T> partitionBy(Collection<? extends Field<?>> fields) {
        windowSpecification.partitionBy(fields);
        return this;
    }

    @Override
    @Deprecated
    public final WindowOrderByStep<T> partitionByOne() {
        windowSpecification.partitionByOne();
        return this;
    }

    @Override
    public /* non-final */ AbstractWindowFunction<T> orderBy(OrderField<?>... fields) {
        windowSpecification.orderBy(fields);
        return this;
    }

    @Override
    public /* non-final */ AbstractWindowFunction<T> orderBy(Collection<? extends OrderField<?>> fields) {
        windowSpecification.orderBy(fields);
        return this;
    }

    @Override
    public final WindowExcludeStep<T> rowsUnboundedPreceding() {
        windowSpecification.rowsUnboundedPreceding();
        return this;
    }

    @Override
    public final WindowExcludeStep<T> rowsPreceding(int number) {
        windowSpecification.rowsPreceding(number);
        return this;
    }

    @Override
    public final WindowExcludeStep<T> rowsCurrentRow() {
        windowSpecification.rowsCurrentRow();
        return this;
    }

    @Override
    public final WindowExcludeStep<T> rowsUnboundedFollowing() {
        windowSpecification.rowsUnboundedFollowing();
        return this;
    }

    @Override
    public final WindowExcludeStep<T> rowsFollowing(int number) {
        windowSpecification.rowsFollowing(number);
        return this;
    }

    @Override
    public final WindowRowsAndStep<T> rowsBetweenUnboundedPreceding() {
        windowSpecification.rowsBetweenUnboundedPreceding();
        return this;
    }

    @Override
    public final WindowRowsAndStep<T> rowsBetweenPreceding(int number) {
        windowSpecification.rowsBetweenPreceding(number);
        return this;
    }

    @Override
    public final WindowRowsAndStep<T> rowsBetweenCurrentRow() {
        windowSpecification.rowsBetweenCurrentRow();
        return this;
    }

    @Override
    public final WindowRowsAndStep<T> rowsBetweenUnboundedFollowing() {
        windowSpecification.rowsBetweenUnboundedFollowing();
        return this;
    }

    @Override
    public final WindowRowsAndStep<T> rowsBetweenFollowing(int number) {
        windowSpecification.rowsBetweenFollowing(number);
        return this;
    }

    @Override
    public final WindowExcludeStep<T> rangeUnboundedPreceding() {
        windowSpecification.rangeUnboundedPreceding();
        return this;
    }

    @Override
    public final WindowExcludeStep<T> rangePreceding(int number) {
        windowSpecification.rangePreceding(number);
        return this;
    }

    @Override
    public final WindowExcludeStep<T> rangeCurrentRow() {
        windowSpecification.rangeCurrentRow();
        return this;
    }

    @Override
    public final WindowExcludeStep<T> rangeUnboundedFollowing() {
        windowSpecification.rangeUnboundedFollowing();
        return this;
    }

    @Override
    public final WindowExcludeStep<T> rangeFollowing(int number) {
        windowSpecification.rangeFollowing(number);
        return this;
    }

    @Override
    public final WindowRowsAndStep<T> rangeBetweenUnboundedPreceding() {
        windowSpecification.rangeBetweenUnboundedPreceding();
        return this;
    }

    @Override
    public final WindowRowsAndStep<T> rangeBetweenPreceding(int number) {
        windowSpecification.rangeBetweenPreceding(number);
        return this;
    }

    @Override
    public final WindowRowsAndStep<T> rangeBetweenCurrentRow() {
        windowSpecification.rangeBetweenCurrentRow();
        return this;
    }

    @Override
    public final WindowRowsAndStep<T> rangeBetweenUnboundedFollowing() {
        windowSpecification.rangeBetweenUnboundedFollowing();
        return this;
    }

    @Override
    public final WindowRowsAndStep<T> rangeBetweenFollowing(int number) {
        windowSpecification.rangeBetweenFollowing(number);
        return this;
    }

    @Override
    public final WindowExcludeStep<T> groupsUnboundedPreceding() {
        windowSpecification.groupsUnboundedPreceding();
        return this;
    }

    @Override
    public final WindowExcludeStep<T> groupsPreceding(int number) {
        windowSpecification.groupsPreceding(number);
        return this;
    }

    @Override
    public final WindowExcludeStep<T> groupsCurrentRow() {
        windowSpecification.groupsCurrentRow();
        return this;
    }

    @Override
    public final WindowExcludeStep<T> groupsUnboundedFollowing() {
        windowSpecification.groupsUnboundedFollowing();
        return this;
    }

    @Override
    public final WindowExcludeStep<T> groupsFollowing(int number) {
        windowSpecification.groupsFollowing(number);
        return this;
    }

    @Override
    public final WindowRowsAndStep<T> groupsBetweenUnboundedPreceding() {
        windowSpecification.groupsBetweenUnboundedPreceding();
        return this;
    }

    @Override
    public final WindowRowsAndStep<T> groupsBetweenPreceding(int number) {
        windowSpecification.groupsBetweenPreceding(number);
        return this;
    }

    @Override
    public final WindowRowsAndStep<T> groupsBetweenCurrentRow() {
        windowSpecification.groupsBetweenCurrentRow();
        return this;
    }

    @Override
    public final WindowRowsAndStep<T> groupsBetweenUnboundedFollowing() {
        windowSpecification.groupsBetweenUnboundedFollowing();
        return this;
    }

    @Override
    public final WindowRowsAndStep<T> groupsBetweenFollowing(int number) {
        windowSpecification.groupsBetweenFollowing(number);
        return this;
    }

    @Override
    public final WindowExcludeStep<T> andUnboundedPreceding() {
        windowSpecification.andUnboundedPreceding();
        return this;
    }

    @Override
    public final WindowExcludeStep<T> andPreceding(int number) {
        windowSpecification.andPreceding(number);
        return this;
    }

    @Override
    public final WindowExcludeStep<T> andCurrentRow() {
        windowSpecification.andCurrentRow();
        return this;
    }

    @Override
    public final WindowExcludeStep<T> andUnboundedFollowing() {
        windowSpecification.andUnboundedFollowing();
        return this;
    }

    @Override
    public final WindowExcludeStep<T> andFollowing(int number) {
        windowSpecification.andFollowing(number);
        return this;
    }

    @Override
    public final WindowFinalStep<T> excludeCurrentRow() {
        windowSpecification.excludeCurrentRow();
        return this;
    }

    @Override
    public final WindowFinalStep<T> excludeGroup() {
        windowSpecification.excludeGroup();
        return this;
    }

    @Override
    public final WindowFinalStep<T> excludeTies() {
        windowSpecification.excludeTies();
        return this;
    }

    @Override
    public final WindowFinalStep<T> excludeNoOthers() {
        windowSpecification.excludeNoOthers();
        return this;
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final MWindowSpecification $windowSpecification() {
        return windowSpecification;
    }

    final AbstractWindowFunction<T> $windowSpecification(MWindowSpecification s) {
        windowSpecification = (WindowSpecificationImpl) s;
        return this;
    }

    @Override
    public final MWindowDefinition $windowDefinition() {
        return windowDefinition;
    }

    final AbstractWindowFunction<T> $windowDefinition(MWindowDefinition d) {
        windowDefinition = (WindowDefinitionImpl) d;
        return this;
    }

    public final NullTreatment $nullTreatment() {
        return nullTreatment;
    }

    final AbstractWindowFunction<T> $nullTreatment(NullTreatment n) {
        nullTreatment = n;
        return this;
    }

    public final FromFirstOrLast $fromFirstOrLast() {
        return fromFirstOrLast;
    }

    final AbstractWindowFunction<T> $fromFirstOrLast(FromFirstOrLast f) {
        fromFirstOrLast = f;
        return this;
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public /* non-final */ <R> R traverse(
        R init,
        Predicate<? super R> abort,
        Predicate<? super MQueryPart> recurse,
        BiFunction<? super R, ? super MQueryPart, ? extends R> accumulate
    ) {
        return QOM.traverse(init, abort, recurse, accumulate, this, $windowDefinition(), $windowSpecification());
    }
}
