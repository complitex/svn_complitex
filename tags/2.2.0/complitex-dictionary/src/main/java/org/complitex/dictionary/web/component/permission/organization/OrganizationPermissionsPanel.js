/**
 *
 * @author Artem
 */

(function($){
    $.fn.extend({

        organization_permission_select: function(){
            this.find("select[multiple]").change(function(){
                var $select = $(this);
                if($select.find("option[data-all]:selected").size() == 0){
                    $select.find("option[data-always-selected]").attr("selected", true);
                }
            });

            return this;
        }
    });
})(jQuery)