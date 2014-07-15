package org.complitex.dictionary.web.component.ajax;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FeedbackMessagesModel;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.09.2010 13:46:19
 */
public class AjaxFeedbackPanel extends FeedbackPanel {

    private List<FeedbackMessage> rendered = new ArrayList<FeedbackMessage>();
    private boolean showTome = true;

    public AjaxFeedbackPanel(final String id) {
        this(id, null);
    }

    public AjaxFeedbackPanel(final String id, IFeedbackMessageFilter filter) {
        super(id, filter);

        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);

        add(new AjaxLink<Void>("clean") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                target.add(AjaxFeedbackPanel.this);
                clean();
            }

            @Override
            public boolean isVisible() {
                return anyMessage();
            }
        });
    }

    @Override
    protected FeedbackMessagesModel newFeedbackMessagesModel() {
        return new FeedbackMessagesModel(this) {

            @Override
            protected List<FeedbackMessage> processMessages(List<FeedbackMessage> messages) {
                rendered.addAll(messages);

                return rendered;
            }
        };
    }

    public void clean() {
        rendered.clear();
    }

    public List<FeedbackMessage> getRendered() {
        return rendered;
    }

    public void setRendered(List<FeedbackMessage> rendered) {
        this.rendered = rendered;
    }

    public boolean isShowTome() {
        return showTome;
    }

    public void setShowTome(boolean showTome) {
        this.showTome = showTome;
    }
}
