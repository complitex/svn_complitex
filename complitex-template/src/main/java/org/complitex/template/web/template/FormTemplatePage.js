setFocusOnFirstFormElement = function(){
    var INPUT_TAGS = ["INPUT", "SELECT", "TEXTAREA"];
    var id = $(".formFrame form").attr("id");
    var form = document.getElementById(id);

    if(form != null){
        for(i=0; i<form.elements.length; i++){
            var element = form.elements[i];

            //for debug only.
            //alert("Element = {id : "+element.id+", tagName : "+element.tagName+", type : "+element.type+", disabled : "+element.disabled+"}");

            if(element.type != "hidden" && !element.disabled && ($.inArray(element.tagName.toUpperCase(), INPUT_TAGS) > -1)){
                element.focus();
                return;
            }
        }
    }
}

$(document).ready(setFocusOnFirstFormElement);