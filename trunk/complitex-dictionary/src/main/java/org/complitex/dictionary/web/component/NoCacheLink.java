package org.complitex.dictionary.web.component;

import org.apache.wicket.markup.html.link.Link;

import java.util.Random;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 07.02.12 16:11
 */
public abstract class NoCacheLink extends Link {
    private Random random = new Random();

    public NoCacheLink(String id) {
        super(id);
    }

    @Override
    protected CharSequence getURL() {
        return super.getURL() + "&" + random.nextInt();
    }
}
