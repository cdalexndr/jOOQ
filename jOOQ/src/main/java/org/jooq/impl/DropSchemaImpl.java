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
 * The <code>DROP SCHEMA</code> statement.
 */
@SuppressWarnings({ "unused" })
final class DropSchemaImpl
extends
    AbstractDDLQuery
implements
    MDropSchema,
    DropSchemaStep,
    DropSchemaFinalStep
{

    final Schema  schema;
    final boolean ifExists;
          Cascade cascade;

    DropSchemaImpl(
        Configuration configuration,
        Schema schema,
        boolean ifExists
    ) {
        this(
            configuration,
            schema,
            ifExists,
            null
        );
    }

    DropSchemaImpl(
        Configuration configuration,
        Schema schema,
        boolean ifExists,
        Cascade cascade
    ) {
        super(configuration);

        this.schema = schema;
        this.ifExists = ifExists;
        this.cascade = cascade;
    }

    // -------------------------------------------------------------------------
    // XXX: DSL API
    // -------------------------------------------------------------------------

    @Override
    public final DropSchemaImpl cascade() {
        this.cascade = Cascade.CASCADE;
        return this;
    }

    @Override
    public final DropSchemaImpl restrict() {
        this.cascade = Cascade.RESTRICT;
        return this;
    }

    // -------------------------------------------------------------------------
    // XXX: QueryPart API
    // -------------------------------------------------------------------------



    private static final Clause[]        CLAUSES                    = { Clause.DROP_SCHEMA };
    private static final Set<SQLDialect> NO_SUPPORT_IF_EXISTS       = SQLDialect.supportedBy(DERBY, FIREBIRD);
    private static final Set<SQLDialect> REQUIRES_RESTRICT          = SQLDialect.supportedBy(DERBY);






    private final boolean supportsIfExists(Context<?> ctx) {
        return !NO_SUPPORT_IF_EXISTS.contains(ctx.dialect());
    }

    @Override
    public final void accept(Context<?> ctx) {














        accept0(ctx);
    }

    private void accept0(Context<?> ctx) {
        if (ifExists && !supportsIfExists(ctx))
            tryCatch(ctx, DDLStatementType.DROP_SCHEMA, c -> accept1(c));
        else
            accept1(ctx);
    }

    private void accept1(Context<?> ctx) {
        ctx.start(Clause.DROP_SCHEMA_SCHEMA)
           .visit(K_DROP);






            ctx.sql(' ').visit(K_SCHEMA);

        if (ifExists && supportsIfExists(ctx))
            ctx.sql(' ').visit(K_IF_EXISTS);

        ctx.sql(' ').visit(schema);

        if (cascade == Cascade.CASCADE)
            ctx.sql(' ').visit(K_CASCADE);
        else if (cascade == Cascade.RESTRICT || REQUIRES_RESTRICT.contains(ctx.dialect()))
            ctx.sql(' ').visit(K_RESTRICT);

        ctx.end(Clause.DROP_SCHEMA_SCHEMA);
    }


    @Override
    public final Clause[] clauses(Context<?> ctx) {
        return CLAUSES;
    }



    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Schema $schema() {
        return schema;
    }

    @Override
    public final boolean $ifExists() {
        return ifExists;
    }

    @Override
    public final Cascade $cascade() {
        return cascade;
    }

    @Override
    public final MDropSchema $schema(MSchema newValue) {
        return constructor().apply(newValue, $ifExists(), $cascade());
    }

    @Override
    public final MDropSchema $ifExists(boolean newValue) {
        return constructor().apply($schema(), newValue, $cascade());
    }

    @Override
    public final MDropSchema $cascade(Cascade newValue) {
        return constructor().apply($schema(), $ifExists(), newValue);
    }

    public final Function3<? super MSchema, ? super Boolean, ? super Cascade, ? extends MDropSchema> constructor() {
        return (a1, a2, a3) -> new DropSchemaImpl(configuration(), (Schema) a1, a2, a3);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(
            this,
            $schema(),
            $ifExists(),
            $cascade(),
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
            $schema()
        );
    }
}
