package org.complitex.dictionary.util;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.10.12 17:43
 */
public class ExceptionUtil {
    public static String getCauseMessage(Exception e, boolean initial){
        if (e.getCause() != null){
            String message =  e.getMessage() + ". Причина: ";

            if (initial || e.getCause().getMessage() == null){
                Throwable t = e;

                while (t.getCause() != null){
                    t = t.getCause();
                }

                return message + t.getMessage();
            }

            return message + e.getCause().getMessage();
        }

        return e.getMessage();
    }
}
