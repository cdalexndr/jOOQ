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

import static org.jooq.impl.DSL.row;

import java.util.Arrays;
import java.util.Collection;

import org.jooq.BetweenAndStep1;
import org.jooq.Comparator;
import org.jooq.Condition;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Function1;
import org.jooq.QuantifiedSelect;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Records;
import org.jooq.Result;
import org.jooq.Row;
import org.jooq.Row1;
import org.jooq.Select;
import org.jooq.SelectField;
import org.jooq.Statement;

/**
 * @author Lukas Eder
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
final class RowImpl1<T1> extends AbstractRow<Record1<T1>> implements Row1<T1> {

    RowImpl1(SelectField<T1> field1) {
        super(field1);
    }

    RowImpl1(FieldsImpl<?> fields) {
        super((FieldsImpl) fields);
    }

    // ------------------------------------------------------------------------
    // Mapping convenience methods
    // ------------------------------------------------------------------------

    @Override
    public final <U> SelectField<U> mapping(Function1<? super T1, ? extends U> function) {
        return rf().convertFrom(Records.mapping(function));
    }

    @Override
    public final <U> SelectField<U> mapping(Class<U> uType, Function1<? super T1, ? extends U> function) {
        return rf().convertFrom(uType, Records.mapping(function));
    }

    // ------------------------------------------------------------------------
    // XXX: Row accessor API
    // ------------------------------------------------------------------------

    @Override
    public final Field<T1> field1() {
        return (Field<T1>) fields.field(0);
    }

    // ------------------------------------------------------------------------
    // Generic comparison predicates
    // ------------------------------------------------------------------------

    @Override
    public final Condition compare(Comparator comparator, Row1<T1> row) {
        return new RowCondition(this, row, comparator);
    }

    @Override
    public final Condition compare(Comparator comparator, Record1<T1> record) {
        return new RowCondition(this, record.valuesRow(), comparator);
    }

    @Override
    public final Condition compare(Comparator comparator, T1 t1) {
        return compare(comparator, row(Tools.field(t1, (DataType) dataType(0))));
    }

    @Override
    public final Condition compare(Comparator comparator, Field<T1> t1) {
        return compare(comparator, row(t1));
    }

    @Override
    public final Condition compare(Comparator comparator, Select<? extends Record1<T1>> select) {
        return new RowSubqueryCondition(this, select, comparator);
    }

    @Override
    public final Condition compare(Comparator comparator, QuantifiedSelect<? extends Record1<T1>> select) {
        return new RowSubqueryCondition(this, select, comparator);
    }

    // ------------------------------------------------------------------------
    // Equal / Not equal comparison predicates
    // ------------------------------------------------------------------------

    @Override
    public final Condition equal(Row1<T1> row) {
        return compare(Comparator.EQUALS, row);
    }

    @Override
    public final Condition equal(Record1<T1> record) {
        return compare(Comparator.EQUALS, record);
    }

    @Override
    public final Condition equal(T1 t1) {
        return compare(Comparator.EQUALS, t1);
    }

    @Override
    public final Condition equal(Field<T1> t1) {
        return compare(Comparator.EQUALS, t1);
    }

    @Override
    public final Condition eq(Row1<T1> row) {
        return equal(row);
    }

    @Override
    public final Condition eq(Record1<T1> record) {
        return equal(record);
    }

    @Override
    public final Condition eq(T1 t1) {
        return equal(t1);
    }

    @Override
    public final Condition eq(Field<T1> t1) {
        return equal(t1);
    }

    @Override
    public final Condition notEqual(Row1<T1> row) {
        return compare(Comparator.NOT_EQUALS, row);
    }

    @Override
    public final Condition notEqual(Record1<T1> record) {
        return compare(Comparator.NOT_EQUALS, record);
    }

    @Override
    public final Condition notEqual(T1 t1) {
        return compare(Comparator.NOT_EQUALS, t1);
    }

    @Override
    public final Condition notEqual(Field<T1> t1) {
        return compare(Comparator.NOT_EQUALS, t1);
    }

    @Override
    public final Condition ne(Row1<T1> row) {
        return notEqual(row);
    }

    @Override
    public final Condition ne(Record1<T1> record) {
        return notEqual(record);
    }

    @Override
    public final Condition ne(T1 t1) {
        return notEqual(t1);
    }

    @Override
    public final Condition ne(Field<T1> t1) {
        return notEqual(t1);
    }

    // ------------------------------------------------------------------------
    // Ordering comparison predicates
    // ------------------------------------------------------------------------

    @Override
    public final Condition lessThan(Row1<T1> row) {
        return compare(Comparator.LESS, row);
    }

    @Override
    public final Condition lessThan(Record1<T1> record) {
        return compare(Comparator.LESS, record);
    }

    @Override
    public final Condition lessThan(T1 t1) {
        return compare(Comparator.LESS, t1);
    }

    @Override
    public final Condition lessThan(Field<T1> t1) {
        return compare(Comparator.LESS, t1);
    }

    @Override
    public final Condition lt(Row1<T1> row) {
        return lessThan(row);
    }

    @Override
    public final Condition lt(Record1<T1> record) {
        return lessThan(record);
    }

    @Override
    public final Condition lt(T1 t1) {
        return lessThan(t1);
    }

    @Override
    public final Condition lt(Field<T1> t1) {
        return lessThan(t1);
    }

    @Override
    public final Condition lessOrEqual(Row1<T1> row) {
        return compare(Comparator.LESS_OR_EQUAL, row);
    }

    @Override
    public final Condition lessOrEqual(Record1<T1> record) {
        return compare(Comparator.LESS_OR_EQUAL, record);
    }

    @Override
    public final Condition lessOrEqual(T1 t1) {
        return compare(Comparator.LESS_OR_EQUAL, t1);
    }

    @Override
    public final Condition lessOrEqual(Field<T1> t1) {
        return compare(Comparator.LESS_OR_EQUAL, t1);
    }

    @Override
    public final Condition le(Row1<T1> row) {
        return lessOrEqual(row);
    }

    @Override
    public final Condition le(Record1<T1> record) {
        return lessOrEqual(record);
    }

    @Override
    public final Condition le(T1 t1) {
        return lessOrEqual(t1);
    }

    @Override
    public final Condition le(Field<T1> t1) {
        return lessOrEqual(t1);
    }

    @Override
    public final Condition greaterThan(Row1<T1> row) {
        return compare(Comparator.GREATER, row);
    }

    @Override
    public final Condition greaterThan(Record1<T1> record) {
        return compare(Comparator.GREATER, record);
    }

    @Override
    public final Condition greaterThan(T1 t1) {
        return compare(Comparator.GREATER, t1);
    }

    @Override
    public final Condition greaterThan(Field<T1> t1) {
        return compare(Comparator.GREATER, t1);
    }

    @Override
    public final Condition gt(Row1<T1> row) {
        return greaterThan(row);
    }

    @Override
    public final Condition gt(Record1<T1> record) {
        return greaterThan(record);
    }

    @Override
    public final Condition gt(T1 t1) {
        return greaterThan(t1);
    }

    @Override
    public final Condition gt(Field<T1> t1) {
        return greaterThan(t1);
    }

    @Override
    public final Condition greaterOrEqual(Row1<T1> row) {
        return compare(Comparator.GREATER_OR_EQUAL, row);
    }

    @Override
    public final Condition greaterOrEqual(Record1<T1> record) {
        return compare(Comparator.GREATER_OR_EQUAL, record);
    }

    @Override
    public final Condition greaterOrEqual(T1 t1) {
        return compare(Comparator.GREATER_OR_EQUAL, t1);
    }

    @Override
    public final Condition greaterOrEqual(Field<T1> t1) {
        return compare(Comparator.GREATER_OR_EQUAL, t1);
    }

    @Override
    public final Condition ge(Row1<T1> row) {
        return greaterOrEqual(row);
    }

    @Override
    public final Condition ge(Record1<T1> record) {
        return greaterOrEqual(record);
    }

    @Override
    public final Condition ge(T1 t1) {
        return greaterOrEqual(t1);
    }

    @Override
    public final Condition ge(Field<T1> t1) {
        return greaterOrEqual(t1);
    }

    // ------------------------------------------------------------------------
    // [NOT] BETWEEN predicates
    // ------------------------------------------------------------------------

    @Override
    public final BetweenAndStep1<T1> between(T1 t1) {
        return between(row(Tools.field(t1, (DataType) dataType(0))));
    }

    @Override
    public final BetweenAndStep1<T1> between(Field<T1> t1) {
        return between(row(t1));
    }

    @Override
    public final BetweenAndStep1<T1> between(Row1<T1> row) {
        return new RowBetweenCondition<>(this, row, false, false);
    }

    @Override
    public final BetweenAndStep1<T1> between(Record1<T1> record) {
        return between(record.valuesRow());
    }

    @Override
    public final Condition between(Row1<T1> minValue, Row1<T1> maxValue) {
        return between(minValue).and(maxValue);
    }

    @Override
    public final Condition between(Record1<T1> minValue, Record1<T1> maxValue) {
        return between(minValue).and(maxValue);
    }

    @Override
    public final BetweenAndStep1<T1> betweenSymmetric(T1 t1) {
        return betweenSymmetric(row(Tools.field(t1, (DataType) dataType(0))));
    }

    @Override
    public final BetweenAndStep1<T1> betweenSymmetric(Field<T1> t1) {
        return betweenSymmetric(row(t1));
    }

    @Override
    public final BetweenAndStep1<T1> betweenSymmetric(Row1<T1> row) {
        return new RowBetweenCondition<>(this, row, false, true);
    }

    @Override
    public final BetweenAndStep1<T1> betweenSymmetric(Record1<T1> record) {
        return betweenSymmetric(record.valuesRow());
    }

    @Override
    public final Condition betweenSymmetric(Row1<T1> minValue, Row1<T1> maxValue) {
        return betweenSymmetric(minValue).and(maxValue);
    }

    @Override
    public final Condition betweenSymmetric(Record1<T1> minValue, Record1<T1> maxValue) {
        return betweenSymmetric(minValue).and(maxValue);
    }

    @Override
    public final BetweenAndStep1<T1> notBetween(T1 t1) {
        return notBetween(row(Tools.field(t1, (DataType) dataType(0))));
    }

    @Override
    public final BetweenAndStep1<T1> notBetween(Field<T1> t1) {
        return notBetween(row(t1));
    }

    @Override
    public final BetweenAndStep1<T1> notBetween(Row1<T1> row) {
        return new RowBetweenCondition<>(this, row, true, false);
    }

    @Override
    public final BetweenAndStep1<T1> notBetween(Record1<T1> record) {
        return notBetween(record.valuesRow());
    }

    @Override
    public final Condition notBetween(Row1<T1> minValue, Row1<T1> maxValue) {
        return notBetween(minValue).and(maxValue);
    }

    @Override
    public final Condition notBetween(Record1<T1> minValue, Record1<T1> maxValue) {
        return notBetween(minValue).and(maxValue);
    }

    @Override
    public final BetweenAndStep1<T1> notBetweenSymmetric(T1 t1) {
        return notBetweenSymmetric(row(Tools.field(t1, (DataType) dataType(0))));
    }

    @Override
    public final BetweenAndStep1<T1> notBetweenSymmetric(Field<T1> t1) {
        return notBetweenSymmetric(row(t1));
    }

    @Override
    public final BetweenAndStep1<T1> notBetweenSymmetric(Row1<T1> row) {
        return new RowBetweenCondition<>(this, row, true, true);
    }

    @Override
    public final BetweenAndStep1<T1> notBetweenSymmetric(Record1<T1> record) {
        return notBetweenSymmetric(record.valuesRow());
    }

    @Override
    public final Condition notBetweenSymmetric(Row1<T1> minValue, Row1<T1> maxValue) {
        return notBetweenSymmetric(minValue).and(maxValue);
    }

    @Override
    public final Condition notBetweenSymmetric(Record1<T1> minValue, Record1<T1> maxValue) {
        return notBetweenSymmetric(minValue).and(maxValue);
    }

    // ------------------------------------------------------------------------
    // [NOT] DISTINCT predicates
    // ------------------------------------------------------------------------

    @Override
    public final Condition isNotDistinctFrom(Row1<T1> row) {
        return new RowIsDistinctFrom(this, row, true);
    }

    @Override
    public final Condition isNotDistinctFrom(Record1<T1> record) {
        return isNotDistinctFrom(record.valuesRow());
    }

    @Override
    public final Condition isNotDistinctFrom(T1 t1) {
        return isNotDistinctFrom(Tools.field(t1, (DataType) dataType(0)));
    }

    @Override
    public final Condition isNotDistinctFrom(Field<T1> t1) {
        return isNotDistinctFrom(row(t1));
    }

    @Override
    public final Condition isNotDistinctFrom(Select<? extends Record1<T1>> select) {
        return new RowIsDistinctFrom(this, select, true);
    }

    @Override
    public final Condition isDistinctFrom(Row1<T1> row) {
        return new RowIsDistinctFrom(this, row, false);
    }

    @Override
    public final Condition isDistinctFrom(Record1<T1> record) {
        return isDistinctFrom(record.valuesRow());
    }

    @Override
    public final Condition isDistinctFrom(T1 t1) {
        return isDistinctFrom(Tools.field(t1, (DataType) dataType(0)));
    }

    @Override
    public final Condition isDistinctFrom(Field<T1> t1) {
        return isDistinctFrom(row(t1));
    }

    @Override
    public final Condition isDistinctFrom(Select<? extends Record1<T1>> select) {
        return new RowIsDistinctFrom(this, select, false);
    }

    // ------------------------------------------------------------------------
    // [NOT] IN predicates
    // ------------------------------------------------------------------------

    @Override
    public final Condition in(Row1<T1>... rows) {
        return in(Arrays.asList(rows));
    }

    @Override
    public final Condition in(Record1<T1>... records) {
        QueryPartList<Row> rows = new QueryPartList<>();

        for (Record record : records)
            rows.add(record.valuesRow());

        return new RowInCondition(this, rows, false);
    }

    @Override
    public final Condition notIn(Row1<T1>... rows) {
        return notIn(Arrays.asList(rows));
    }

    @Override
    public final Condition notIn(Record1<T1>... records) {
        QueryPartList<Row> rows = new QueryPartList<>();

        for (Record record : records)
            rows.add(record.valuesRow());

        return new RowInCondition(this, rows, true);
    }

    @Override
    public final Condition in(Collection<? extends Row1<T1>> rows) {
        return new RowInCondition(this, new QueryPartList<Row>(rows), false);
    }

    @Override
    public final Condition in(Result<? extends Record1<T1>> result) {
        return new RowInCondition(this, new QueryPartList<Row>(Tools.rows(result)), false);
    }

    @Override
    public final Condition notIn(Collection<? extends Row1<T1>> rows) {
        return new RowInCondition(this, new QueryPartList<Row>(rows), true);
    }

    @Override
    public final Condition notIn(Result<? extends Record1<T1>> result) {
        return new RowInCondition(this, new QueryPartList<Row>(Tools.rows(result)), true);
    }

    // ------------------------------------------------------------------------
    // Predicates involving subqueries
    // ------------------------------------------------------------------------

    @Override
    public final Condition equal(Select<? extends Record1<T1>> select) {
        return compare(Comparator.EQUALS, select);
    }

    @Override
    public final Condition equal(QuantifiedSelect<? extends Record1<T1>> select) {
        return compare(Comparator.EQUALS, select);
    }

    @Override
    public final Condition eq(Select<? extends Record1<T1>> select) {
        return equal(select);
    }

    @Override
    public final Condition eq(QuantifiedSelect<? extends Record1<T1>> select) {
        return equal(select);
    }

    @Override
    public final Condition notEqual(Select<? extends Record1<T1>> select) {
        return compare(Comparator.NOT_EQUALS, select);
    }

    @Override
    public final Condition notEqual(QuantifiedSelect<? extends Record1<T1>> select) {
        return compare(Comparator.NOT_EQUALS, select);
    }

    @Override
    public final Condition ne(Select<? extends Record1<T1>> select) {
        return notEqual(select);
    }

    @Override
    public final Condition ne(QuantifiedSelect<? extends Record1<T1>> select) {
        return notEqual(select);
    }

    @Override
    public final Condition greaterThan(Select<? extends Record1<T1>> select) {
        return compare(Comparator.GREATER, select);
    }

    @Override
    public final Condition greaterThan(QuantifiedSelect<? extends Record1<T1>> select) {
        return compare(Comparator.GREATER, select);
    }

    @Override
    public final Condition gt(Select<? extends Record1<T1>> select) {
        return greaterThan(select);
    }

    @Override
    public final Condition gt(QuantifiedSelect<? extends Record1<T1>> select) {
        return greaterThan(select);
    }

    @Override
    public final Condition greaterOrEqual(Select<? extends Record1<T1>> select) {
        return compare(Comparator.GREATER_OR_EQUAL, select);
    }

    @Override
    public final Condition greaterOrEqual(QuantifiedSelect<? extends Record1<T1>> select) {
        return compare(Comparator.GREATER_OR_EQUAL, select);
    }

    @Override
    public final Condition ge(Select<? extends Record1<T1>> select) {
        return greaterOrEqual(select);
    }

    @Override
    public final Condition ge(QuantifiedSelect<? extends Record1<T1>> select) {
        return greaterOrEqual(select);
    }

    @Override
    public final Condition lessThan(Select<? extends Record1<T1>> select) {
        return compare(Comparator.LESS, select);
    }

    @Override
    public final Condition lessThan(QuantifiedSelect<? extends Record1<T1>> select) {
        return compare(Comparator.LESS, select);
    }

    @Override
    public final Condition lt(Select<? extends Record1<T1>> select) {
        return lessThan(select);
    }

    @Override
    public final Condition lt(QuantifiedSelect<? extends Record1<T1>> select) {
        return lessThan(select);
    }

    @Override
    public final Condition lessOrEqual(Select<? extends Record1<T1>> select) {
        return compare(Comparator.LESS_OR_EQUAL, select);
    }

    @Override
    public final Condition lessOrEqual(QuantifiedSelect<? extends Record1<T1>> select) {
        return compare(Comparator.LESS_OR_EQUAL, select);
    }

    @Override
    public final Condition le(Select<? extends Record1<T1>> select) {
        return lessOrEqual(select);
    }

    @Override
    public final Condition le(QuantifiedSelect<? extends Record1<T1>> select) {
        return lessOrEqual(select);
    }

    @Override
    public final Condition in(Select<? extends Record1<T1>> select) {
        return compare(Comparator.IN, select);
    }

    @Override
    public final Condition notIn(Select<? extends Record1<T1>> select) {
        return compare(Comparator.NOT_IN, select);
    }

































}
