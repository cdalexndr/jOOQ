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

import static org.jooq.impl.Keywords.K_ROWS_FROM;
import static org.jooq.impl.Names.N_ROWSFROM;
import static org.jooq.impl.Tools.EMPTY_TABLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.jooq.Context;
import org.jooq.Field;
import org.jooq.Function1;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableOptions;
import org.jooq.impl.QOM.MList;
import org.jooq.impl.QOM.MQueryPart;
import org.jooq.impl.QOM.MRowsFrom;
import org.jooq.impl.QOM.MTable;

/**
 * @author Lukas Eder
 */
final class RowsFrom extends AbstractTable<Record> implements MRowsFrom {

    private final TableList tables;

    RowsFrom(Table<?>... tables) {
        super(TableOptions.expression(), N_ROWSFROM);

        this.tables = new TableList(Arrays.asList(tables));
    }

    @Override
    public final Class<? extends Record> getRecordType() {
        // TODO: [#4695] Calculate the correct Record[B] type
        return RecordImplN.class;
    }

    @Override
    final FieldsImpl<Record> fields0() {
        List<Field<?>> fields = new ArrayList<>();

        for (Table<?> table : tables)
            for (Field<?> field : table.fields())
                fields.add(DSL.field(DSL.name(field.getName()), field.getDataType()));

        return new FieldsImpl<>(fields);
    }

    @Override
    public final void accept(Context<?> ctx) {
        ctx.visit(K_ROWS_FROM).sql(" (").declareTables(true, c -> c.visit(tables)).sql(')');
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final MList<? extends Table<?>> $tables() {
        return QueryPartListView.wrap(tables);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(
            this,
            tables.toArray(EMPTY_TABLE),
            RowsFrom::new,
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
        return QOM.traverse(init, abort, recurse, accumulate, this, $tables());
    }
}
