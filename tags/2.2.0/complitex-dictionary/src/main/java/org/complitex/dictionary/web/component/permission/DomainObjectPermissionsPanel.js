/**
 *
 * @author Artem
 */

(function($){
    $.fn.extend({

        permission_select: function(){
            this.find("select[multiple]").change(function(){
                var $select = $(this);
                if($select.find("option:selected").size() == 0){
                    $select.find("option[data-all]").attr("selected", true);
                } else {
                    if($select.find("option[data-all]:selected").size() > 0){
                        $select.find("option:not([data-all])").attr("selected", false);
                    }
                }
            });

            return this;
        }
    });
})(jQuery)