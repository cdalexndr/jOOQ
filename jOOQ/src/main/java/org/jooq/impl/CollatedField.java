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

import static org.jooq.impl.Keywords.K_COLLATE;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.jooq.Binding;
import org.jooq.Collation;
import org.jooq.Context;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Function1;
import org.jooq.impl.QOM.MCollated;
import org.jooq.impl.QOM.MCollation;
import org.jooq.impl.QOM.MField;
import org.jooq.impl.QOM.MQueryPart;

/**
 * @author Lukas Eder
 */
final class CollatedField extends AbstractField<String> implements MCollated {

    private final Field<?>  field;
    private final Collation collation;

    CollatedField(Field<?> field, Collation collation) {
        super(field.getQualifiedName(), type(field), field.getCommentPart(), binding(field));

        this.field = field;
        this.collation = collation;
    }

    @SuppressWarnings("unchecked")
    private static final Binding<?, String> binding(Field<?> field) {
        return field.getType() == String.class ? (Binding<?, String>) field.getBinding() : SQLDataType.VARCHAR.getBinding();
    }

    @SuppressWarnings("unchecked")
    private static final DataType<String> type(Field<?> field) {
        return field.getType() == String.class ? (DataType<String>) field.getDataType() : SQLDataType.VARCHAR;
    }

    @Override
    public final void accept(Context<?> ctx) {

        // [#8011] Collations are vendor-specific storage clauses, which we might need to ignore
        if (ctx.configuration().data("org.jooq.ddl.ignore-storage-clauses") == null)
            ctx.sql("((").visit(field).sql(") ").visit(K_COLLATE).sql(' ').visit(collation).sql(')');
        else
            ctx.visit(field);
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final <R> R traverse(
        R init,
        Predicate<? super R> abort,
        Predicate<? super MQueryPart> recurse,
        BiFunction<? super R, ? super MQueryPart, ? extends R> accumulate
    ) {
        return QOM.traverse(init, abort, recurse, accumulate, this, field, collation);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(this, field, collation, CollatedField::new, replacement);
    }

    @Override
    public final MField<?> $field() {
        return field;
    }

    @Override
    public final MCollation $collation() {
        return collation;
    }
}
