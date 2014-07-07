package org.complitex.dictionary.entity.description;

import java.io.Serializable;
import java.util.Locale;

/**
 * @author Pavel Sknar
 */
public interface ILocalizedType extends Serializable {
    String getLabel(Locale locale);
}
