$(function(){
    var form = $(".formFrame form");
    if(form.length > 0){
        var elements = $(form).find('input, select, textarea');
        $.each(elements, function(index, field){
            //for debug only.
            /*
            if(console){
                console.log("Element = {name : "+field.name+", tagName : "+field.tagName+", type : "+field.type+
                    ", disabled : "+field.disabled+", hasClass: "+ $(field).hasClass('form-template-page-unfocusable')+
                    ", class: "+$(field).attr("class")+"}");
            } */

            var $field = $(field);
            if($field.is(":visible:enabled:not(:hidden)")){
                $(field).focus();
                return false;
            }
        });
    }
});