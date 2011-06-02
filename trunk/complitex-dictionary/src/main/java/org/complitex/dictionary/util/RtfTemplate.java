package org.complitex.dictionary.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 11.05.11 17:45
 */
public class RtfTemplate {
    public enum VARIABLE_TYPE{SIMPLE, FORM}

    private final static Logger log = LoggerFactory.getLogger(RtfTemplate.class);

    public final static Charset CHARSET1251 = Charset.forName("Windows-1251");

    public static class RtfTemplateException extends RuntimeException {
        public RtfTemplateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final static Pattern SIMPLE_VARIABLE_PATTERN = Pattern.compile("%%(\\S+)%%", Pattern.DOTALL | Pattern.MULTILINE);

    private final static Pattern FORM_VARIABLE_PATTERN = Pattern.compile(
            "\\{\\\\field\\{\\\\\\*\\\\fldinst FORMTEXT \\{\\\\\\*\\\\datafield \\S+}}\\{\\\\fldrslt (\\S+)}\\{\\\\\\*\\\\formfield\\" +
                    "{\\\\ffownhelp\\{\\\\\\*\\\\ffhelptext }}}}", Pattern.DOTALL | Pattern.MULTILINE);


    private StringBuilder template = new StringBuilder(8192);

    private Map<String, String> values = new HashMap<String, String>();

    public RtfTemplate(InputStream inputStream, Map<String, String> values) {
        this.values.putAll(values);

        if (!(inputStream instanceof BufferedInputStream)) {
            inputStream = new BufferedInputStream(inputStream);
        }

        Reader reader = new InputStreamReader(inputStream, CHARSET1251);

        try {
            for (int c; (c = reader.read()) != -1;) {
                template.append((char) c);
            }
        } catch (IOException e) {
            throw new RtfTemplateException("Ошибка чтения файла rtf шаблона", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.debug("Ошибка закрытия потока", e);
            }
        }
    }

    public RtfTemplate addValue(String key, String value) {
        values.put(key, value);

        return this;
    }

    public RtfTemplate addValues(Map<String, String> values) {
        this.values.putAll(values);

        return this;
    }

    public String fill(VARIABLE_TYPE variableType) {
        if (values.isEmpty()) {
            return template.toString();
        }

        StringBuffer result = new StringBuffer(template.length());

        Matcher matcher;

        switch (variableType){
            case FORM:
                matcher = FORM_VARIABLE_PATTERN.matcher(template);
                break;
            case SIMPLE:
            default:
                matcher = SIMPLE_VARIABLE_PATTERN.matcher(template);
                break;
        }

        while (matcher.find()) {
            String value = values.get(matcher.group(1));

            if (value != null) {
                matcher.appendReplacement(result, getHexEscape(Matcher.quoteReplacement(value).getBytes(CHARSET1251)));
            }
        }

        matcher.appendTail(result);

        return result.toString();
    }

    public void fill(OutputStream outputStream, VARIABLE_TYPE variableType){
        try {
            outputStream.write(fill(variableType).getBytes(CHARSET1251));
        } catch (IOException e) {
            throw new RtfTemplateException("Ошибка записи в поток", e);
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                log.debug("Ошибка закрытия потока", e);
            }
        }
    }

    private static final String HEXES = "0123456789abcdef";

    private String getHexEscape(byte[] raw) {
        if (raw == null) {
            return null;
        }

        StringBuilder hex = new StringBuilder(2 * raw.length);

        for (byte b : raw) {
            hex.append("\\\\'").append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }

        return hex.toString();
    }
}
