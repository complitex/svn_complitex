package org.complitex.dictionaryfw.mybatis;

import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.08.2010 16:09:01
 */
public class TransactionalMethodInterceptor {
    private static Logger log = LoggerFactory.getLogger(TransactionalMethodInterceptor.class);

    private static final Class<?>[] CAUSE_TYPES = new Class[]{ Throwable.class };
    private static final Class<?>[] MESSAGE_CAUSE_TYPES = new Class[]{ String.class, Throwable.class };

    @EJB(beanName = "SqlSessionFactoryBean")
    private SqlSessionFactoryBean sqlSessionFactoryBean;

    @SuppressWarnings({"EjbProhibitedPackageUsageInspection"})
    @AroundInvoke
    public Object intercept(InvocationContext invocation) throws Throwable {
        SqlSessionManager sqlSessionManager = sqlSessionFactoryBean.getSqlSessionManager();

        Method interceptedMethod = invocation.getMethod();
        Transactional transactional = interceptedMethod.getAnnotation(Transactional.class);

        if (transactional == null){
            return invocation.proceed();
        }

        String debugPrefix = null;
        if (log.isDebugEnabled()) {
            debugPrefix = "[Intercepted method: " + interceptedMethod.toGenericString() + "]";
        }

        boolean isSessionInherited = sqlSessionManager.isManagedSessionStarted();

        if (isSessionInherited) {
            if (log.isDebugEnabled()) {
                log.debug(debugPrefix
                        + " - SqlSession already set for thread: "
                        + Thread.currentThread().getId());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug(debugPrefix
                        + " - SqlSession not set for thread: "
                        + Thread.currentThread().getId()
                        + ", creating a new one");
            }

            if (TransactionIsolationLevel.NONE == transactional.isolationLevel()) {
                sqlSessionManager.startManagedSession(transactional.executorType(), transactional.autoCommit());
            } else {
                sqlSessionManager.startManagedSession(transactional.executorType(), transactional.isolationLevel());
            }
        }

        Object object = null;
        try {
            object = invocation.proceed();
            if (!isSessionInherited && !transactional.autoCommit()) {
                sqlSessionManager.commit(transactional.force());
            }
        } catch (Throwable t) {
            // rollback the transaction
            sqlSessionManager.rollback(transactional.force());

            // check the caught exception is declared in the invoked method
            for (Class<?> exceptionClass : interceptedMethod.getExceptionTypes()) {
                if (exceptionClass.isAssignableFrom(t.getClass())) {
                    throw t;
                }
            }

            // check the caught exception is of same rethrow type
            if (transactional.rethrowExceptionsAs().isAssignableFrom(t.getClass())) {
                throw t;
            }

            // rethrow the exception as new exception
            String errorMessage;
            Object[] initargs;
            Class<?>[] initargsType;

            if (transactional.exceptionMessage().length() != 0) {
                errorMessage = MessageFormat.format(transactional.exceptionMessage(), invocation.getParameters());
                initargs = new Object[]{ errorMessage, t };
                initargsType = MESSAGE_CAUSE_TYPES;
            } else {
                initargs = new Object[]{ t };
                initargsType = CAUSE_TYPES;
            }

            Constructor<? extends Throwable> exceptionConstructor = getMatchingConstructor(transactional.rethrowExceptionsAs(), initargsType);
            Throwable rethrowEx;
            if (exceptionConstructor != null) {
                try {
                    rethrowEx = exceptionConstructor.newInstance(initargs);
                } catch (Exception e) {
                    errorMessage = "Impossible to re-throw '"
                            + transactional.rethrowExceptionsAs().getName()
                            + "', it needs the constructor with "
                            + Arrays.toString(initargsType)
                            + " argument(s).";
                    log.error(errorMessage, e);
                    rethrowEx = new RuntimeException(errorMessage, e);
                }
            } else {
                errorMessage = "Impossible to re-throw '"
                        + transactional.rethrowExceptionsAs().getName()
                        + "', it needs the constructor with "
                        + Arrays.toString(CAUSE_TYPES)
                        + " or "
                        + Arrays.toString(MESSAGE_CAUSE_TYPES)
                        + " arguments.";
                log.error(errorMessage);
                rethrowEx = new RuntimeException(errorMessage);
            }

            throw rethrowEx;
        } finally {
            // skip close when the sqlSession is inherited from another Transactional method
            if (!isSessionInherited) {
                if (log.isDebugEnabled()) {
                    log.debug(debugPrefix
                            + " - SqlSession of thread: "
                            + Thread.currentThread().getId()
                            + " terminated his lyfe-cycle, closing it");
                }

                sqlSessionManager.close();
            } else if (log.isDebugEnabled()) {
                log.debug(debugPrefix
                        + " - SqlSession of thread: "
                        + Thread.currentThread().getId()
                        + " is inherited, skipped close operation");
            }
        }

        return object;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> Constructor<E> getMatchingConstructor(Class<E> type,
                                                                               Class<?>[] argumentsType) {
        Class<? super E> currentType = type;
        while (Object.class != currentType) {
            for (Constructor<?> constructor : currentType.getConstructors()) {
                if (Arrays.equals(argumentsType, constructor.getParameterTypes())) {
                    return (Constructor<E>) constructor;
                }
            }
            currentType = currentType.getSuperclass();
        }
        return null;
    }
}
