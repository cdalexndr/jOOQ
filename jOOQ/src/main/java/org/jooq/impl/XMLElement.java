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
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.toChar;
import static org.jooq.impl.Keywords.K_NAME;
import static org.jooq.impl.Names.N_XMLELEMENT;
import static org.jooq.impl.QueryPartCollectionView.wrap;
import static org.jooq.impl.Tools.aliased;
import static org.jooq.impl.Tools.unalias;
import static org.jooq.impl.Tools.BooleanDataKey.DATA_LIST_ALREADY_INDENTED;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.Context;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Function1;
import org.jooq.Name;
import org.jooq.XML;
import org.jooq.XMLAttributes;
import org.jooq.impl.QOM.MList;
import org.jooq.impl.QOM.MQueryPart;
import org.jooq.impl.QOM.MXmlelement;

/**
 * @author Lukas Eder
 */
final class XMLElement extends AbstractField<XML> implements MXmlelement {

    private final Name                    elementName;
    private final XMLAttributes           attributes;
    private final QueryPartList<Field<?>> content;

    XMLElement(Name elementName, XMLAttributes attributes, Collection<? extends Field<?>> content) {
        super(N_XMLELEMENT, SQLDataType.XML);

        this.elementName = elementName;
        this.attributes = attributes == null ? XMLAttributesImpl.EMPTY : attributes;
        this.content = new QueryPartList<>(content);
    }

    @Override
    public final void accept(Context<?> ctx) {
        boolean hasContent = !content.isEmpty();
        boolean hasAttributes = !((XMLAttributesImpl) attributes).attributes.isEmpty();
















        boolean format = hasAttributes || !content.isSimple();

        Consumer<Context<?>> accept0 = c -> {
            c.visit(K_NAME).sql(' ').visit(elementName);

            if (hasAttributes)
                if (format)
                    c.sql(',').formatSeparator().visit(attributes);
                else
                    c.sql(", ").visit(attributes);

            if (hasContent)
                if (format)
                    c.sql(',').formatSeparator().visit(wrap(content).map(xmlCastMapper(ctx)));
                else
                    c.sql(", ").visit(wrap(content).map(xmlCastMapper(ctx)));
        };

        ctx.visit(N_XMLELEMENT).sql('(');

        if (format) {
            ctx.formatIndentStart()
               .formatNewLine()
               .data(DATA_LIST_ALREADY_INDENTED, true, c -> accept0.accept(c))
               .formatIndentEnd()
               .formatNewLine();
        }
        else
            accept0.accept(ctx);

        ctx.sql(')');
    }

    static final Function<? super Field<?>, ? extends Field<?>> xmlCastMapper(final Context<?> ctx) {
        return field -> xmlCast(ctx, field);
    }

    static final Field<?> xmlCast(Context<?> ctx, Field<?> field) {
        Field<?> result = field;
        DataType<?> type = field.getDataType();

        switch (ctx.family()) {









            default:
                break;
        }

        if (result != field && aliased(field) != null)
            return result.as(field);
        else
            return result;
    }

    // -------------------------------------------------------------------------
    // XXX: Query Object Model
    // -------------------------------------------------------------------------

    @Override
    public final Name $elementName() {
        return elementName;
    }

    @Override
    public final XMLAttributes $attributes() {
        return attributes;
    }

    @Override
    public final MList<? extends Field<?>> $content() {
        return content;
    }

    @Override
    public final <R> R traverse(
        R init,
        Predicate<? super R> abort,
        Predicate<? super MQueryPart> recurse,
        BiFunction<? super R, ? super MQueryPart, ? extends R> accumulate
    ) {
        return QOM.traverse(init, abort, recurse, accumulate, this, $elementName(), $attributes(), $content());
    }

    @Override
    public final MQueryPart replace(Function1<? super MQueryPart, ? extends MQueryPart> replacement) {
        return QOM.replace(this, $elementName(), $attributes(), $content(), XMLElement::new, replacement);
    }
}
