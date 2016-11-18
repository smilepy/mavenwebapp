$(function(){
  /* 左侧展开收起 */
  $(".nav-list dt").on("click",function(){
    if (!$(this).hasClass('nav-list-active')) {
      $(this).addClass('nav-list-active');
      $(this).closest("dl").find('dd').show();
      $(this).closest("dl").find('dd').each(function () {
        $(this).click(function (i) {
          $(this).addClass("dd-select").siblings("dd").removeClass("dd-select");
        })
      })
    } else {
      $(this).removeClass('nav-list-active');
      $(this).closest("dl").find('dd').hide();
    }
  });

  var nav = $(".nav-item");
  nav.hover(function(){
    $(this).find(".dropdown-menu").show();
  },function(){
    $(this).find(".dropdown-menu").hide();
  });

  /* 地区 */
  $(".zone-switcher-1").on("click",function(){
    if(!$(this).hasClass("zone-active")){
      $(this).addClass("zone-active");
      $(this).find(".zone-switcher-select").show();
      var zone_val;
      var text = $(".zone-text");
      $(".zone-switcher-select li").each(function(){
        $(this).on("click",function(){
          $(this).find(".label-unchecked").addClass("label-current");
          $(this).siblings().find(".label-unchecked").removeClass("label-current");

          zone_val =  $(this).find(".label-text").text();
          text.text(zone_val);
        })
      })
    }else{
      $(this).removeClass("zone-active");
      $(this).find(".zone-switcher-select").hide();
    }
  });

  /* 弹出层 */
  $("#create").on("click",function(){
    $(".window-overlay").show();
    $(".modal").show();
  });
  $(".close").on("click",function(){
    $(".window-overlay").hide();
    $(".modal").hide();
  });

  /* tab */
  var  list_li = $(".page-tabs li");
  var  change_div = $(".tab-list .pane") ;
  list_li.on('click',function() {
    var index = $(this).index();
    $(this).addClass("selected").siblings("li").removeClass("selected");
    change_div.eq(index).show().siblings("div").hide();
  });
});/**
 * Created by Administrator on 2016/4/28.
 */
