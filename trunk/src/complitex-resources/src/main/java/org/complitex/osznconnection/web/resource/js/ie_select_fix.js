
if((typeof(commons_IEDropdown) == "undefined") && (jQuery.browser.msie) && (parseInt(jQuery.browser.version) == 8)){

    var commons_IEDropdownBase = {
        select : null,
        originalWidth: null,
        dontWiden: false,

        widen: function() {
            if(this.dontWiden) return;
            var styledWidth = this.select.offsetWidth;
            this.select.style.width = "auto";
            var desiredWidth = this.select.offsetWidth;
            // If this control needs less than it was styled for, then we don't need to bother with widening it.
            if(desiredWidth < styledWidth) {
                this.dontWiden = true;
                this.select.style.width = this.originalWidth;
                $(this.select).click(); // Simulate another click, since setting styles has already caused the box to close at this point.
            }
        },

        shrink: function() {
            this.select.style.width = this.originalWidth;
        }
    }

    var commons_IEDropdown = function(select_element){
        this.select = select_element;
        this.originalWidth = $(this.select).width();
        this.dontWiden = false;
        var thisObject = this;

        $(this.select)
        .bind("mousedown", function(){
            commons_IEDropdownBase.widen.apply(thisObject, arguments);
        })
        .bind("change blur", function(){
            commons_IEDropdownBase.shrink.apply(thisObject, arguments);
        });
    }

    commons_IEDropdown.prototype = commons_IEDropdownBase;

    //apply fix to all select elements:
    $(document).ready(function(){
        $("select").each(function(index, select){
            new commons_IEDropdown(select);
        });
    });
}