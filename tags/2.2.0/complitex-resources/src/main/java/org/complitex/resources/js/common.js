
/* Common API */

var Complitex = Complitex || {};
Complitex.Common = {
    addCookie : function(name, value, expires){
        // set time, it's in milliseconds
        var today = new Date();
        today.setTime( today.getTime() );

        /*
        if the expires variable is set, make the correct
        expires time, the current script below will set
        it for x number of days, to make it for hours,
        delete * 24, for minutes, delete * 60 * 24
        */
        if ( expires )
        {
            expires = expires * 1000 * 60 * 60 * 24;
        }
        var expires_date = new Date( today.getTime() + (expires) );
        var path = Complitex.Common.getApplicationContext();
        var domain = Complitex.Common.getDomain();

        var cookie = name + "=" +escape( value )
        + ( ( expires ) ? ";expires=" + expires_date.toGMTString() : "" ) +
        ( ( path ) ? ";path=" + path : "" ) +
        ( ( domain ) ? ";domain=" + domain : "" );

        document.cookie = cookie;
    },

    getCookieNames : function(subName){
        var allCookies = document.cookie.split( ';' ), result = [];
        for (var i = 0; i < allCookies.length; i++ ){
            // now we'll split apart each name=value pair
            var name = allCookies[i].split( '=' );

            // and trim left/right whitespace while we're at it
            name = name[0].replace(/^\s+|\s+$/g, '');

            // if the extracted name matches passed check_name
            if (name.indexOf(subName, 0) > -1){
                result.push(name);
            }
        }
        return result;
    },

    // this fixes an issue with the old method, ambiguous values
    // with this test document.cookie.indexOf( name + "=" );
    getCookie : function(name){
        // first we'll split this cookie up into name/value pairs
        // note: document.cookie only returns name=value, not the other components
        var a_all_cookies = document.cookie.split( ';' );
        var a_temp_cookie = '';
        var cookie_name = '';
        var cookie_value = '';
        var b_cookie_found = false; // set boolean t/f default f

        for (var i = 0; i < a_all_cookies.length; i++ )
        {
            // now we'll split apart each name=value pair
            a_temp_cookie = a_all_cookies[i].split( '=' );


            // and trim left/right whitespace while we're at it
            cookie_name = a_temp_cookie[0].replace(/^\s+|\s+$/g, '');

            // if the extracted name matches passed check_name
            if (cookie_name == name)
            {
                b_cookie_found = true;
                // we need to handle case where cookie has no value but exists (no = sign, that is):
                if ( a_temp_cookie.length > 1 )
                {
                    cookie_value = unescape( a_temp_cookie[1].replace(/^\s+|\s+$/g, '') );
                }
                // note that in cases where cookie is initialized but no value, null is returned
                return cookie_value;
            }
            a_temp_cookie = null;
            cookie_name = '';
        }
        if ( !b_cookie_found )
        {
            return null;
        }
    },
    
    setCookie : function(name, value, expires){
        Complitex.Common.deleteCookie(name);
        Complitex.Common.addCookie(name, value, expires);
    },

    deleteCookie : function(name){
        var path = Complitex.Common.getApplicationContext();
        var domain = Complitex.Common.getDomain();

        if ( Complitex.Common.getCookie( name ) ) document.cookie = name + "=" +
            ( ( path ) ? ";path=" + path : "") +
            ( ( domain ) ? ";domain=" + domain : "" ) +
            ";expires=Thu, 01-Jan-1970 00:00:01 GMT";
    },
    
    getApplicationContext : function(){
        var applicationContext;
        var pathname = window.location.pathname;
        var secondSlashIndex = pathname.indexOf("/", 1);
        if(secondSlashIndex > 0){
            applicationContext = pathname.substring(0, secondSlashIndex);
        } else {
            applicationContext = pathname;
        }
        return applicationContext+"/";
    },

    getDomain : function(){
        return "";
    },

    /**
     * See also org.complitex.template.web.template.MenuManager class.
     */
    MenuInfo : {

        //public constants.
        
        //simple menu
        MENU_COOKIE_PREFIX : "MenuPrefix_",

        /** 
         *  Main menu (info left panel) cookie's name const. This const must be the same as appropriate const in 
         *  org.complitex.template.web.template.MenuManager class.
         */
        MAIN_MENU_COOKIE : "MainMenuCookie",
        
        /**
         * Possible main menu cookie's values. Must be the same as appropriate const values in
         * org.complitex.template.web.template.MenuManager class.
         */
        MAIN_MENU_EXPANDED : "1",
        MAIN_MENU_COLLAPSED : "0",

        /** 
         *  Selected menu item cookie's name const. This const must be the same as appropriate const in 
         *  org.complitex.template.web.template.MenuManager class.
         */
        SELECTED_MENU_ITEM_COOKIE : "SelectedMenuItem"
    }
};

/**
 * Resize table headers to set the same width for corresponding columns
 * of multiple tables on the same page.
 *
 * --------------               -----------------
 * |xxx|  ??    |               |xxx   |  ??    |
 * --------------               -----------------
 * ...                  ==>     ...
 * -----------------            -----------------
 * |yyyyyy|  ??    |            |yyyyyy|  ??    |
 * -----------------            -----------------
 */
var alignTableColumns = function() {
    var largest = 0;
    $('table.same-width th').each(function() {
        var width = $(this)[0].offsetWidth;
        if(width > largest) {
            largest = width;
        }
    }).width(largest);
};
$(document).ready(alignTableColumns);
