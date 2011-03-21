/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.mysql;

import com.google.common.collect.ImmutableSet;
import java.sql.SQLException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public final class MySqlErrors {

    private static final Logger log = LoggerFactory.getLogger(MySqlErrors.class);

    private static final Collection<Integer> INTEGRITY_CONSTRAINT_VIOLATION_ERROR_CODES = ImmutableSet.of(
            630, 839, 840, 893, 1062, 1169, 1215, 1216, 1217, 1451, 1452, 1557);

    private MySqlErrors() {
    }

    public static boolean isIntegrityConstraintViolationError(SQLException exception) {
        return INTEGRITY_CONSTRAINT_VIOLATION_ERROR_CODES.contains(exception.getErrorCode());
    }
}
