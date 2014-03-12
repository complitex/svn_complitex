$(function(){
    $(".organization_picker_form ").on("keyup", ".filter-td input[type='text']", function(event){
            var input = $(this);
            if(event.which == $.ui.keyCode.ENTER){
                input.closest(".organization_picker_form").find(".organization-picker-find").click();
            }
        });        
});