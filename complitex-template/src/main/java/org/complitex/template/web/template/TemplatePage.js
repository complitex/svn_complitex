$(function(){
    //simple menus
    (function(){
        /* consts */
        var expandedMenuClassName = "expanded";
        var collapsedMenuClassName = "collapsed";

        $(".block div").each(function(){
            var t = Complitex.Common.getCookie(Complitex.Common.MenuInfo.MENU_COOKIE_PREFIX + $(this).attr("id"))
            //t = 1 - expanded, t = 0 - collapsed
            if(t){
                if(t == 0){
                    //collapsed
                    var element = $(this).find("h2");
                    $(element).parent().find(".bottom").toggle();
                    $(element).removeClass(expandedMenuClassName).addClass(collapsedMenuClassName);
                } else{
                    //expanded
                    //nothing to do.
                }
            }
        });

        $(".block h2 div").hover(function(){
            $(this).addClass("hover");
        }, function(){
            $(this).removeClass("hover");
        });
        $(".block h2").click(function(){
            $(this).parent().find(".bottom").toggle("fast");
            if($(this).hasClass(expandedMenuClassName)){
                Complitex.Common.addCookie(Complitex.Common.MenuInfo.MENU_COOKIE_PREFIX + $(this).parent().attr("id"), "0", "");
                $(this).removeClass(expandedMenuClassName).addClass(collapsedMenuClassName);
            } else {
                Complitex.Common.addCookie(Complitex.Common.MenuInfo.MENU_COOKIE_PREFIX + $(this).parent().attr("id"), "1", "");
                $(this).removeClass(collapsedMenuClassName).addClass(expandedMenuClassName);
            }
        });
    })();

    //main information panel and content panel
    (function(){
        /* consts */
        var expandedContentClassName = "ContentExpanded";
        var collapsedContentClassName = "ContentCollapsed";
        var expandedMainMenuClassName = "expanded";
        var collapsedMainMenuClassName = "collapsed";

        var t = Complitex.Common.getCookie(Complitex.Common.MenuInfo.MAIN_MENU_COOKIE);
        //t = Complitex.Common.MenuInfo.MAIN_MENU_COLLAPSED - expanded, t = Complitex.Common.MenuInfo.MAIN_MENU_EXPANDED - collapsed
        if(t){
            if(t == Complitex.Common.MenuInfo.MAIN_MENU_COLLAPSED){
                //collapsed
                $("#LeftPanel").toggle();
                $("#TopPanel #ButtonMain").removeClass(expandedMainMenuClassName).addClass(collapsedMainMenuClassName);
                $("#Content").removeClass(collapsedContentClassName).addClass(expandedContentClassName);
            } else{
                //expanded
                //nothing to do.
            }
        }

        $("#TopPanel #ButtonMain div").hover(function(){
            $(this).addClass("hover");
        }, function(){
            $(this).removeClass("hover");
        })
        $("#TopPanel #ButtonMain").click(function(){
            $("#LeftPanel").toggle("fast");
            if($(this).hasClass(expandedMainMenuClassName)){
                Complitex.Common.setCookie(Complitex.Common.MenuInfo.MAIN_MENU_COOKIE, Complitex.Common.MenuInfo.MAIN_MENU_COLLAPSED, "");
                $(this).removeClass(expandedMainMenuClassName).addClass(collapsedMainMenuClassName);
                $("#Content").removeClass(collapsedContentClassName).addClass(expandedContentClassName);
            } else {
                Complitex.Common.setCookie(Complitex.Common.MenuInfo.MAIN_MENU_COOKIE, Complitex.Common.MenuInfo.MAIN_MENU_EXPANDED, "");
                $(this).addClass(expandedMainMenuClassName).removeClass(collapsedMainMenuClassName);
                $("#Content").removeClass(expandedContentClassName).addClass(collapsedContentClassName);
            }
        });
    })();

    //selected menu item behaviour
    (function(){
        /* consts */
        var selectedMenuItemClassName = "SelectedMenuItem";

        $(".block li a").each(function(){
            var id = $(this).attr("id");
            if(id){
                $(this).click(function(){
                    Complitex.Common.setCookie(Complitex.Common.MenuInfo.SELECTED_MENU_ITEM_COOKIE, $(this).attr("id"), "");
                });
            }
        });

        var selecteMenuItemID = Complitex.Common.getCookie(Complitex.Common.MenuInfo.SELECTED_MENU_ITEM_COOKIE);
        if(selecteMenuItemID){
            $("#"+selecteMenuItemID).parent().addClass(selectedMenuItemClassName)
            // $("#"+selecteMenuItemID).focus();
            // $("#ButtonMain").focus();
        }
    })();
});