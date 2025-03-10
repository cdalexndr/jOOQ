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
// ...
// ...
import static org.jooq.SQLDialect.CUBRID;
import static org.jooq.SQLDialect.DERBY;
// ...
// ...
// ...
// ...
// ...
import static org.jooq.SQLDialect.SQLITE;
// ...
// ...
import static org.jooq.impl.Keywords.K_CURRENT;
import static org.jooq.impl.Keywords.K_TIMESTAMP;
import static org.jooq.impl.Names.N_CURRENT_BIGDATETIME;
import static org.jooq.impl.Names.N_CURRENT_TIMESTAMP;
import static org.jooq.impl.Names.N_GETDATE;
import static org.jooq.impl.Names.N_NOW;

import java.sql.Timestamp;
import java.util.Set;

import org.jooq.Context;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.impl.QOM.MCurrentTimestamp;

/**
 * @author Lukas Eder
 */
final class CurrentTimestamp<T> extends AbstractField<T> implements MCurrentTimestamp<T> {

    private static final Set<SQLDialect> NO_SUPPORT_PRECISION = SQLDialect.supportedBy(CUBRID, DERBY, SQLITE);

    private final Field<Integer>         precision;

    CurrentTimestamp(DataType<T> type) {
        this(type, null);
    }

    CurrentTimestamp(DataType<T> type, Field<Integer> precision) {
        super(N_CURRENT_TIMESTAMP, type);

        this.precision = precision;
    }

    @Override
    public final void accept(Context<?> ctx) {
        switch (ctx.family()) {

































            case MARIADB:
            case MYSQL:
                if (precision != null && !NO_SUPPORT_PRECISION.contains(ctx.dialect()))
                    ctx.visit(N_CURRENT_TIMESTAMP).sql('(').visit(precision).sql(')');
                else
                    ctx.visit(N_CURRENT_TIMESTAMP).sql("()");

                break;

            default:
                if (precision != null && !NO_SUPPORT_PRECISION.contains(ctx.dialect()))
                    ctx.visit(K_CURRENT).sql('_').visit(K_TIMESTAMP).sql('(').visit(precision).sql(')');
                else
                    ctx.visit(K_CURRENT).sql('_').visit(K_TIMESTAMP);

                break;
        }
    }
}
