/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.mysql;

import com.google.common.collect.ImmutableSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 *
 * @author Artem
 */
public final class MySqlErrors {

    private static final Collection<Integer> INTEGRITY_CONSTRAINT_VIOLATION_ERROR_CODES = ImmutableSet.of(
            630, 839, 840, 893, 1062, 1169, 1215, 1216, 1217, 1451, 1452, 1557);
    private static final int DUPLICATE_ERROR_CODE = 1062;

    private MySqlErrors() {
    }

    public static boolean isIntegrityConstraintViolationError(SQLException exception) {
        return INTEGRITY_CONSTRAINT_VIOLATION_ERROR_CODES.contains(exception.getErrorCode());
    }

    public static boolean isDuplicateError(SQLException exception) {
        return DUPLICATE_ERROR_CODE == exception.getErrorCode();
    }
}
