$(document).ready(function(){
    $("fieldset.collapsible_fs > legend").live("click", function(){
        var $legend = $(this);
        var $image = $legend.find(".image");
        if($image.hasClass("minus")){
            $image.removeClass("minus").addClass("plus");
        } else if($image.hasClass("plus")){
            $image.removeClass("plus").addClass("minus");
        }
        var $content = $legend.closest("fieldset").find(".fs_content");
        $content.toggle("fast");
    });
});