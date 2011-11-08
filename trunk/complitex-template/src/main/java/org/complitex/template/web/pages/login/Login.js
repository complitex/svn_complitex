$(function(){
    //delete menu related cookies
    Complitex.Common.deleteCookie(Complitex.Common.MenuInfo.SELECTED_MENU_ITEM_COOKIE);
    Complitex.Common.deleteCookie(Complitex.Common.MenuInfo.MAIN_MENU_COOKIE);
    var menuCookieNames = Complitex.Common.getCookieNames(Complitex.Common.MenuInfo.MENU_COOKIE_PREFIX);
    for(var i = 0; i<menuCookieNames.length; i++){
        Complitex.Common.deleteCookie(menuCookieNames[i]);
    }

    //set focus on login textfield.
    $(".login_field").focus();
});