
//TODO: remove it
var SERVLET_SESSION_COOKIE_NAME = "JSESSIONID";

$(document).ready(function(){
    $("#homePageLink").click(function(){
        deleteCookie(SERVLET_SESSION_COOKIE_NAME);
//        return true;
    });
});