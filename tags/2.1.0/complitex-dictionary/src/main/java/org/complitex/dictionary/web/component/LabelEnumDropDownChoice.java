package org.complitex.dictionary.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.11.12 16:08
 */
public class LabelEnumDropDownChoice<T extends Enum<T>> extends Panel {
    private EnumDropDownChoice<T> choice;

    public LabelEnumDropDownChoice(String id, Class<T> enumClass, IModel<T> model, boolean nullValid) {
        super(id, model);

        add(new TextLabel("label", model){
            @Override
            public boolean isVisible() {
                return !LabelEnumDropDownChoice.this.isEnabled();
            }
        });

        add(choice = new EnumDropDownChoice<T>("choice", enumClass, model, nullValid){
            @Override
            public boolean isVisible() {
                return LabelEnumDropDownChoice.this.isEnabled();
            }
        });
    }

    @Override
    public Component add(Behavior... behaviors) {
        return choice.add(behaviors);
    }
}