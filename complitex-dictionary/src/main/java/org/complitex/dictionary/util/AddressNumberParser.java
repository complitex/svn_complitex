/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.util;

import static com.google.common.collect.Sets.*;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.lang.Integer.*;
import static org.apache.wicket.util.string.Strings.*;

/**
 *
 * @author Artem
 */
public final class AddressNumberParser {

    private static final Logger log = LoggerFactory.getLogger(AddressNumberParser.class);
    private static final String ALL_REGEXP =
            "^(?:(?:\\s*[1-9](?:\\w|\\p{InCyrillic}|\\p{InCyrillic_Supplementary})*\\s*|\\s*[1-9]\\d*\\s*-\\s*[1-9]\\d*\\s*),)+$";
    private static final String REGEXP =
            "\\s*([1-9](?:\\w|\\p{InCyrillic}|\\p{InCyrillic_Supplementary})*)\\s*|\\s*([1-9]\\d*)\\s*-\\s*([1-9]\\d*)\\s*";
    private static final Pattern PATTERN = Pattern.compile(REGEXP);

    private AddressNumberParser() {
    }

    static void inspectGroups(String value) {
        value = value.trim();
        String[] parts = value.split(",");
        log.debug("All parts: {}", Arrays.asList(parts));
        Matcher matcher = null;
        for (String part : parts) {
            log.debug("Part: {}", part);
            if (matcher == null) {
                matcher = PATTERN.matcher(part);
            } else {
                matcher.reset(part);
            }
            if (matcher.matches()) {
                log.debug("Group 1: {}", matcher.group(1));
                log.debug("Group 2: {}", matcher.group(2));
                log.debug("Group 3: {}", matcher.group(3));
            }
        }
    }

    public static String[] parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value is null.");
        }

        Set<String> resultSet = newHashSet();
        value = value.trim();
        String[] parts = value.split(",");
        Matcher matcher = null;
        for (String part : parts) {
            if (matcher == null) {
                matcher = PATTERN.matcher(part);
            } else {
                matcher.reset(part);
            }
            if (matcher.matches()) {
                String group1 = matcher.group(1);
                if (group1 == null) {
                    String group2 = matcher.group(2);
                    String group3 = matcher.group(3);
                    int from = parseInt(group2);
                    int to = parseInt(group3);
                    if (from > to) {
                        int x = from;
                        from = to;
                        to = x;
                    }
                    for (int i = from; i <= to; i++) {
                        resultSet.add(String.valueOf(i));
                    }
                } else {
                    resultSet.add(group1);
                }
            }
        }
        String[] result = new String[resultSet.size()];
        return resultSet.toArray(result);
    }

    public static boolean matches(String value) {
        if (isEmpty(value)) {
            return false;
        }
        return (value + ",").matches(ALL_REGEXP);
    }
}
