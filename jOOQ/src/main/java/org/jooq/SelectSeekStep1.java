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
package org.jooq;

import org.jetbrains.annotations.NotNull;

/**
 * This type is used for the {@link Select}'s DSL API when selecting generic
 * {@link Record} types.
 * <p>
 * Example: <code><pre>
 * -- get all authors' first and last names, and the number
 * -- of books they've written in German, if they have written
 * -- more than five books in German in the last three years
 * -- (from 2011), and sort those authors by last names
 * -- limiting results to the second and third row
 *
 *   SELECT T_AUTHOR.FIRST_NAME, T_AUTHOR.LAST_NAME, COUNT(*)
 *     FROM T_AUTHOR
 *     JOIN T_BOOK ON T_AUTHOR.ID = T_BOOK.AUTHOR_ID
 *    WHERE T_BOOK.LANGUAGE = 'DE'
 *      AND T_BOOK.PUBLISHED &gt; '2008-01-01'
 * GROUP BY T_AUTHOR.FIRST_NAME, T_AUTHOR.LAST_NAME
 *   HAVING COUNT(*) &gt; 5
 * ORDER BY T_AUTHOR.LAST_NAME ASC NULLS FIRST
 *    LIMIT 2
 *   OFFSET 1
 *      FOR UPDATE
 *       OF FIRST_NAME, LAST_NAME
 *       NO WAIT
 * </pre></code> Its equivalent in jOOQ <code><pre>
 * create.select(TAuthor.FIRST_NAME, TAuthor.LAST_NAME, create.count())
 *       .from(T_AUTHOR)
 *       .join(T_BOOK).on(TBook.AUTHOR_ID.equal(TAuthor.ID))
 *       .where(TBook.LANGUAGE.equal("DE"))
 *       .and(TBook.PUBLISHED.greaterThan(parseDate('2008-01-01')))
 *       .groupBy(TAuthor.FIRST_NAME, TAuthor.LAST_NAME)
 *       .having(create.count().greaterThan(5))
 *       .orderBy(TAuthor.LAST_NAME.asc().nullsFirst())
 *       .limit(2)
 *       .offset(1)
 *       .forUpdate()
 *       .of(TAuthor.FIRST_NAME, TAuthor.LAST_NAME)
 *       .noWait();
 * </pre></code> Refer to the manual for more details
 *
 * @author Lukas Eder
 */
public interface SelectSeekStep1<R extends Record, T1> extends SelectLimitStep<R> {

    /**
     * Add a synthetic <code>SEEK AFTER</code> clause to the query.
     * <p>
     * The synthetic <code>SEEK AFTER</code> clause is an alternative way to specify
     * an <code>OFFSET</code>, and thus to perform paging in a SQL query. This
     * can be advantageous for two reasons:
     * <p>
     * <ol>
     * <li>The SQL generated from the <code>SEEK AFTER</code> clause is a regular
     * predicate, which can be used by query plan optimisers to choose an
     * appropriate index. The SQL standard <code>OFFSET</code> clause will need
     * to skip <code>N</code> rows in memory.</li>
     * <li>The <code>SEEK AFTER</code> clause is stable with respect to new data being
     * inserted or data being deleted while paging through pages.</li>
     * </ol>
     * <p>
     * Example: <code><pre>
     * DSL.using(configuration)
     *    .selectFrom(TABLE)
     *    .orderBy(ID, CODE)
     *    .seek(3, "abc")
     *    .fetch();
     * </pre></code>
     * <p>
     * The above query will render the following SQL statement:
     * <p>
     * <code><pre>
     * SELECT table.col1, table.col2, ... FROM table
     * WHERE (id, code) &gt; (3, 'abc')
     * ORDER BY id ASC, code ASC
     * </pre></code>
     * <p>
     * The actual row value expression predicate may be expanded into this
     * equivalent predicate:
     * <p>
     * <code><pre>
     * WHERE (id &gt; 3) OR (id = 3 AND code &gt; 'abc')
     * </pre></code>
     * <p>
     * The <code>SEEK AFTER</code> method currently does not support seeking
     * <code>NULL</code> values, or operating with <code>NULLS FIRST</code>,
     * <code>NULLS LAST</code> clauses in the <code>ORDER BY</code> clause.
     *
     * @see <a
     *      href="http://use-the-index-luke.com/sql/partial-results/fetch-next-page">http://use-the-index-luke.com/sql/partial-results/fetch-next-page</a>
     * @see <a
     *      href="http://blog.jooq.org/2013/10/26/faster-sql-paging-with-jooq-using-the-seek-method/">http://blog.jooq.org/2013/10/26/faster-sql-paging-with-jooq-using-the-seek-method</a>
     * @see #seekAfter(Object)
     */
    @NotNull @CheckReturnValue
    @Support
    SelectSeekLimitStep<R> seek(T1 t1);

    /**
     * Add a synthetic <code>SEEK AFTER</code> clause to the query.
     * <p>
     * The synthetic <code>SEEK AFTER</code> clause is an alternative way to specify
     * an <code>OFFSET</code>, and thus to perform paging in a SQL query. This
     * can be advantageous for two reasons:
     * <p>
     * <ol>
     * <li>The SQL generated from the <code>SEEK AFTER</code> clause is a regular
     * predicate, which can be used by query plan optimisers to choose an
     * appropriate index. The SQL standard <code>OFFSET</code> clause will need
     * to skip <code>N</code> rows in memory.</li>
     * <li>The <code>SEEK AFTER</code> clause is stable with respect to new data being
     * inserted or data being deleted while paging through pages.</li>
     * </ol>
     * <p>
     * Example: <code><pre>
     * DSL.using(configuration)
     *    .selectFrom(TABLE)
     *    .orderBy(ID, CODE)
     *    .seek(3, "abc")
     *    .fetch();
     * </pre></code>
     * <p>
     * The above query will render the following SQL statement:
     * <p>
     * <code><pre>
     * SELECT table.col1, table.col2, ... FROM table
     * WHERE (id, code) &gt; (3, 'abc')
     * ORDER BY id ASC, code ASC
     * </pre></code>
     * <p>
     * The actual row value expression predicate may be expanded into this
     * equivalent predicate:
     * <p>
     * <code><pre>
     * WHERE (id &gt; 3) OR (id = 3 AND code &gt; 'abc')
     * </pre></code>
     * <p>
     * The <code>SEEK AFTER</code> method currently does not support seeking
     * <code>NULL</code> values, or operating with <code>NULLS FIRST</code>,
     * <code>NULLS LAST</code> clauses in the <code>ORDER BY</code> clause.
     *
     * @see <a
     *      href="http://use-the-index-luke.com/sql/partial-results/fetch-next-page">http://use-the-index-luke.com/sql/partial-results/fetch-next-page</a>
     * @see <a
     *      href="http://blog.jooq.org/2013/10/26/faster-sql-paging-with-jooq-using-the-seek-method/">http://blog.jooq.org/2013/10/26/faster-sql-paging-with-jooq-using-the-seek-method</a>
     * @see #seekAfter(Field)
     */
    @NotNull @CheckReturnValue
    @Support
    SelectSeekLimitStep<R> seek(Field<T1> field1);

    /**
     * Add a synthetic <code>SEEK AFTER</code> clause to the query.
     * <p>
     * The synthetic <code>SEEK AFTER</code> clause is an alternative way to specify
     * an <code>OFFSET</code>, and thus to perform paging in a SQL query. This
     * can be advantageous for two reasons:
     * <p>
     * <ol>
     * <li>The SQL generated from the <code>SEEK AFTER</code> clause is a regular
     * predicate, which can be used by query plan optimisers to choose an
     * appropriate index. The SQL standard <code>OFFSET</code> clause will need
     * to skip <code>N</code> rows in memory.</li>
     * <li>The <code>SEEK AFTER</code> clause is stable with respect to new data being
     * inserted or data being deleted while paging through pages.</li>
     * </ol>
     * <p>
     * Example: <code><pre>
     * DSL.using(configuration)
     *    .selectFrom(TABLE)
     *    .orderBy(ID, CODE)
     *    .seekAfter(3, "abc")
     *    .fetch();
     * </pre></code>
     * <p>
     * The above query will render the following SQL statement:
     * <p>
     * <code><pre>
     * SELECT table.col1, table.col2, ... FROM table
     * WHERE (id, code) &gt; (3, 'abc')
     * ORDER BY id ASC, code ASC
     * </pre></code>
     * <p>
     * The actual row value expression predicate may be expanded into this
     * equivalent predicate:
     * <p>
     * <code><pre>
     * WHERE (id &gt; 3) OR (id = 3 AND code &gt; 'abc')
     * </pre></code>
     * <p>
     * The <code>SEEK AFTER</code> method currently does not support seeking
     * <code>NULL</code> values, or operating with <code>NULLS FIRST</code>,
     * <code>NULLS LAST</code> clauses in the <code>ORDER BY</code> clause.
     *
     * @see <a
     *      href="http://use-the-index-luke.com/sql/partial-results/fetch-next-page">http://use-the-index-luke.com/sql/partial-results/fetch-next-page</a>
     * @see <a
     *      href="http://blog.jooq.org/2013/10/26/faster-sql-paging-with-jooq-using-the-seek-method/">http://blog.jooq.org/2013/10/26/faster-sql-paging-with-jooq-using-the-seek-method</a>
     */
    @NotNull @CheckReturnValue
    @Support
    SelectSeekLimitStep<R> seekAfter(T1 t1);

    /**
     * Add a synthetic <code>SEEK AFTER</code> clause to the query.
     * <p>
     * The synthetic <code>SEEK AFTER</code> clause is an alternative way to specify
     * an <code>OFFSET</code>, and thus to perform paging in a SQL query. This
     * can be advantageous for two reasons:
     * <p>
     * <ol>
     * <li>The SQL generated from the <code>SEEK AFTER</code> clause is a regular
     * predicate, which can be used by query plan optimisers to choose an
     * appropriate index. The SQL standard <code>OFFSET</code> clause will need
     * to skip <code>N</code> rows in memory.</li>
     * <li>The <code>SEEK AFTER</code> clause is stable with respect to new data being
     * inserted or data being deleted while paging through pages.</li>
     * </ol>
     * <p>
     * Example: <code><pre>
     * DSL.using(configuration)
     *    .selectFrom(TABLE)
     *    .orderBy(ID, CODE)
     *    .seekAfter(3, "abc")
     *    .fetch();
     * </pre></code>
     * <p>
     * The above query will render the following SQL statement:
     * <p>
     * <code><pre>
     * SELECT table.col1, table.col2, ... FROM table
     * WHERE (id, code) &gt; (3, 'abc')
     * ORDER BY id ASC, code ASC
     * </pre></code>
     * <p>
     * The actual row value expression predicate may be expanded into this
     * equivalent predicate:
     * <p>
     * <code><pre>
     * WHERE (id &gt; 3) OR (id = 3 AND code &gt; 'abc')
     * </pre></code>
     * <p>
     * The <code>SEEK AFTER</code> method currently does not support seeking
     * <code>NULL</code> values, or operating with <code>NULLS FIRST</code>,
     * <code>NULLS LAST</code> clauses in the <code>ORDER BY</code> clause.
     *
     * @see <a
     *      href="http://use-the-index-luke.com/sql/partial-results/fetch-next-page">http://use-the-index-luke.com/sql/partial-results/fetch-next-page</a>
     * @see <a
     *      href="http://blog.jooq.org/2013/10/26/faster-sql-paging-with-jooq-using-the-seek-method/">http://blog.jooq.org/2013/10/26/faster-sql-paging-with-jooq-using-the-seek-method</a>
     */
    @NotNull @CheckReturnValue
    @Support
    SelectSeekLimitStep<R> seekAfter(Field<T1> field1);

    /**
     * Add a synthetic <code>SEEK BEFORE</code> clause to the query.
     * <p>
     * The synthetic <code>SEEK BEFORE</code> clause is an alternative way to specify
     * an <code>OFFSET</code>, and thus to perform paging in a SQL query. This
     * can be advantageous for two reasons:
     * <p>
     * <ol>
     * <li>The SQL generated from the <code>SEEK BEFORE</code> clause is a regular
     * predicate, which can be used by query plan optimisers to choose an
     * appropriate index. The SQL standard <code>OFFSET</code> clause will need
     * to skip <code>N</code> rows in memory.</li>
     * <li>The <code>SEEK BEFORE</code> clause is stable with respect to new data being
     * inserted or data being deleted while paging through pages.</li>
     * </ol>
     * <p>
     * Example: <code><pre>
     * DSL.using(configuration)
     *    .selectFrom(TABLE)
     *    .orderBy(ID, CODE)
     *    .seekBefore(3, "abc")
     *    .fetch();
     * </pre></code>
     * <p>
     * The above query will render the following SQL statement:
     * <p>
     * <code><pre>
     * SELECT table.col1, table.col2, ... FROM table
     * WHERE (id, code) &lt; (3, 'abc')
     * ORDER BY id ASC, code ASC
     * </pre></code>
     * <p>
     * The actual row value expression predicate may be expanded into this
     * equivalent predicate:
     * <p>
     * <code><pre>
     * WHERE (id &lt; 3) OR (id = 3 AND code &lt; 'abc')
     * </pre></code>
     * <p>
     * The <code>SEEK BEFORE</code> method currently does not support seeking
     * <code>NULL</code> values, or operating with <code>NULLS FIRST</code>,
     * <code>NULLS LAST</code> clauses in the <code>ORDER BY</code> clause.
     *
     * @see <a
     *      href="http://use-the-index-luke.com/sql/partial-results/fetch-next-page">http://use-the-index-luke.com/sql/partial-results/fetch-next-page</a>
     * @see <a
     *      href="http://blog.jooq.org/2013/10/26/faster-sql-paging-with-jooq-using-the-seek-method/">http://blog.jooq.org/2013/10/26/faster-sql-paging-with-jooq-using-the-seek-method</a>
     * @deprecated - [#7461] - SEEK BEFORE is not implemented correctly
     */
    @Deprecated
    @NotNull @CheckReturnValue
    @Support
    SelectSeekLimitStep<R> seekBefore(T1 t1);

    /**
     * Add a synthetic <code>SEEK BEFORE</code> clause to the query.
     * <p>
     * The synthetic <code>SEEK BEFORE</code> clause is an alternative way to specify
     * an <code>OFFSET</code>, and thus to perform paging in a SQL query. This
     * can be advantageous for two reasons:
     * <p>
     * <ol>
     * <li>The SQL generated from the <code>SEEK BEFORE</code> clause is a regular
     * predicate, which can be used by query plan optimisers to choose an
     * appropriate index. The SQL standard <code>OFFSET</code> clause will need
     * to skip <code>N</code> rows in memory.</li>
     * <li>The <code>SEEK BEFORE</code> clause is stable with respect to new data being
     * inserted or data being deleted while paging through pages.</li>
     * </ol>
     * <p>
     * Example: <code><pre>
     * DSL.using(configuration)
     *    .selectFrom(TABLE)
     *    .orderBy(ID, CODE)
     *    .seekBefore(3, "abc")
     *    .fetch();
     * </pre></code>
     * <p>
     * The above query will render the following SQL statement:
     * <p>
     * <code><pre>
     * SELECT table.col1, table.col2, ... FROM table
     * WHERE (id, code) &lt; (3, 'abc')
     * ORDER BY id ASC, code ASC
     * </pre></code>
     * <p>
     * The actual row value expression predicate may be expanded into this
     * equivalent predicate:
     * <p>
     * <code><pre>
     * WHERE (id &lt; 3) OR (id = 3 AND code &lt; 'abc')
     * </pre></code>
     * <p>
     * The <code>SEEK BEFORE</code> method currently does not support seeking
     * <code>NULL</code> values, or operating with <code>NULLS FIRST</code>,
     * <code>NULLS LAST</code> clauses in the <code>ORDER BY</code> clause.
     *
     * @see <a
     *      href="http://use-the-index-luke.com/sql/partial-results/fetch-next-page">http://use-the-index-luke.com/sql/partial-results/fetch-next-page</a>
     * @see <a
     *      href="http://blog.jooq.org/2013/10/26/faster-sql-paging-with-jooq-using-the-seek-method/">http://blog.jooq.org/2013/10/26/faster-sql-paging-with-jooq-using-the-seek-method</a>
     * @deprecated - [#7461] - SEEK BEFORE is not implemented correctly
     */
    @Deprecated
    @NotNull @CheckReturnValue
    @Support
    SelectSeekLimitStep<R> seekBefore(Field<T1> field1);
}
