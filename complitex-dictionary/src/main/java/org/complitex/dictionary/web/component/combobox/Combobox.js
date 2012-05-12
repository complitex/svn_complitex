/**
 *
 * @author Artem
 */

(function($) {
    $.widget("ui.combobox", {
        _create: function() {
            var input,
            self = this,
            select = this.element.hide(),
            selected = select.children(":selected"),
            value = selected.val() ? selected.text() : "",
            wrapper = $("<span>")
            .addClass("ui-combobox")
            .insertAfter(select);

            input = $("<input type='text'>")
            .appendTo(wrapper)
            .val(value)
            .bind("blur.autocomplete", function(){
                var val = $(this).val();
                if(!val) {
                    select.val("");
                }
            })
            .autocomplete({
                delay: 0,
                minLength: 0,
                source: function(request, response) {
                    var matcher = new RegExp($.ui.autocomplete.escapeRegex(request.term), "i");
                    response(select.children("option:enabled").map(function() {
                        var text = $(this).text();
                        if (this.value && (!request.term || matcher.test(text)))
                            return {
                                label: text.replace(
                                    new RegExp(
                                        "(?![^&;]+;)(?!<[^<>]*)(" +
                                        $.ui.autocomplete.escapeRegex(request.term) +
                                        ")(?![^<>]*>)(?![^&;]+;)", "gi"
                                        ), "<strong>$1</strong>" ),
                                value: text,
                                option: this
                            };
                    }));
                },
                select: function(event, ui) {
                    ui.item.option.selected = true;
                    self._trigger("selected", event, {
                        item: ui.item.option
                    });
                },
                change: function(event, ui) {
                    if (!ui.item) {
                        var matcher = new RegExp("^" + $.ui.autocomplete.escapeRegex($(this).val()) + "$", "i"),
                        valid = false;
                        select.children("option:enabled").each(function() {
                            if ($(this).text().match(matcher)) {
                                this.selected = valid = true;
                                return false;
                            }
                        });
                        if (!valid) {
                            // remove invalid value, as it didn't match anything
                            $(this).val("");
                            select.val("");
                            input.data("autocomplete").term = "";
                            return false;
                        }
                    }
                }
            });
            
            input.data("autocomplete")._renderItem = function(ul, item) {
                $("<li></li>")
                .data("item.autocomplete", item)
                .append("<a>" + item.label + "</a>")
                .appendTo(ul);
            };
            
            $("<a>")
            .attr("tabIndex", -1)
            .appendTo(wrapper)
            .button({
                icons: {
                    primary: "ui-icon-triangle-1-s"
                },
                text: false
            })
            .removeClass("ui-corner-all")
            .addClass("ui-combobox-select-all")
            .click(function() {
                // close if already visible
                if (input.autocomplete("widget").is(":visible")) {
                    input.autocomplete("close");
                    return;
                }

                // work around a bug (likely same cause as #5265)
                $(this).blur();

                // pass empty string as value to search for, displaying all results
                input.autocomplete("search", "");
                input.focus();
            })
            .find(".ui-button-text").remove();
        },

        destroy: function() {
            this.wrapper.remove();
            this.element.show();
            $.Widget.prototype.destroy.call(this);
        }
    });
})(jQuery);