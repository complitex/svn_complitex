/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.dateinput;

import java.util.Locale;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.WicketRuntimeException;

/**
 *
 * @author Artem
 */
class MaskedDateInputLanguageResourceReference extends ResourceReference {

    enum MaskedDateInputLanguages {

        RUSSIAN("ru"),
        UKRAINIAN("uk");
        private final Locale locale;

        /**
         * Constructor
         * @param locale
         */
        MaskedDateInputLanguages(Locale locale) {
            this.locale = locale;
        }

        /**
         * Constructor
         * @param language
         */
        MaskedDateInputLanguages(String language) {
            this.locale = new Locale(language);
        }

        /**
         * Constructor
         * @param language
         * @param country
         */
        MaskedDateInputLanguages(String language, String country) {
            this.locale = new Locale(language, country);
        }

        /**
         * Constructor
         * @param language
         * @param country
         * @param variant
         */
        MaskedDateInputLanguages(String language, String country, String variant) {
            this.locale = new Locale(language, country, variant);
        }

        /**
         * @return the locale
         */
        Locale getLocale() {
            return locale;
        }

        static MaskedDateInputLanguages getMaskedDateInputLanguages(Locale locale) {
            if (locale == null) {
                return null;
            }

            Locale tmpLocale = null;
            String language = locale.getLanguage();
            String country = locale.getCountry();
            String variant = locale.getVariant();
            String empty = "";

            country = country == null || country.trim().length() <= 0 ? null : country;
            variant = variant == null || variant.trim().length() <= 0 ? null : variant;

            // Equals on language-country-variant
            if (variant != null) {
                for (MaskedDateInputLanguages l : values()) {
                    tmpLocale = l.getLocale();

                    if (tmpLocale.getLanguage().equals(language)
                            && tmpLocale.getCountry().equals(country)
                            && tmpLocale.getVariant().equals(variant)) {
                        return l;
                    }
                }
            }

            // Equals on language-country
            if (country != null) {
                for (MaskedDateInputLanguages l : values()) {
                    tmpLocale = l.getLocale();

                    if (tmpLocale.getLanguage().equals(language)
                            && tmpLocale.getCountry().equals(country)
                            && tmpLocale.getVariant().equals(empty)) {
                        return l;
                    }
                }
            }

            // Equals on language
            for (MaskedDateInputLanguages l : values()) {
                tmpLocale = l.getLocale();

                if (tmpLocale.getLanguage().equals(language)
                        && tmpLocale.getCountry().equals(empty)
                        && tmpLocale.getVariant().equals(empty)) {
                    return l;
                }
            }

            return null;
        }

        static CharSequence getJsFileName(MaskedDateInputLanguages mdil) {
            if (mdil == null) {
                return null;
            }

            Locale locale = mdil.getLocale();
            String country = locale.getCountry();
            String variant = locale.getVariant();
            StringBuffer js = new StringBuffer();

            js.append("jquery.masked_dateinput-");
            js.append(locale.getLanguage());

            if (country != null && country.trim().length() > 0) {
                js.append("-" + country);

                if (variant != null && variant.trim().length() > 0) {
                    js.append("-" + variant);
                }
            }

            js.append(".js");

            return js;
        }
    }

    /**
     * Constructor
     * @param locale Locale
     */
    MaskedDateInputLanguageResourceReference(Locale locale) {
        super(MaskedDateInputLanguageResourceReference.class, getJsFilename(locale));
    }

    /**
     * Method to calculate the name of the javascript file
     * @param locale Locale
     * @return the name
     */
    private static String getJsFilename(Locale locale) {
        MaskedDateInputLanguages mdil = MaskedDateInputLanguages.getMaskedDateInputLanguages(locale);

        if (mdil == null) {
            throw new WicketRuntimeException("The locale cannot load the required javascript locale file");
        }

        return MaskedDateInputLanguages.getJsFileName(mdil).toString();
    }
}
