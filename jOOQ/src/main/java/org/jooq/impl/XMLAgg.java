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
import static org.jooq.impl.Names.N_XMLAGG;

import org.jooq.Context;
import org.jooq.Field;
import org.jooq.Function1;
import org.jooq.XML;
import org.jooq.XMLAggOrderByStep;
import org.jooq.impl.QOM.MAggregateFunction;
import org.jooq.impl.QOM.MField;
import org.jooq.impl.QOM.MXMLAgg;


/**
 * @author Lukas Eder
 */
final class XMLAgg extends AbstractAggregateFunction<XML> implements XMLAggOrderByStep<XML>, MXMLAgg {

    XMLAgg(Field<XML> arg) {
        super(false, N_XMLAGG, SQLDataType.XML, arg);
    }

    @Override
    public void accept(Context<?> ctx) {











        ctx.visit(N_XMLAGG).sql('(');
        acceptArguments0(ctx);
        acceptOrderBy(ctx);
        ctx.sql(')');

        acceptFilterClause(ctx);
        acceptOverClause(ctx);
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    @Override
    public final MField<XML> $arg1() {
        return (MField<XML>) getArguments().get(0);
    }

    @Override
    public final Function1<? super MField<XML>, ? extends MAggregateFunction<XML>> constructor() {
        return f -> new XMLAgg((Field<XML>) f);
    }
}
