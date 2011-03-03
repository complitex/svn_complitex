
//menus
var expandedMenuClassName = "expanded";
var collapsedMenuClassName = "collapsed";

var menuCookiePrefix = "MenuPrefix_";

menuLoad = function(){
    $(".block div").each(function(){
        var t = getCookie(menuCookiePrefix+$(this).attr("id"))
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
            setCookie(menuCookiePrefix+$(this).parent().attr("id"), "0", "");
            $(this).removeClass(expandedMenuClassName).addClass(collapsedMenuClassName);
        //            $(this).closest(".LeftBlock").addClass("LeftBlockOff").removeClass("LeftBlock");
        } else {
            setCookie(menuCookiePrefix+$(this).parent().attr("id"), "1", "");
            $(this).removeClass(collapsedMenuClassName).addClass(expandedMenuClassName);
        //            $(this).closest(".LeftBlockOff").addClass("LeftBlock").removeClass("LeftBlockOff");
        }
    });
};

//selected menu item behaviour
var selectedMenuItemCookieName = "SelectedMenuItem";
var selectedMenuItemClassName = "SelectedMenuItem";

selectedMenuLoad = function(){
    $(".block li a").each(function(){
        var id = $(this).attr("id");
        if(id){
            $(this).click(function(){
                setCookie(selectedMenuItemCookieName, $(this).attr("id"), "");
            });
        }
    });

    var selecteMenuItemID = getCookie(selectedMenuItemCookieName);
    if(selecteMenuItemID){
        $("#"+selecteMenuItemID).parent().addClass(selectedMenuItemClassName)
//        $("#"+selecteMenuItemID).focus();
//        $("#ButtonMain").focus();
    }
};

//main information panel and content panel

var expandedContentClassName = "ContentExpanded";
var collapsedContentClassName = "ContentCollapsed";
var expandedMainMenuClassName = "expanded";
var collapsedMainMenuClassName = "collapsed";

var mainMenuCookieName = "MainMenuCookie";

mainPanelLoad = function(){
    var t = getCookie(mainMenuCookieName);
    //t = 1 - expanded, t = 0 - collapsed
    if(t){
        if(t == 0){
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
            setCookie(mainMenuCookieName, "0", "");
            $(this).removeClass(expandedMainMenuClassName).addClass(collapsedMainMenuClassName);
            $("#Content").removeClass(collapsedContentClassName).addClass(expandedContentClassName);
        } else {
            setCookie(mainMenuCookieName, "1", "");
            $(this).addClass(expandedMainMenuClassName).removeClass(collapsedMainMenuClassName);
            $("#Content").removeClass(expandedContentClassName).addClass(collapsedContentClassName);
        }
    });
};

$(document).ready(function(){
    menuLoad();
    mainPanelLoad();
    selectedMenuLoad();
});