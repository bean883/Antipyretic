## 路由框架设计

#### 路由跳转只做页面跳转，不管是activity还是fragment还是view

##### 1. 网页跳转原生页面
期望实现：

`
Router.loadUrl("scheme://host/path", context);
`
##### 2. 原生页面跳转原生页面
期望实现：

```
public interface TestRouter {

    @RoutingUri("mgapp://www.mgzf.com/test/group/{gid}/room/{cid}")
    Router openTest(@RoutingPath("gid") String a,
                        @RoutingPath("cid") String b,
                        @RoutingQuery("type") String c,
                        @RoutingQuery("score") String d);

    @RoutingUri("mgapp://www.mgzf.com/test/room")
    @RoutingTransition(enterAnim = R.anim.activity_open_in_bottom, exitAnim = R.anim.activity_open_out_bottom)
    Router openTestWithAnim(@RoutingQuery("id") String a,
                            @RoutingExtra("extra") String b,
                            @RoutingExtra("testObj") TestObj obj);

    @RoutingUri("mgapp://www.mgzf.com/test")
    @RoutingForResult(requestCode = 119)
    void openTestForResult();
}
```


```
Router.getInstance().create(Service.class, context).loadPage(Object... params);
```

Router.getInstance().create(Service.class, fragment).open();


##### uri默认会去查找manifest里注册的，如果找不到就去路由表RoutingTable（自动生成的一个类）查找对应的页面
##### 传递的参数、路由表 由 编译时注解 自动注入