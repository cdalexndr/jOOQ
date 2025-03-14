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
 * The <code>DATE ADD</code> statement.
 */
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
final class DateAdd<T>
extends
    AbstractField<T>
implements
    MDateAdd<T>
{

    final Field<T>                date;
    final Field<? extends Number> interval;
    final DatePart                datePart;

    DateAdd(
        Field<T> date,
        Field<? extends Number> interval
    ) {
        super(
            N_DATE_ADD,
            allNotNull((DataType) dataType(date), date, interval)
        );

        this.date = nullSafeNotNull(date, (DataType) OTHER);
        this.interval = nullSafeNotNull(interval, (DataType) OTHER);
        this.datePart = null;
    }

    DateAdd(
        Field<T> date,
        Field<? extends Number> interval,
        DatePart datePart
    ) {
        super(
            N_DATE_ADD,
            allNotNull((DataType) dataType(date), date, interval)
        );

        this.date = nullSafeNotNull(date, (DataType) OTHER);
        this.interval = nullSafeNotNull(interval, (DataType) OTHER);
        this.datePart = datePart;
    }

    // -------------------------------------------------------------------------
    // XXX: QueryPart API
    // -------------------------------------------------------------------------



    @Override
    public final void accept(Context<?> ctx) {
        if (datePart == null)
            ctx.visit(date.add(interval));
        else
            accept0(ctx);
    }

    private final void accept0(Context<?> ctx) {
        Keyword keyword = null;
        Name    name    = null;
        String  string  = null;

        switch (ctx.family()) {



            case CUBRID:
            case MARIADB:
            case MYSQL: {
                ctx.visit(N_DATE_ADD).sql('(').visit(date).sql(", ").visit(K_INTERVAL).sql(' ').visit(interval).sql(' ').visit(standardKeyword()).sql(')');
                break;
            }

            case DERBY:
            case HSQLDB: {
                switch (datePart) {
                    case YEAR:   keyword = DSL.keyword("sql_tsi_year");   break;
                    case MONTH:  keyword = DSL.keyword("sql_tsi_month");  break;
                    case DAY:    keyword = DSL.keyword("sql_tsi_day");    break;
                    case HOUR:   keyword = DSL.keyword("sql_tsi_hour");   break;
                    case MINUTE: keyword = DSL.keyword("sql_tsi_minute"); break;
                    case SECOND: keyword = DSL.keyword("sql_tsi_second"); break;
                    default: throw unsupported();
                }

                ctx.sql("{fn ").visit(N_TIMESTAMPADD).sql('(').visit(keyword).sql(", ").visit(interval).sql(", ").visit(date).sql(") }");
                break;
            }

            case H2: {
                switch (datePart) {
                    case YEAR:   string = "year";   break;
                    case MONTH:  string = "month";  break;
                    case DAY:    string = "day";    break;
                    case HOUR:   string = "hour";   break;
                    case MINUTE: string = "minute"; break;
                    case SECOND: string = "second"; break;
                    default: throw unsupported();
                }

                ctx.visit(N_DATEADD).sql('(').visit(inline(string)).sql(", ").visit(interval).sql(", ").visit(date).sql(')');
                break;
            }






























            case POSTGRES:
            case YUGABYTE: {
                switch (datePart) {
                    case YEAR:   string = "1 year";   break;
                    case MONTH:  string = "1 month";  break;
                    case DAY:    string = "1 day";    break;
                    case HOUR:   string = "1 hour";   break;
                    case MINUTE: string = "1 minute"; break;
                    case SECOND: string = "1 second"; break;
                    default: throw unsupported();
                }

                // [#10258] [#11954]
                if (((AbstractField<?>) interval).getExpressionDataType().isInterval())
                    ctx.sql('(').visit(date).sql(" + ").visit(interval).sql(')');

                else if (getDataType().isDate())

                    // [#10258] Special case for DATE + INTEGER arithmetic
                    if (datePart == DatePart.DAY)
                        ctx.sql('(').visit(date).sql(" + ").visit(interval).sql(')');

                    // [#3824] Ensure that the output for DATE arithmetic will also be of type DATE, not TIMESTAMP
                    else
                        ctx.sql('(').visit(date).sql(" + ").visit(interval).sql(" * ").visit(K_INTERVAL).sql(' ').visit(inline(string)).sql(")::date");

                else
                    ctx.sql('(').visit(date).sql(" + ").visit(interval).sql(" * ").visit(K_INTERVAL).sql(' ').visit(inline(string)).sql(")");

                break;
            }

            case SQLITE: {
                switch (datePart) {
                    case YEAR:   string = " year";   break;
                    case MONTH:  string = " month";  break;
                    case DAY:    string = " day";    break;
                    case HOUR:   string = " hour";   break;
                    case MINUTE: string = " minute"; break;
                    case SECOND: string = " second"; break;
                    default: throw unsupported();
                }

                ctx.visit(N_STRFTIME).sql("('%Y-%m-%d %H:%M:%f', ").visit(date).sql(", ").visit(interval.concat(inline(string))).sql(')');
                break;
            }






































































































































            default: {
                ctx.visit(N_DATEADD).sql('(').visit(standardKeyword()).sql(", ").visit(interval).sql(", ").visit(date).sql(')');
                break;
            }
        }
    }

    private final Keyword standardKeyword() {
        switch (datePart) {
            case YEAR:
            case MONTH:
            case DAY:
            case HOUR:
            case MINUTE:
            case SECOND:
                return datePart.toKeyword();

            default:
                throw unsupported();
        }
    }

    private final UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("Unknown date part : " + datePart);
    }

















    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Field<T> $date() {
        return date;
    }

    @Override
    public final Field<? extends Number> $interval() {
        return interval;
    }

    @Override
    public final DatePart $datePart() {
        return datePart;
    }

    @Override
    public final MDateAdd<T> $date(MField<T> newValue) {
        return constructor().apply(newValue, $interval(), $datePart());
    }

    @Override
    public final MDateAdd<T> $interval(MField<? extends Number> newValue) {
        return constructor().apply($date(), newValue, $datePart());
    }

    @Override
    public final MDateAdd<T> $datePart(DatePart newValue) {
        return constructor().apply($date(), $interval(), newValue);
    }

    public final Function3<? super MField<T>, ? super MField<? extends Number>, ? super DatePart, ? extends MDateAdd<T>> constructor() {
        return (a1, a2, a3) -> new DateAdd<>((Field<T>) a1, (Field<? extends Number>) a2, a3);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(
            this,
            $date(),
            $interval(),
            $datePart(),
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
            $date(),
            $interval()
        );
    }

    // -------------------------------------------------------------------------
    // XXX: The Object API
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object that) {
        if (that instanceof DateAdd) {
            return
                StringUtils.equals($date(), ((DateAdd) that).$date()) &&
                StringUtils.equals($interval(), ((DateAdd) that).$interval()) &&
                StringUtils.equals($datePart(), ((DateAdd) that).$datePart())
            ;
        }
        else
            return super.equals(that);
    }
}
