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

import org.jooq.impl.QOM.MUDT;

/**
 * UDT definition.
 * <p>
 * Instances of this type cannot be created directly. They are available from
 * generated code.
 *
 * @param <R> The record type
 * @author Lukas Eder
 */
public interface UDT<R extends UDTRecord<R>> extends RecordQualifier<R>, MUDT {

    /**
     * Whether this data type can be used from SQL statements.
     */
    boolean isSQLUsable();

    /**
     * Whether this data type is a synthetic, structural UDT type.
     * <p>
     * This is <code>true</code> for example:
     * <ul>
     * <li>For Oracle <code>TAB%ROWTYPE</code> references, which are synthetic
     * PL/SQL RECORD types in PL/SQL.</li>
     * </ul>
     */
    boolean isSynthetic();
}
