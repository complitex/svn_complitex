package org.complitex.dictionary.mybatis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 14.09.12 17:48
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FixedIdTypeHandler {
}
