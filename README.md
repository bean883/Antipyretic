# Antipyretic
页面跳转框架，框架设计参见wiki

### 使用(支持activity跳转和fragment数据绑定，fragment需手动跳转)
#### 初始化(在Application中)
只有一个Module的情况，只需初始化一个路由表
```
Antipyretic.Config cfg = new Antipyretic.Config();
//cfg.intentAction = BuildConfig.APPLICATION_ID + ".intent.action";(可以配置action隐式跳转)
cfg.host = "xxx";
cfg.packageName = getPackageName();
Antipyretic.get().config(cfg).init(RoutingMap.get());
```
如果是多Module，可以调用add添加不同的路由表（RoutingMap_moduleName中的后缀替换成自己的Module名）
```
Antipyretic.get().config(cfg).add(RoutingMap_moduleName.get());
```
##### 1. 网页跳转原生页面

`
Antipyretic.loadUrl(url, context);
`
##### 2. 原生页面跳转原生页面

###### 先定义跳转接口

```
public interface TestRouter {
    /**
     * 打开测试页面
     * @param a groupId
     * @param b RoomId
     * @param c type
     * @param d score
     */
    @Uri("app://www.xxx.com/test/group/{gid}/room/{rid}")
    void openTest(@Path("gid") String a,
                    @Path("rid") String b,
                    @Query("type") String c,
                    @Query("score") String d);

    /**
     * 打开测试页面,动画从下往上
     * @param id ID
     * @param extra extra
     * @param obj object
     */
    @Uri("app://xxx/test/room")
    @Transition(enterAnim = R.anim.activity_open_in_bottom, exitAnim = R.anim.activity_open_out_bottom)
    void openTestWithAnim(@Query("id") String id, @Extra("extra") String extra, @Extra("testObj") TestObj obj);

    @Uri("http://www.xxx.com/test")
    @ForResult(requestCode = 119)
    void openTestForResult();

    @Uri("app://web")
    boolean toWeb(@Query("url") String url);

    @Uri("/test")
    void anim();

    @Uri("/blank/{id}")
    BlankFragment toBlank(@Path("id") String a, @Query("param") String b, @Extra("extra") TestObj obj);
}
```
###### 再定义activity URI 匹配规则(在activity上用@Table标注匹配路径或者在manifest.xml中注册)
```
@Table("/test")
public class TestActivity extends AppCompatActivity {
    ...
}
```
**@Table中的uri与上面interface中的uri有一定的区别，interface中的uri是跳转规则，@Table中的uri是匹配规则，如果这个uri不写全，比如没有scheme、host，则会匹配上跳转规则中的path一致的action，忽略掉scheme和host**

如果不写@Table注解，则需要去manifest.xml中IntentFilter配置scheme、host、path、pathPrefix等

###### 跳转代码
**跳activity**
```
Antipyretic.get().create(TestRouter.class, this).openTest("8", "119", "activity", "666");
```
**跳Fragment（把Fragment对象传给路由处理，参数绑定和activity一致，标注注解和调用bind方法即可）**
```
BlankFragment fragment = Antipyretic.get().create(TestRouter.class, new BlankFragment()).toBlank("123", "abc", new TestObj("^_^"));
FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
fragmentTransaction.replace(R.id.main, fragment);
fragmentTransaction.commit();
```

###### 自动注入参数
**在oncreate方法中调用bind方法即可**
```
Antipyretic.bind(this);
```
**注入的参数需要使用@Param标注，需要与最先定义的接口参数保持一致**

**@Param中的type类型分别对应uri path中占位符{}、?key=value、bundle支持的对象传值**

###### 全局拦截器使用（返回值决定是否拦截跳转，如果单个页面有特殊处理，处理完或者页面关掉的时候务必移除拦截器）
```
Antipyretic.Interceptor interceptor = new Antipyretic.Interceptor() {
    @Override
    public boolean intercept(Uri uri) {
        Toast.makeText(MainActivity.this, "uri=" + uri.toString(), Toast.LENGTH_SHORT).show();
        return false;
    }
}
Antipyretic.get().addInterceptor(interceptor);

Antipyretic.get().removeInterceptor(interceptor);
```
###### 跳转结果
**跳转接口里是有一个boolean返回值的，没找到页面跳转会返回false，可以做一些失败操作**
