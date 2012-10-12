package org.complitex.dictionary.service;

import org.apache.wicket.ThreadContext;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 12.10.12 16:13
 */
public abstract class ContextProcessListener<T> implements IProcessListener<T> {
    private int processed = 0;
    private int skipped = 0;
    private int errors = 0;

    private ThreadContext threadContext = ThreadContext.get(false);

    @Override
    public void processed(T object) {
        restoreContext() ;
        processed++;

        onProcessed(object);
    }

    public abstract void onProcessed(T object);

    @Override
    public void skip(T object) {
        restoreContext() ;

        onSkip(object);
    }

    public abstract void onSkip(T object);

    @Override
    public void error(T object, Exception e) {
        restoreContext() ;

        onError(object, e);
    }

    public abstract void onError(T object, Exception e);

    @Override
    public void done() {
        restoreContext() ;

        onDone();
    }

    public abstract void onDone();

    public int getProcessed() {
        return processed;
    }

    public int getSkipped() {
        return skipped;
    }

    public int getErrors() {
        return errors;
    }

    protected void restoreContext() {
        ThreadContext.restore(threadContext);
    }
}
