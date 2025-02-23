/*
 * This file is generated by jOOQ.
 */
package org.jooq.example.r2dbc.db;


import java.util.Arrays;
import java.util.List;

import org.jooq.Schema;
import org.jooq.impl.CatalogImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DefaultCatalog extends CatalogImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>DEFAULT_CATALOG</code>
     */
    public static final DefaultCatalog DEFAULT_CATALOG = new DefaultCatalog();

    /**
     * The schema <code>R2DBC_EXAMPLE</code>.
     */
    public final R2dbcExample R2DBC_EXAMPLE = R2dbcExample.R2DBC_EXAMPLE;

    /**
     * No further instances allowed
     */
    private DefaultCatalog() {
        super("");
    }

    @Override
    public final List<Schema> getSchemas() {
        return Arrays.asList(
            R2dbcExample.R2DBC_EXAMPLE
        );
    }
}
