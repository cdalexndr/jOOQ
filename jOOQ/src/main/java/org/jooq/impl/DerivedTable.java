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

// ...
import static org.jooq.impl.Names.N_SELECT;
import static org.jooq.impl.Tools.visitSubquery;

import org.jooq.Clause;
import org.jooq.Context;
import org.jooq.Function1;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.TableOptions;
import org.jooq.impl.QOM.MDerivedTable;
import org.jooq.impl.QOM.MSelect;
import org.jooq.impl.QOM.MTable;

/**
 * @author Lukas Eder
 */
class DerivedTable<R extends Record> extends AbstractTable<R> implements MDerivedTable<R> {

    private final Select<R> query;

    DerivedTable(Select<R> query) {
        super(TableOptions.expression(), N_SELECT);

        this.query = query;
    }

    final Select<R> query() {
        return query;
    }

    @Override
    public final Table<R> as(Name alias) {
        return new TableAlias<>(this, alias, c -> true);
    }

    @Override
    public final Table<R> as(Name alias, Name... fieldAliases) {
        return new TableAlias<>(this, alias, fieldAliases, c -> true);
    }

    @Override
    final FieldsImpl<R> fields0() {
        return new FieldsImpl<>(query.getSelect());
    }

    @Override
    public final Class<? extends R> getRecordType() {
        return query.getRecordType();
    }

    @Override
    public final void accept(Context<?> ctx) {




        visitSubquery(ctx, query, false);
    }

    @Override // Avoid AbstractTable implementation
    public final Clause[] clauses(Context<?> ctx) {
        return null;
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Function1<? super MSelect<R>, ? extends MTable<R>> constructor() {
        return t -> new DerivedTable<>((Select<R>) t);
    }

    @Override
    public final Select<R> $arg1() {
        return query;
    }
}
