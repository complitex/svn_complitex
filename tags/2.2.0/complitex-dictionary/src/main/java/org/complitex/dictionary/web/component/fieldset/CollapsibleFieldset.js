/**
 *
 * @author Artem
 */

(function($){
    $.fn.extend({
        collapsible_fieldset: function(){
            return this.each(function(){
                var fieldset = $(this).find("fieldset.collapsible_fs");
                var legend = fieldset.children("legend");
                var content = fieldset.children(".fs_content");
                var image = legend.find(".fs_image");
                
                legend.bind("click", function(){
                    if(image.hasClass("minus")){
                        image.removeClass("minus").addClass("plus");
                    } else if(image.hasClass("plus")){
                        image.removeClass("plus").addClass("minus");
                    }
                    content.toggle("fast");
                });
            });
        }
    });
})(jQuery);