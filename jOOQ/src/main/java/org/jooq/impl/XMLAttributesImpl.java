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
import static org.jooq.impl.Names.N_XMLATTRIBUTES;
import static org.jooq.impl.Tools.BooleanDataKey.DATA_AS_REQUIRED;
import static org.jooq.impl.XMLElement.xmlCastMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.jooq.Context;
import org.jooq.Field;
import org.jooq.Function1;
import org.jooq.Name;
import org.jooq.XMLAttributes;
import org.jooq.impl.QOM.MList;
import org.jooq.impl.QOM.MQueryPart;
import org.jooq.impl.QOM.MXmlAttributes;

/**
 * @author Lukas Eder
 */
final class XMLAttributesImpl extends AbstractQueryPart implements XMLAttributes {

    static final XMLAttributes      EMPTY = new XMLAttributesImpl(Collections.emptyList());
    final SelectFieldList<Field<?>> attributes;

    XMLAttributesImpl(Collection<? extends Field<?>> attributes) {
        this.attributes = new SelectFieldList<>(attributes);
    }

    @Override
    public final void accept(Context<?> ctx) {







        boolean format = attributes.size() > 1;

        ctx.data(DATA_AS_REQUIRED, true, c -> {
            c.visit(N_XMLATTRIBUTES).sql('(');

            if (format)
                c.formatIndentStart().formatNewLine();

            c.declareFields(true, x -> x.visit(new SelectFieldList<>(attributes).map(xmlCastMapper(ctx))));

            if (format)
                c.formatIndentEnd().formatNewLine();

            c.sql(')');
        });
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final MList<? extends Field<?>> $attributes() {
        return attributes;
    }

    @Override
    public final <R> R traverse(
        R init,
        Predicate<? super R> abort,
        Predicate<? super MQueryPart> recurse,
        BiFunction<? super R, ? super MQueryPart, ? extends R> accumulate
    ) {
        return QOM.traverse(init, abort, recurse, accumulate, this, $attributes());
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(this, $attributes(), XMLAttributesImpl::new, replacement);
    }
}
