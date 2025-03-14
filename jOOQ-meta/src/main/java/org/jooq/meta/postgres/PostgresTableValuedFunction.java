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

package org.jooq.meta.postgres;

import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.partitionBy;
import static org.jooq.impl.DSL.row;
import static org.jooq.impl.DSL.rowNumber;
import static org.jooq.impl.DSL.select;
import static org.jooq.meta.postgres.information_schema.Tables.COLUMNS;
import static org.jooq.meta.postgres.information_schema.Tables.PARAMETERS;
import static org.jooq.meta.postgres.information_schema.Tables.ROUTINES;
import static org.jooq.meta.postgres.pg_catalog.Tables.PG_NAMESPACE;
import static org.jooq.meta.postgres.pg_catalog.Tables.PG_PROC;
import static org.jooq.meta.postgres.pg_catalog.Tables.PG_TYPE;
import static org.jooq.tools.StringUtils.defaultString;
import static org.jooq.util.postgres.PostgresDSL.oid;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jooq.Record;
import org.jooq.TableOptions.TableType;
import org.jooq.meta.AbstractTableDefinition;
import org.jooq.meta.ColumnDefinition;
import org.jooq.meta.DataTypeDefinition;
import org.jooq.meta.DefaultColumnDefinition;
import org.jooq.meta.DefaultDataTypeDefinition;
import org.jooq.meta.ParameterDefinition;
import org.jooq.meta.SchemaDefinition;
import org.jooq.meta.postgres.information_schema.tables.Columns;
import org.jooq.meta.postgres.information_schema.tables.Parameters;
import org.jooq.meta.postgres.information_schema.tables.Routines;
import org.jooq.meta.postgres.pg_catalog.tables.PgNamespace;
import org.jooq.meta.postgres.pg_catalog.tables.PgProc;
import org.jooq.meta.postgres.pg_catalog.tables.PgType;

/**
 * @author Lukas Eder
 */
public class PostgresTableValuedFunction extends AbstractTableDefinition {

    private final PostgresRoutineDefinition routine;
    private final String                    specificName;

    public PostgresTableValuedFunction(SchemaDefinition schema, String name, String specificName, String comment) {
        this(schema, name, specificName, comment, null);
    }

    public PostgresTableValuedFunction(SchemaDefinition schema, String name, String specificName, String comment, String source) {
        super(schema, name, comment, TableType.FUNCTION, source);

        this.routine = new PostgresRoutineDefinition(schema.getDatabase(), schema.getInputName(), name, specificName);
        this.specificName = specificName;
    }

    @Override
    public List<ColumnDefinition> getElements0() throws SQLException {
        List<ColumnDefinition> result = new ArrayList<>();

        Routines r = ROUTINES;
        Parameters p = PARAMETERS;
        PgNamespace pg_n = PG_NAMESPACE;
        PgProc pg_p = PG_PROC;
        Columns c = COLUMNS;
        PgType pg_t = PG_TYPE;

        for (Record record : create()

            // [#3375] The first subselect is expected to return only those
            // table-valued functions that return a TABLE type, as that TABLE
            // type is reported implicitly via PARAMETERS.PARAMETER_MODE = 'OUT'
            .select(
                p.PARAMETER_NAME,
                rowNumber().over(partitionBy(p.SPECIFIC_NAME).orderBy(p.ORDINAL_POSITION)).as(p.ORDINAL_POSITION),
                p.DATA_TYPE,
                p.CHARACTER_MAXIMUM_LENGTH,
                p.NUMERIC_PRECISION,
                p.NUMERIC_SCALE,
                inline("true").as(c.IS_NULLABLE),
               (((PostgresDatabase) getDatabase()).is94()
                    ? PARAMETERS.PARAMETER_DEFAULT
                    : inline((String) null)).as(c.COLUMN_DEFAULT),
                p.UDT_SCHEMA,
                p.UDT_NAME
            )
            .from(r)
            .join(p).on(row(r.SPECIFIC_CATALOG, r.SPECIFIC_SCHEMA, r.SPECIFIC_NAME)
                        .eq(p.SPECIFIC_CATALOG, p.SPECIFIC_SCHEMA, p.SPECIFIC_NAME))
            .join(pg_n).on(r.SPECIFIC_SCHEMA.eq(pg_n.NSPNAME))
            .join(pg_p).on(pg_p.PRONAMESPACE.eq(pg_n.OID))
                       .and(pg_p.PRONAME.concat("_").concat(pg_p.OID).eq(r.SPECIFIC_NAME))
            .where(r.SPECIFIC_NAME.eq(specificName))
            .and(p.PARAMETER_MODE.ne("IN"))
            .and(pg_p.PRORETSET)

            .unionAll(

            // [#3376] The second subselect is expected to return only those
            // table-valued functions that return a SETOF [ table type ], as that
            // table reference is reported via a TYPE_UDT that matches a table
            // from INFORMATION_SCHEMA.TABLES
             select(
                coalesce(c.COLUMN_NAME               , getName()                   ).as(c.COLUMN_NAME),
                coalesce(c.ORDINAL_POSITION          , inline(1)                   ).as(c.ORDINAL_POSITION),
                coalesce(c.DATA_TYPE                 , r.DATA_TYPE                 ).as(c.DATA_TYPE),
                coalesce(c.CHARACTER_MAXIMUM_LENGTH  , r.CHARACTER_MAXIMUM_LENGTH  ).as(c.CHARACTER_MAXIMUM_LENGTH),
                coalesce(c.NUMERIC_PRECISION         , r.NUMERIC_PRECISION         ).as(c.NUMERIC_PRECISION),
                coalesce(c.NUMERIC_SCALE             , r.NUMERIC_SCALE             ).as(c.NUMERIC_SCALE),
                coalesce(c.IS_NULLABLE               , "true"                      ).as(c.IS_NULLABLE),
                coalesce(c.COLUMN_DEFAULT            , inline((String) null)       ).as(c.COLUMN_DEFAULT),
                coalesce(c.UDT_SCHEMA                , inline((String) null)       ).as(c.UDT_SCHEMA),
                coalesce(c.UDT_NAME                  , r.UDT_NAME                  ,
                         field(select(pg_t.TYPNAME).from(pg_t).where(pg_t.OID.eq(pg_p.PRORETTYPE)))
                                                                                   ).as(c.UDT_NAME)
            )
            .from(r)

            // [#4269] SETOF [ scalar type ] routines don't have any corresponding
            // entries in INFORMATION_SCHEMA.COLUMNS. Their single result table
            // column type is contained in ROUTINES
            .leftOuterJoin(c)
                .on(row(r.TYPE_UDT_CATALOG, r.TYPE_UDT_SCHEMA, r.TYPE_UDT_NAME)
                    .eq(c.TABLE_CATALOG,    c.TABLE_SCHEMA,    c.TABLE_NAME))
            .join(pg_n).on(r.SPECIFIC_SCHEMA.eq(pg_n.NSPNAME))
            .join(pg_p).on(pg_p.PRONAMESPACE.eq(pg_n.OID))
                       .and(pg_p.PRONAME.concat("_").concat(pg_p.OID).eq(r.SPECIFIC_NAME))
            .where(r.SPECIFIC_NAME.eq(specificName))

            // [#4269] Exclude TABLE [ some type ] routines from the first UNION ALL subselect
            // Can this be done more elegantly?
            .and(         row(r.SPECIFIC_CATALOG, r.SPECIFIC_SCHEMA, r.SPECIFIC_NAME)
                .notIn(select(p.SPECIFIC_CATALOG, p.SPECIFIC_SCHEMA, p.SPECIFIC_NAME).from(p).where(p.PARAMETER_MODE.eq("OUT"))))
            .and(pg_p.PRORETSET))

            // Either subselect can be ordered by their ORDINAL_POSITION
            .orderBy(2)
        ) {

            SchemaDefinition typeSchema = null;

            String schemaName = record.get(p.UDT_SCHEMA);
            if (schemaName != null)
                typeSchema = getDatabase().getSchema(schemaName);

            DataTypeDefinition type = new DefaultDataTypeDefinition(
                getDatabase(),
                typeSchema,
                record.get(p.DATA_TYPE),
                record.get(p.CHARACTER_MAXIMUM_LENGTH),
                record.get(p.NUMERIC_PRECISION),
                record.get(p.NUMERIC_SCALE),
                record.get(c.IS_NULLABLE, boolean.class),
                record.get(c.COLUMN_DEFAULT),
                name(
                    record.get(p.UDT_SCHEMA),
                    record.get(p.UDT_NAME)
                )
            );

            result.add(new DefaultColumnDefinition(
                getDatabase().getTable(getSchema(), getName()),
                record.get(p.PARAMETER_NAME),
                result.size() + 1,
                type,
                defaultString(record.get(c.COLUMN_DEFAULT)).startsWith("nextval"),
                null
            ));
        }

        return result;
    }

    @Override
    protected List<ParameterDefinition> getParameters0() {
        return routine.getInParameters();
    }
}
