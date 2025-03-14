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

import org.jooq.impl.DSL;
import org.jooq.impl.QOM.MComment;

/**
 * A comment.
 * <p>
 * Most RDBMS support commenting (i.e. documenting) stored objects, such as
 * {@link Schema}, {@link Table}, {@link Field}, and other objects. Such
 * comments can be modelled in DDL statements as well as retrieved from meta
 * data through the {@link Comment} type.
 * <p>
 * <strong>Example:</strong>
 * <p>
 * <code><pre>
 * // Assuming import static org.jooq.impl.DSL.*;
 *
 * using(configuration)
 *    .commentOnTable(TABLE)
 *    .is(comment("My Comment"))
 *    .execute();
 * </pre></code>
 * <p>
 * Instances can be created using {@link DSL#comment(String)} and overloads.
 *
 * @author Lukas Eder
 */
public interface Comment extends QueryPart, MComment {

    /**
     * Get the comment.
     */
    String getComment();
}
