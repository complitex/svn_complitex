$(function(){
    $(".organization_picker_form .filter-td input[type='text']").live("keyup", function(event){
            var input = $(this);
            if(event.which == $.ui.keyCode.ENTER){
                input.closest(".organization_picker_form").find(".organization-picker-find").click();
            }
        });        
});