package org.complitex.dictionaryfw.mybatis;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.TransactionIsolationLevel;

import javax.interceptor.InterceptorBinding;
import javax.interceptor.Interceptors;
import java.lang.annotation.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.08.2010 13:26:48
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {
    /**
     * Returns the constant indicating the myBatis executor type.
     *
     * @return the constant indicating the myBatis executor type.
     */
    ExecutorType executorType() default ExecutorType.SIMPLE;

    /**
     * Returns the constant indicating the transaction isolation level.
     *
     * @return the constant indicating the transaction isolation level.
     */
    TransactionIsolationLevel isolationLevel() default TransactionIsolationLevel.NONE;

    /**
     * Flag to indicate that myBatis has to force the transaction {@code commit().}
     *
     * @return false by default, user defined otherwise.
     */
    boolean force() default false;

    /**
     * Flag to indicate the auto commit policy.
     *
     * @return false by default, user defined otherwise.
     */
    boolean autoCommit() default false;

    /**
     * The exception re-thrown when an error occurs during the transaction.
     *
     * @return the exception re-thrown when an error occurs during the
     *         transaction.
     */
    Class<? extends Throwable> rethrowExceptionsAs() default Exception.class;

    /**
     * A custom error message when throwing the custom exception.
     *
     * It supports java.text.MessageFormat place holders, intercepted method
     * arguments will be used as message format arguments.
     *
     * @return a custom error message when throwing the custom exception.
     * @see java.text.MessageFormat#format(String, Object...)
     */
    String exceptionMessage() default "";
}
