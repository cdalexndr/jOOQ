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
 * The <code>CREATE SEQUENCE</code> statement.
 */
@SuppressWarnings({ "hiding", "rawtypes", "unused" })
final class CreateSequenceImpl
extends
    AbstractDDLQuery
implements
    MCreateSequence,
    CreateSequenceFlagsStep,
    CreateSequenceFinalStep
{

    final Sequence<?>             sequence;
    final boolean                 ifNotExists;
          Field<? extends Number> startWith;
          Field<? extends Number> incrementBy;
          Field<? extends Number> minvalue;
          boolean                 noMinvalue;
          Field<? extends Number> maxvalue;
          boolean                 noMaxvalue;
          boolean                 cycle;
          boolean                 noCycle;
          Field<? extends Number> cache;
          boolean                 noCache;

    CreateSequenceImpl(
        Configuration configuration,
        Sequence<?> sequence,
        boolean ifNotExists
    ) {
        this(
            configuration,
            sequence,
            ifNotExists,
            null,
            null,
            null,
            false,
            null,
            false,
            false,
            false,
            null,
            false
        );
    }

    CreateSequenceImpl(
        Configuration configuration,
        Sequence<?> sequence,
        boolean ifNotExists,
        Field<? extends Number> startWith,
        Field<? extends Number> incrementBy,
        Field<? extends Number> minvalue,
        boolean noMinvalue,
        Field<? extends Number> maxvalue,
        boolean noMaxvalue,
        boolean cycle,
        boolean noCycle,
        Field<? extends Number> cache,
        boolean noCache
    ) {
        super(configuration);

        this.sequence = sequence;
        this.ifNotExists = ifNotExists;
        this.startWith = startWith;
        this.incrementBy = incrementBy;
        this.minvalue = minvalue;
        this.noMinvalue = noMinvalue;
        this.maxvalue = maxvalue;
        this.noMaxvalue = noMaxvalue;
        this.cycle = cycle;
        this.noCycle = noCycle;
        this.cache = cache;
        this.noCache = noCache;
    }

    // -------------------------------------------------------------------------
    // XXX: DSL API
    // -------------------------------------------------------------------------

    @Override
    public final CreateSequenceImpl startWith(Number startWith) {
        return startWith(Tools.field(startWith, sequence.getDataType()));
    }

    @Override
    public final CreateSequenceImpl startWith(Field<? extends Number> startWith) {
        this.startWith = startWith;
        return this;
    }

    @Override
    public final CreateSequenceImpl incrementBy(Number incrementBy) {
        return incrementBy(Tools.field(incrementBy, sequence.getDataType()));
    }

    @Override
    public final CreateSequenceImpl incrementBy(Field<? extends Number> incrementBy) {
        this.incrementBy = incrementBy;
        return this;
    }

    @Override
    public final CreateSequenceImpl minvalue(Number minvalue) {
        return minvalue(Tools.field(minvalue, sequence.getDataType()));
    }

    @Override
    public final CreateSequenceImpl minvalue(Field<? extends Number> minvalue) {
        this.minvalue = minvalue;
        return this;
    }

    @Override
    public final CreateSequenceImpl noMinvalue() {
        this.noMinvalue = true;
        return this;
    }

    @Override
    public final CreateSequenceImpl maxvalue(Number maxvalue) {
        return maxvalue(Tools.field(maxvalue, sequence.getDataType()));
    }

    @Override
    public final CreateSequenceImpl maxvalue(Field<? extends Number> maxvalue) {
        this.maxvalue = maxvalue;
        return this;
    }

    @Override
    public final CreateSequenceImpl noMaxvalue() {
        this.noMaxvalue = true;
        return this;
    }

    @Override
    public final CreateSequenceImpl cycle() {
        this.cycle = true;
        return this;
    }

    @Override
    public final CreateSequenceImpl noCycle() {
        this.noCycle = true;
        return this;
    }

    @Override
    public final CreateSequenceImpl cache(Number cache) {
        return cache(Tools.field(cache, sequence.getDataType()));
    }

    @Override
    public final CreateSequenceImpl cache(Field<? extends Number> cache) {
        this.cache = cache;
        return this;
    }

    @Override
    public final CreateSequenceImpl noCache() {
        this.noCache = true;
        return this;
    }

    // -------------------------------------------------------------------------
    // XXX: QueryPart API
    // -------------------------------------------------------------------------



    private static final Clause[]            CLAUSES                  = { Clause.CREATE_SEQUENCE };
    private static final Set<SQLDialect>     NO_SUPPORT_IF_NOT_EXISTS = SQLDialect.supportedBy(DERBY, FIREBIRD);
    private static final Set<SQLDialect>     REQUIRES_START_WITH      = SQLDialect.supportedBy(DERBY);
    private static final Set<SQLDialect>     NO_SUPPORT_CACHE         = SQLDialect.supportedBy(DERBY, FIREBIRD, HSQLDB);
    private static final Set<SQLDialect>     NO_SEPARATOR             = SQLDialect.supportedBy(CUBRID, MARIADB);
    private static final Set<SQLDialect>     OMIT_NO_CACHE            = SQLDialect.supportedBy(FIREBIRD, POSTGRES, YUGABYTE);
    private static final Set<SQLDialect>     OMIT_NO_CYCLE            = SQLDialect.supportedBy(FIREBIRD);
    private static final Set<SQLDialect>     OMIT_NO_MINVALUE         = SQLDialect.supportedBy(FIREBIRD);
    private static final Set<SQLDialect>     OMIT_NO_MAXVALUE         = SQLDialect.supportedBy(FIREBIRD);

    private final boolean supportsIfNotExists(Context<?> ctx) {
        return !NO_SUPPORT_IF_NOT_EXISTS.contains(ctx.dialect());
    }

    @Override
    public final void accept(Context<?> ctx) {
        if (ifNotExists && !supportsIfNotExists(ctx))
            tryCatch(ctx, DDLStatementType.CREATE_SEQUENCE, c -> accept0(c));
        else
            accept0(ctx);
    }

    private final void accept0(Context<?> ctx) {
        ctx.start(Clause.CREATE_SEQUENCE_SEQUENCE)
           .visit(K_CREATE)
           .sql(' ')
           .visit(ctx.family() == CUBRID ? K_SERIAL : K_SEQUENCE)
           .sql(' ');

        if (ifNotExists && supportsIfNotExists(ctx))
            ctx.visit(K_IF_NOT_EXISTS)
               .sql(' ');

        ctx.visit(sequence);
        String noSeparator = NO_SEPARATOR.contains(ctx.dialect()) ? "" : " ";

        // Some databases default to sequences starting with MIN_VALUE
        if (startWith == null && REQUIRES_START_WITH.contains(ctx.dialect()))
            ctx.sql(' ').visit(K_START_WITH).sql(" 1");
        else if (startWith != null)
            ctx.sql(' ').visit(K_START_WITH).sql(' ').visit(startWith);

        if (incrementBy != null)
            ctx.sql(' ').visit(K_INCREMENT_BY).sql(' ').visit(incrementBy);

        if (minvalue != null)
            ctx.sql(' ').visit(K_MINVALUE).sql(' ').visit(minvalue);
        else if (noMinvalue && !OMIT_NO_MINVALUE.contains(ctx.dialect()))
            ctx.sql(' ').visit(K_NO).sql(noSeparator).visit(K_MINVALUE);

        if (maxvalue != null)
            ctx.sql(' ').visit(K_MAXVALUE).sql(' ').visit(maxvalue);
        else if (noMaxvalue && !OMIT_NO_MAXVALUE.contains(ctx.dialect()))
            ctx.sql(' ').visit(K_NO).sql(noSeparator).visit(K_MAXVALUE);

        if (cycle)
            ctx.sql(' ').visit(K_CYCLE);
        else if (noCycle && !OMIT_NO_CYCLE.contains(ctx.dialect()))
            ctx.sql(' ').visit(K_NO).sql(noSeparator).visit(K_CYCLE);

        if (!NO_SUPPORT_CACHE.contains(ctx.dialect()))
            if (cache != null)
                ctx.sql(' ').visit(K_CACHE).sql(' ').visit(cache);
            else if (noCache && !OMIT_NO_CACHE.contains(ctx.dialect()))
                ctx.sql(' ').visit(K_NO).sql(noSeparator).visit(K_CACHE);

        ctx.end(Clause.CREATE_SEQUENCE_SEQUENCE);
    }

    @Override
    public final Clause[] clauses(Context<?> ctx) {
        return CLAUSES;
    }



    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Sequence<?> $sequence() {
        return sequence;
    }

    @Override
    public final boolean $ifNotExists() {
        return ifNotExists;
    }

    @Override
    public final Field<? extends Number> $startWith() {
        return startWith;
    }

    @Override
    public final Field<? extends Number> $incrementBy() {
        return incrementBy;
    }

    @Override
    public final Field<? extends Number> $minvalue() {
        return minvalue;
    }

    @Override
    public final boolean $noMinvalue() {
        return noMinvalue;
    }

    @Override
    public final Field<? extends Number> $maxvalue() {
        return maxvalue;
    }

    @Override
    public final boolean $noMaxvalue() {
        return noMaxvalue;
    }

    @Override
    public final boolean $cycle() {
        return cycle;
    }

    @Override
    public final boolean $noCycle() {
        return noCycle;
    }

    @Override
    public final Field<? extends Number> $cache() {
        return cache;
    }

    @Override
    public final boolean $noCache() {
        return noCache;
    }

    @Override
    public final MCreateSequence $sequence(MSequence<?> newValue) {
        return constructor().apply(newValue, $ifNotExists(), $startWith(), $incrementBy(), $minvalue(), $noMinvalue(), $maxvalue(), $noMaxvalue(), $cycle(), $noCycle(), $cache(), $noCache());
    }

    @Override
    public final MCreateSequence $ifNotExists(boolean newValue) {
        return constructor().apply($sequence(), newValue, $startWith(), $incrementBy(), $minvalue(), $noMinvalue(), $maxvalue(), $noMaxvalue(), $cycle(), $noCycle(), $cache(), $noCache());
    }

    @Override
    public final MCreateSequence $startWith(MField<? extends Number> newValue) {
        return constructor().apply($sequence(), $ifNotExists(), newValue, $incrementBy(), $minvalue(), $noMinvalue(), $maxvalue(), $noMaxvalue(), $cycle(), $noCycle(), $cache(), $noCache());
    }

    @Override
    public final MCreateSequence $incrementBy(MField<? extends Number> newValue) {
        return constructor().apply($sequence(), $ifNotExists(), $startWith(), newValue, $minvalue(), $noMinvalue(), $maxvalue(), $noMaxvalue(), $cycle(), $noCycle(), $cache(), $noCache());
    }

    @Override
    public final MCreateSequence $minvalue(MField<? extends Number> newValue) {
        return constructor().apply($sequence(), $ifNotExists(), $startWith(), $incrementBy(), newValue, $noMinvalue(), $maxvalue(), $noMaxvalue(), $cycle(), $noCycle(), $cache(), $noCache());
    }

    @Override
    public final MCreateSequence $noMinvalue(boolean newValue) {
        return constructor().apply($sequence(), $ifNotExists(), $startWith(), $incrementBy(), $minvalue(), newValue, $maxvalue(), $noMaxvalue(), $cycle(), $noCycle(), $cache(), $noCache());
    }

    @Override
    public final MCreateSequence $maxvalue(MField<? extends Number> newValue) {
        return constructor().apply($sequence(), $ifNotExists(), $startWith(), $incrementBy(), $minvalue(), $noMinvalue(), newValue, $noMaxvalue(), $cycle(), $noCycle(), $cache(), $noCache());
    }

    @Override
    public final MCreateSequence $noMaxvalue(boolean newValue) {
        return constructor().apply($sequence(), $ifNotExists(), $startWith(), $incrementBy(), $minvalue(), $noMinvalue(), $maxvalue(), newValue, $cycle(), $noCycle(), $cache(), $noCache());
    }

    @Override
    public final MCreateSequence $cycle(boolean newValue) {
        return constructor().apply($sequence(), $ifNotExists(), $startWith(), $incrementBy(), $minvalue(), $noMinvalue(), $maxvalue(), $noMaxvalue(), newValue, $noCycle(), $cache(), $noCache());
    }

    @Override
    public final MCreateSequence $noCycle(boolean newValue) {
        return constructor().apply($sequence(), $ifNotExists(), $startWith(), $incrementBy(), $minvalue(), $noMinvalue(), $maxvalue(), $noMaxvalue(), $cycle(), newValue, $cache(), $noCache());
    }

    @Override
    public final MCreateSequence $cache(MField<? extends Number> newValue) {
        return constructor().apply($sequence(), $ifNotExists(), $startWith(), $incrementBy(), $minvalue(), $noMinvalue(), $maxvalue(), $noMaxvalue(), $cycle(), $noCycle(), newValue, $noCache());
    }

    @Override
    public final MCreateSequence $noCache(boolean newValue) {
        return constructor().apply($sequence(), $ifNotExists(), $startWith(), $incrementBy(), $minvalue(), $noMinvalue(), $maxvalue(), $noMaxvalue(), $cycle(), $noCycle(), $cache(), newValue);
    }

    public final Function12<? super MSequence<?>, ? super Boolean, ? super MField<? extends Number>, ? super MField<? extends Number>, ? super MField<? extends Number>, ? super Boolean, ? super MField<? extends Number>, ? super Boolean, ? super Boolean, ? super Boolean, ? super MField<? extends Number>, ? super Boolean, ? extends MCreateSequence> constructor() {
        return (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12) -> new CreateSequenceImpl(configuration(), (Sequence<?>) a1, a2, (Field<? extends Number>) a3, (Field<? extends Number>) a4, (Field<? extends Number>) a5, a6, (Field<? extends Number>) a7, a8, a9, a10, (Field<? extends Number>) a11, a12);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(
            this,
            $sequence(),
            $ifNotExists(),
            $startWith(),
            $incrementBy(),
            $minvalue(),
            $noMinvalue(),
            $maxvalue(),
            $noMaxvalue(),
            $cycle(),
            $noCycle(),
            $cache(),
            $noCache(),
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
            $sequence(),
            $startWith(),
            $incrementBy(),
            $minvalue(),
            $maxvalue(),
            $cache()
        );
    }
}
