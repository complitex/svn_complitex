/**
 *
 * @author Artem
 */

(function($) {

    /**
     * Default masked dateinput global settings
     */
    $.masked_dateinput_global_settings = {
        day_placeholder: 'd',
        month_placeholder: 'm',
        year_placeholder: 'y',
        separator: '.',
        debug : false
    };

    $.fn.extend({
        //Helper Function for Caret positioning
        caret: function(begin, end) {
            if (this.length == 0) return;
            if (typeof begin == 'number') {
                end = (typeof end == 'number') ? end : begin;
                return this.each(function() {
                    if (this.setSelectionRange) {
                        this.focus();
                        this.setSelectionRange(begin, end);
                    } else if (this.createTextRange) {
                        var range = this.createTextRange();
                        range.collapse(true);
                        range.moveEnd('character', end);
                        range.moveStart('character', begin);
                        range.select();
                    }
                });
            } else {
                if (this[0].setSelectionRange) {
                    begin = this[0].selectionStart;
                    end = this[0].selectionEnd;
                } else if (document.selection && document.selection.createRange) {
                    var range = document.selection.createRange();
                    begin = 0 - range.duplicate().moveStart('character', -100000);
                    end = begin + range.text.length;
                }
                return {
                    begin: begin,
                    end: end
                };
            }
        },
        unmask_dateinput: function() {
            return this.trigger("unmask_dateinput");
        },

        mask_dateinput: function(options) {
            return this.each(function() {
                var input = $(this);

                var local_settings = $.extend({
                    min_date : null,
                    max_date : null
                }, options || {});

                var placeholder = $.masked_dateinput_global_settings.day_placeholder+$.masked_dateinput_global_settings.day_placeholder+
                $.masked_dateinput_global_settings.separator+$.masked_dateinput_global_settings.month_placeholder+
                $.masked_dateinput_global_settings.month_placeholder+$.masked_dateinput_global_settings.separator+
                $.masked_dateinput_global_settings.year_placeholder+$.masked_dateinput_global_settings.year_placeholder+
                $.masked_dateinput_global_settings.year_placeholder+$.masked_dateinput_global_settings.year_placeholder;

                input.attr("placeholder", placeholder);
                var buffer = placeholder.split("");

                var ignore = false;  			//Variable for ignoring control keys
                var focusText = input.val();

                function validate(){
                    var dateString = buffer.join('');
                    var isValidDate = false;

                    var day = dateString.substring(0, 2);
                    log("Raw day: "+day);
                    if(day - 0 == day){
                        var month = dateString.substring(3, 5);
                        log("Raw month: "+month);
                        if(month - 0 == month){
                            var year = dateString.substring(6);
                            log("Raw year: "+year);
                            if(year - 0 == year){
                                var date = new Date(year, month-1, day);
                                log("Date object: "+date);
                                if(date.getMonth() == month-1 && date.getDate() == day && date.getFullYear() == year){
                                    isValidDate = true;
                                    if(local_settings.min_date && (date < local_settings.min_date)){
                                        isValidDate = false;
                                    }
                                    if(local_settings.max_date && (date > local_settings.max_date)){
                                        isValidDate = false;
                                    }
                                }
                            }
                        }
                    }
                    if(isValidDate){
                        markSuccess();
                    } else {
                        markError();
                    }
                }

                function markError(){
                    input.addClass("masked_dateinput_error");
                }

                function unmarkError(){
                    input.removeClass("masked_dateinput_error");
                }

                function markSuccess(){
                    unmarkError();
                }

                function log(){
                    if($.masked_dateinput_global_settings.debug && console.log){
                        console.log.apply('', arguments);
                    }
                }

                function seekNext(pos) {
                    if(pos <=0 || pos == 2 || pos == 3 || pos >= 5){
                        return pos + 1;
                    } else {
                        return pos+2;
                    }
                }

                function shiftL(pos) {
                    if(pos == 2 || pos == 5){
                        pos--;
                    }
                    switch (pos) {
                        case 9:
                        case 8:
                        case 7:
                        case 6:
                            for(var i = pos; i < 10; i++){
                                buffer[i] = $.masked_dateinput_global_settings.year_placeholder;
                            }
                            break;
                        case 3:
                            buffer[3] = $.masked_dateinput_global_settings.month_placeholder;
                        case 4:
                            buffer[4] = $.masked_dateinput_global_settings.month_placeholder;
                            break;
                        case 0:
                            buffer[0] = $.masked_dateinput_global_settings.day_placeholder;
                        case 1:
                            buffer[1] = $.masked_dateinput_global_settings.day_placeholder;
                            break;
                    }
                    writeBuffer();
                    input.caret(pos);
                }

                function shiftR(pos) {
                    switch(pos){
                        case 0:
                            buffer[1] = $.masked_dateinput_global_settings.day_placeholder;
                            break;
                        case 3:
                            buffer[4] = $.masked_dateinput_global_settings.month_placeholder;
                            break;
                        case 6:
                            for(var i = 7; i<10; i++){
                                buffer[i] = $.masked_dateinput_global_settings.year_placeholder;
                            }
                            break;
                    }
                }

                function determineNextCaretPosition(){
                    var caretPos = 0;
                    if(buffer[0] == $.masked_dateinput_global_settings.day_placeholder){
                        caretPos = 0;
                    }else if(buffer[1] == $.masked_dateinput_global_settings.day_placeholder){
                        caretPos = 1;
                    } else if(buffer[3] == $.masked_dateinput_global_settings.month_placeholder){
                        caretPos = 3;
                    } else if(buffer[4] == $.masked_dateinput_global_settings.month_placeholder){
                        caretPos = 4;
                    } else {
                        for(var i = 6; i<10; i++){
                            if(buffer[i] == $.masked_dateinput_global_settings.year_placeholder){
                                caretPos = i;
                                break;
                            }
                        }
                    }
                    return caretPos;
                }

                function keydownEvent(e) {
                    var pos = $(this).caret();
                    var k = e.keyCode;
                    ignore = (k < 16 || (k > 16 && k < 32) || (k > 32 && k < 41));

                    //delete selection before proceeding
                    if ((pos.begin - pos.end) != 0 && (!ignore || k == 8 || k == 46)){
                        clearBuffer(pos.begin, pos.end);
                        writeBuffer();
                        input.caret(determineNextCaretPosition());
                    }

                    //backspace, delete, and escape get special treatment
                    if (k == 8) {//backspace
                        shiftL(pos.begin - 1);
                        return false;
                    } else if(k == 46){ // delete
                        return false;
                    }
                }

                function keypressEvent(e) {
                    if (ignore) {
                        ignore = false;
                        //Fixes Mac FF bug on backspace
                        return (e.keyCode == 8) ? false : null;
                    }
                    e = e || window.event;
                    var k = e.charCode || e.keyCode || e.which;
                    var pos = $(this).caret();

                    if (e.ctrlKey || e.altKey || e.metaKey) {//Ignore
                        return true;
                    } else if ((k >= 32 && k <= 125) || k > 186) {//typeable characters
                        var p = seekNext(pos.begin - 1);
                        if (p < 10) {
                            var c = String.fromCharCode(k);
                            var isValid = isValidSymbol(p,c);
                            if (isValid) {
                                shiftR(p);
                                buffer[p] = c;
                                writeBuffer();
                                var next = seekNext(p);
                                $(this).caret(next);
                            }

                        }
                    }
                    return false;
                }

                function isValidSymbol(pos, c){
                    var isValid = false;
                    switch (pos) {
                        case 0:
                            isValid = /[0-3]/.test(c);
                            break;
                        case 1:
                            var d1 = buffer[0];
                            if(d1 == 0){
                                isValid = /[1-9]/.test(c);
                            } else if(d1 == 1 || d1 == 2){
                                isValid = /[0-9]/.test(c);
                            } else if(d1 == 3){
                                isValid = (c == 0 || c == 1);
                            }
                            break;
                        case 2:
                            isValid = $.masked_dateinput_global_settings.separator == c;
                            break;
                        case 3:
                            isValid = (c == 0 || c == 1);
                            break;
                        case 4:
                            var m1 = buffer[3];
                            if(m1 == 0){
                                isValid = /[1-9]/.test(c);
                            } else if(m1 == 1){
                                isValid = /[0-2]/.test(c);
                            }
                            break;
                        case 5:
                            isValid = $.masked_dateinput_global_settings.separator == c;
                            break;
                        case 6:
                            isValid = (c == 1 || c == 2);
                            break;
                        case 7 :
                            var y1 = buffer[6];
                            if(y1 == 1){
                                isValid = c == 9;
                            } else if(y1 == 2){
                                isValid = c == 0;
                            }
                            break;
                        case 8:
                        case 9:
                            isValid = /\d/.test(c);
                            break;
                    }
                    return isValid;
                }

                function keyupEvent(e){
                    validate();
                }

                function clearBuffer(start, end) {
                    for (var i = start; i < end && i < 10; i++) {
                        buffer[i] = placeholder.charAt(i);
                    }
                }

                function writeBuffer() {
                    return input.val(buffer.join('')).val();
                }

                function checkValue(){
                    var val = input.val();
                    if(val.length == 10){
                        for(var i = 0; i<10; i++){
                            var c = val.charAt(i);
                            buffer[i] = isValidSymbol(i,c) ? c : placeholder.charAt(i);
                        }
                    }
                    writeBuffer();
                }

                function isEmpty(){
                    return input.val() == placeholder;
                }

                if (!input.attr("readonly")){
                    input
                    .one("unmask_dateinput", function() {
                        input
                        .unbind(".mask_dateinput");
                    })
                    .bind("focus.mask_dateinput", function() {
                        focusText = input.val();

                        checkValue();
                        validate();

                        setTimeout(function() {
                            input.caret(determineNextCaretPosition());
                        }, 0);
                    })
                    .bind("blur.mask_dateinput", function() {
                        checkValue();
                        if(isEmpty()){
                            input.val("");
                            unmarkError();
                        }

                        if (input.val() != focusText){
                            input.change();
                        }
                    })
                    .bind("keydown.mask_dateinput", keydownEvent)
                    .bind("keypress.mask_dateinput", keypressEvent)
                    .bind("keyup.mask_dateinput", keyupEvent);
                }
                checkValue();
                if(isEmpty()){
                    input.val("");
                }
            });
        }
    });
})(jQuery);