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

import static org.jooq.impl.Keywords.K_AS;
import static org.jooq.impl.Keywords.K_CREATE;
import static org.jooq.impl.Keywords.K_ENUM;
import static org.jooq.impl.Keywords.K_TYPE;
import static org.jooq.impl.SQLDataType.VARCHAR;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.jooq.Configuration;
import org.jooq.Context;
import org.jooq.CreateTypeFinalStep;
import org.jooq.CreateTypeStep;
import org.jooq.Field;
import org.jooq.Function1;
import org.jooq.Name;
import org.jooq.conf.ParamType;
import org.jooq.impl.QOM.MCreateType;
import org.jooq.impl.QOM.MField;
import org.jooq.impl.QOM.MList;
import org.jooq.impl.QOM.MQueryPart;

/**
 * @author Lukas Eder
 */
final class CreateTypeImpl extends AbstractDDLQuery implements

    // Cascading interface implementations for CREATE TYPE behaviour
    CreateTypeStep,
    CreateTypeFinalStep,
    MCreateType

{

    private final Name                         type;
    private final QueryPartList<Field<String>> values;

    CreateTypeImpl(Configuration configuration, Name type) {
        super(configuration);

        this.type = type;
        this.values = new QueryPartList<>();
    }

    // ------------------------------------------------------------------------
    // XXX: DSL API
    // ------------------------------------------------------------------------

    @Override
    public final CreateTypeFinalStep asEnum() {
        return asEnum(Collections.emptyList());
    }

    @Override
    public final CreateTypeFinalStep asEnum(String... v) {
        return asEnum(Tools.map(v, s -> DSL.inline(s)));
    }

    @SafeVarargs
    @Override
    public final CreateTypeFinalStep asEnum(Field<String>... v) {
        return asEnum(Arrays.asList(v));
    }

    @Override
    public final CreateTypeFinalStep asEnum(Collection<?> v) {
        values.addAll(Tools.fields(v, VARCHAR));
        return this;
    }

    // ------------------------------------------------------------------------
    // XXX: QueryPart API
    // ------------------------------------------------------------------------

    @Override
    public final void accept(Context<?> ctx) {
        ctx.visit(K_CREATE).sql(' ').visit(K_TYPE).sql(' ')
           .visit(type).sql(' ')
           .visit(K_AS).sql(' ').visit(K_ENUM).sql(" (")
           .visit(values, ParamType.INLINED)
           .sql(')');
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Name $name() {
        return type;
    }

    @Override
    public final MList<? extends Field<String>> $values() {
        return values;
    }

    @Override
    public final <R> R traverse(
        R init,
        Predicate<? super R> abort,
        Predicate<? super MQueryPart> recurse,
        BiFunction<? super R, ? super MQueryPart, ? extends R> accumulate
    ) {
        return QOM.traverse(init, abort, recurse, accumulate, this, type, values);
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(this, type, values, (t, v) -> new CreateTypeImpl(configuration(), t).asEnum(v), replacement);
    }
}
