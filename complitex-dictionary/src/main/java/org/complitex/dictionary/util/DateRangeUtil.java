package org.complitex.dictionary.util;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import org.complitex.dictionary.entity.IDateRange;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.10.12 15:48
 */
public class DateRangeUtil {
    public static boolean isConnected(IDateRange d1, IDateRange d2){
        Range<Date> r1 = d1.getEndDate() != null
                ? Ranges.closed(d1.getBeginDate(), d1.getEndDate())
                : Ranges.atLeast(d1.getBeginDate());

        Range<Date> r2 = d2.getEndDate() != null
                ? Ranges.closed(d2.getBeginDate(), d2.getEndDate())
                : Ranges.atLeast(d2.getBeginDate());

        return r1.isConnected(r2);
    }

    public static boolean encloses(IDateRange d1, IDateRange d2){
        Range<Date> r1 = d1.getEndDate() != null
                ? Ranges.closed(d1.getBeginDate(), d1.getEndDate())
                : Ranges.atLeast(d1.getBeginDate());

        Range<Date> r2 = d2.getEndDate() != null
                ? Ranges.closed(d2.getBeginDate(), d2.getEndDate())
                : Ranges.atLeast(d2.getBeginDate());

        return r1.encloses(r2);
    }
}
