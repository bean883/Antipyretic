package cn.bingod.antipyretic.library;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import cn.bingod.antipyretic.Extra;
import cn.bingod.antipyretic.Flags;
import cn.bingod.antipyretic.ForResult;
import cn.bingod.antipyretic.Path;
import cn.bingod.antipyretic.Query;
import cn.bingod.antipyretic.RequestCode;
import cn.bingod.antipyretic.Transition;

/**
 * @author bin
 * @since 2017/7/10
 */
public final class Antipyretic {

    private final static String TAG = "Antipyretic";

    private final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
    private final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");

    private static Antipyretic instance;

    private static final Map<Class<?>, Constructor> INJECTING_MAP = new LinkedHashMap<>();
    private ArrayList<Interceptor> interceptors = new ArrayList<>();
    private Map<String, String> map;

    private Config cfg;

    public static synchronized Antipyretic get() {
        if (instance == null) {
            instance = new Antipyretic();
        }
        return instance;
    }

    public Antipyretic config (Config cfg) {
        this.cfg = cfg;
        return this;
    }

    public Antipyretic add (Map<String, String> map) {
        if (instance.map == null)
            instance.map = new HashMap<>();
        instance.map.putAll(map);
        return this;
    }

    public void init (Map<String, String> map) {
        if (instance.map == null)
            instance.map = new HashMap<>();
        instance.map.putAll(map);
    }

    public Antipyretic addInterceptor(Interceptor interceptor) {
        if (interceptors == null) interceptors = new ArrayList<>();
        interceptors.add(interceptor);
        return this;
    }

    public Antipyretic removeInterceptor(Interceptor interceptor) {
        if (interceptors != null) interceptors.remove(interceptor);
        return this;
    }

    public static boolean loadUrl(String url, Context context) {
        if (!TextUtils.isEmpty(url) && context != null) {
            Uri uri = Uri.parse(url);
            return load(context, uri, uri, null, null, null, 0, null, null);
        }
        Log.e(TAG, "没有找到跳转页面");
        return false;
    }

    public static void bind(Object target) {
        if (target instanceof Activity || target instanceof Fragment) {
            Constructor constructor = findInjectorForClass(target.getClass());
            if (constructor != null) {
                try {
                    constructor.newInstance(target);
                } catch (InstantiationException e) {
                    throw new RuntimeException("Unable to invoke " + constructor, e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unable to invoke " + constructor, e);
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof RuntimeException) {
                        throw (RuntimeException) cause;
                    }
                    if (cause instanceof Error) {
                        throw (Error) cause;
                    }
                    throw new RuntimeException("Unable to create injecting instance.", cause);
                }
            }
        }else {
            throw new IllegalArgumentException("only support activity & fragment");
        }
    }

    private static Constructor findInjectorForClass(Class<?> targetClass) {
        Constructor constructor = INJECTING_MAP.get(targetClass);
        if (constructor != null) {
            return constructor;
        }

        String className = targetClass.getName();
        try {
            Class<?> injectingClass = Class.forName(className + Protocols.SUFFIX);
            constructor = injectingClass.getConstructor(targetClass);
        } catch (NoSuchMethodException e) {
            constructor = findInjectorForClass(targetClass.getSuperclass());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        INJECTING_MAP.put(targetClass, constructor);
        return constructor;
    }

    public <T> T create(final Class<T> service, final Fragment fragment) {
        validateServiceInterface(service);

        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                cn.bingod.antipyretic.Uri routingUri = method.getAnnotation(cn.bingod.antipyretic.Uri.class);

                Uri uri = Uri.parse(routingUri.value());
                uri = Uri.parse(wrapUri(uri));
                Annotation[][] parameterAnnotations = method.getParameterAnnotations();

                Map<String, Object> params = new ArrayMap<>();
                Map<String, Object> uriPathParams = new ArrayMap<>();
                for (int i = 0; i < parameterAnnotations.length; i++) {
                    Annotation[] annotations = parameterAnnotations[i];
                    if (annotations != null && annotations.length > 0) {
                        for (int j = 0; j < annotations.length; j++) {
                            Annotation annotation = annotations[j];
                            Log.d(TAG, "invoke: " + annotation);
                            if (annotation instanceof Path) {
                                Path rp = (Path) annotation;
                                uri = uri.buildUpon().path(uri.getPath()
                                        .replaceFirst(PARAM_URL_REGEX.pattern(), String.valueOf(args[i])))
                                        .build();
                                uriPathParams.put(rp.value(), args[i]);
                            } else if (annotation instanceof Query) {
                                Query rq = (Query) annotation;
                                Uri.Builder builder = uri.buildUpon();
                                builder.appendQueryParameter(rq.value(), String.valueOf(args[i]));
                                uri = builder.build();
                            } else if (annotation instanceof Extra) {
                                Extra rb = (Extra) annotation;
                                params.put(rb.value(), args[i]);
                            }
                        }
                    }
                }
                if (interceptors != null) {
                    for (Interceptor interceptor : interceptors) {
                        if (interceptor.intercept(uri)) return false;
                    }
                }
                Bundle bundle = params2Bundle(uriPathParams);
                if(bundle == null) bundle = new Bundle();
                Bundle extra = params2Bundle(params);
                bundle.putBundle(Protocols.BUNDLE, extra);
                bundle.putParcelable(Protocols.URI, uri);
                fragment.setArguments(bundle);
                return fragment;
            }
        });
    }

    public <T> T create(final Class<T> service, final Context context) {
        return create(service, context, null);
    }

    public <T> T create(final Class<T> service, final Context context, final Bundle options) {
        validateServiceInterface(service);
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                cn.bingod.antipyretic.Uri routingUri = method.getAnnotation(cn.bingod.antipyretic.Uri.class);
                Transition routingTransition = method.getAnnotation(Transition.class);
                ForResult routingForResult = method.getAnnotation(ForResult.class);
                Flags flag = method.getAnnotation(Flags.class);

                Uri uri = Uri.parse(routingUri.value());
                Uri originUri = uri = Uri.parse(wrapUri(uri));
                Annotation[][] parameterAnnotations = method.getParameterAnnotations();

                Map<String, Object> params = new ArrayMap<>();
                Map<String, Object> uriPathParams = new ArrayMap<>();
                int mRequestCode = 0;
                int[] flags = null;
                if (flag != null) flags = flag.value();
                for (int i = 0; i < parameterAnnotations.length; i++) {
                    Annotation[] annotations = parameterAnnotations[i];
                    if (annotations != null && annotations.length > 0) {
                        for (int j = 0; j < annotations.length; j++) {
                            Annotation annotation = annotations[j];
                            if (annotation instanceof Path) {
                                Path rp = (Path) annotation;
                                uri = uri.buildUpon().path(uri.getPath()
                                        .replaceFirst(PARAM_URL_REGEX.pattern(), String.valueOf(args[i])))
                                        .build();
                                uriPathParams.put(rp.value(), args[i]);
                            } else if (annotation instanceof Query) {
                                Query rq = (Query) annotation;
                                Uri.Builder builder = uri.buildUpon();
                                builder.appendQueryParameter(rq.value(), String.valueOf(args[i]));
                                uri = builder.build();
                            } else if (annotation instanceof Extra) {
                                Extra rb = (Extra) annotation;
                                params.put(rb.value(), args[i]);
                            } else if (annotation instanceof RequestCode) {
                                RequestCode rc = (RequestCode) annotation;
                                mRequestCode = (int) args[i];
                            }
                        }
                    }
                }
                if (interceptors != null) {
                    for (Interceptor routingInterceptor : interceptors) {
                        if (routingInterceptor.intercept(uri)) return false;
                    }
                }
                Log.d(TAG, "invoke: " + uri.toString());

                if (mRequestCode != 0)
                    return load(context, uri, originUri, uriPathParams, params, routingTransition, mRequestCode, options, flags);
                else
                    return load(context, uri, originUri, uriPathParams, params, routingTransition, routingForResult == null ? 0 : routingForResult.requestCode(), options, flags);

            }
        });
    }

    private static boolean load(Context context, Uri uri, Uri originUri, Map<String, Object> uriPathParams, Map<String, Object> params, Transition routingTransition, int requestCode, Bundle options, int[] flags) {
        Intent intent = new Intent();
        if(flags != null && flags.length > 0) {
            for (int flag : flags) {
                if (flag != 0) intent.addFlags(flag);
            }
        }
        Bundle bundle = params2Bundle(params);
        params2Intent(intent, uriPathParams);

        if (bundle != null) intent.putExtra(Protocols.BUNDLE, bundle);
        String host = uri.getHost();
        if(host != null && host.startsWith(instance.cfg.intentAction)){
            intent.setAction(host);
        }else {
            intent.setData(uri);
        }
        intent.setPackage(context.getPackageName());
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        if (!activities.isEmpty()) {
            startActivity(context, routingTransition, requestCode, intent, options);
            return true;
        } else {//去路由表找一下如果还是没有那就没有
            String cleanUri = wrapUri(originUri);
            ComponentName componentName = getComponentName(cleanUri);
            if (componentName != null) {
                intent.setPackage(null);
                intent.setComponent(componentName);
                try {
                    startActivity(context, routingTransition, requestCode, intent, options);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.e(TAG, "没有找到跳转页面");
            return false;
        }
    }

    private static ComponentName getComponentName(String uri) {
        if(uri == null) return null;
        if(instance.map == null) return null;
        String name = instance.map.get(uri);
        if(name == null) {
            Set<Map.Entry<String, String>> set = instance.map.entrySet();
            for (Map.Entry<String, String> entry : set) {
                if(uri.startsWith(entry.getKey()) || uri.contains(entry.getKey()))
                    return new ComponentName(instance.cfg.packageName, entry.getValue());
            }
        }
        return null;
    }

    private static String wrapUri(Uri uri) {
        Uri u = uri.normalizeScheme();
        String scheme = u.getScheme();
        String host = u.getHost();
        String path = u.getPath();
        if (scheme == null || scheme.isEmpty()) scheme = instance.cfg.scheme;
        if (host == null || host.isEmpty()) host = instance.cfg.host;
        if (path != null && !path.startsWith("/")) host = "";
        return scheme + "://" + host + path;
    }

    private static void startActivity(Context context, Transition routingTransition, int requestCode, Intent intent, Bundle options) {
        if (context instanceof Activity && requestCode != 0) {
            ((Activity) context).startActivityForResult(intent, requestCode, options);
        } else {
            ContextCompat.startActivity(context, intent, options);
        }
        if (context instanceof Activity && routingTransition != null) {
            ((Activity) context).overridePendingTransition(routingTransition.enterAnim(), routingTransition.exitAnim());
        }
    }

    public static Intent params2Intent(Intent intent, Map<String, Object> params) {
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = "path_"+entry.getKey();
                Object value = entry.getValue();
                if(value != null) {
                    if (value instanceof Integer) {
                        intent.putExtra(key, Integer.parseInt(value.toString()));
                    } else if (value instanceof Boolean) {
                        intent.putExtra(key, Boolean.parseBoolean(value.toString()));
                    } else if (value instanceof Byte) {
                        intent.putExtra(key, Byte.parseByte(value.toString()));
                    } else if (value instanceof Long) {
                        intent.putExtra(key, Long.parseLong(value.toString()));
                    } else if (value instanceof Double) {
                        intent.putExtra(key, Double.parseDouble(value.toString()));
                    } else if (value instanceof Short) {
                        intent.putExtra(key, Short.parseShort(value.toString()));
                    } else if (value instanceof Float) {
                        intent.putExtra(key, Float.parseFloat(value.toString()));
                    } else if (value instanceof String) {
                        intent.putExtra(key, (String) value);
                    } else if (value instanceof CharSequence) {
                        intent.putExtra(key, (CharSequence) value);
                    } else if (value.getClass().isArray()) {
                        if (int[].class.isInstance(value)) {
                            intent.putExtra(key, (int[]) value);
                        } else if (long[].class.isInstance(value)) {
                            intent.putExtra(key, (long[]) value);
                        } else if (double[].class.isInstance(value)) {
                            intent.putExtra(key, (double[]) value);
                        } else if (short[].class.isInstance(value)) {
                            intent.putExtra(key, (short[]) value);
                        } else if (float[].class.isInstance(value)) {
                            intent.putExtra(key, (float[]) value);
                        } else if (String[].class.isInstance(value)) {
                            intent.putExtra(key, (String[]) value);
                        } else if (CharSequence[].class.isInstance(value)) {
                            intent.putExtra(key, (CharSequence[]) value);
                        } else if (Parcelable[].class.isInstance(value)) {
                            intent.putExtra(key, (Parcelable[]) value);
                        }
                    } else if (value instanceof ArrayList && !((ArrayList) value).isEmpty()) {
                        ArrayList list = (ArrayList) value;
                        if (list.get(0) instanceof Integer) {
                            intent.putIntegerArrayListExtra(key, (ArrayList<Integer>) value);
                        } else if (list.get(0) instanceof String) {
                            intent.putStringArrayListExtra(key, (ArrayList<String>) value);
                        } else if (list.get(0) instanceof CharSequence) {
                            intent.putCharSequenceArrayListExtra(key, (ArrayList<CharSequence>) value);
                        } else if (list.get(0) instanceof Parcelable) {
                            intent.putParcelableArrayListExtra(key, (ArrayList<? extends Parcelable>) value);
                        }
                    } else if (value instanceof Parcelable) {
                        intent.putExtra(key, (Parcelable) value);
                    } else if (value instanceof Serializable) {
                        intent.putExtra(key, (Serializable) value);
                    } else {
                        throw new IllegalArgumentException("不支持的参数类型！");
                    }
                }
            }
        }
        return intent;
    }

    public static Bundle params2Bundle(Map<String, Object> params) {
        if (params != null && params.size() > 0) {
            Bundle bundle = new Bundle();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = "extra_"+entry.getKey();
                Object value = entry.getValue();
                if(value != null) {
                    if (value instanceof Integer) {
                        bundle.putInt(key, Integer.parseInt(value.toString()));
                    } else if (value instanceof Boolean) {
                        bundle.putBoolean(key, Boolean.parseBoolean(value.toString()));
                    } else if (value instanceof Byte) {
                        bundle.putByte(key, Byte.parseByte(value.toString()));
                    } else if (value instanceof Long) {
                        bundle.putLong(key, Long.parseLong(value.toString()));
                    } else if (value instanceof Double) {
                        bundle.putDouble(key, Double.parseDouble(value.toString()));
                    } else if (value instanceof Short) {
                        bundle.putShort(key, Short.parseShort(value.toString()));
                    } else if (value instanceof Float) {
                        bundle.putFloat(key, Float.parseFloat(value.toString()));
                    } else if (value instanceof String) {
                        bundle.putString(key, (String) value);
                    } else if (value instanceof CharSequence) {
                        bundle.putCharSequence(key, (CharSequence) value);
                    } else if (value.getClass().isArray()) {
                        if (int[].class.isInstance(value)) {
                            bundle.putIntArray(key, (int[]) value);
                        } else if (long[].class.isInstance(value)) {
                            bundle.putLongArray(key, (long[]) value);
                        } else if (double[].class.isInstance(value)) {
                            bundle.putDoubleArray(key, (double[]) value);
                        } else if (short[].class.isInstance(value)) {
                            bundle.putShortArray(key, (short[]) value);
                        } else if (float[].class.isInstance(value)) {
                            bundle.putFloatArray(key, (float[]) value);
                        } else if (String[].class.isInstance(value)) {
                            bundle.putStringArray(key, (String[]) value);
                        } else if (CharSequence[].class.isInstance(value)) {
                            bundle.putCharSequenceArray(key, (CharSequence[]) value);
                        } else if (Parcelable[].class.isInstance(value)) {
                            bundle.putParcelableArray(key, (Parcelable[]) value);
                        }
                    } else if (value instanceof ArrayList && !((ArrayList) value).isEmpty()) {
                        ArrayList list = (ArrayList) value;
                        if (list.get(0) instanceof Integer) {
                            bundle.putIntegerArrayList(key, (ArrayList<Integer>) value);
                        } else if (list.get(0) instanceof String) {
                            bundle.putStringArrayList(key, (ArrayList<String>) value);
                        } else if (list.get(0) instanceof CharSequence) {
                            bundle.putCharSequenceArrayList(key, (ArrayList<CharSequence>) value);
                        } else if (list.get(0) instanceof Parcelable) {
                            bundle.putParcelableArrayList(key, (ArrayList<? extends Parcelable>) value);
                        }
                    } else if (value instanceof Parcelable) {
                        bundle.putParcelable(key, (Parcelable) value);
                    } else if (value instanceof Serializable) {
                        bundle.putSerializable(key, (Serializable) value);
                    } else {
                        throw new IllegalArgumentException("不支持的参数类型！");
                    }
                }
            }
            return bundle;
        }
        return null;
    }

    private static <T> void validateServiceInterface(Class<T> service) {
        if (!service.isInterface()) {
            throw new IllegalArgumentException("API declarations must be interfaces.");
        }
        if (service.getInterfaces().length > 0) {
            throw new IllegalArgumentException("API interfaces must not extend other interfaces.");
        }
    }

    public static class Config {
        public String packageName;
        public String intentAction = "android.intent.action";
        public String host = "";
        public String scheme = "intent";
    }

    public interface Interceptor {
        boolean intercept(Uri uri);
    }

    public interface Protocols {
        String BUNDLE = "Antipyretic_Bundle";
        String URI = "Antipyretic_Uri";
        String SUFFIX = "_Binder";
    }

    public interface Types {
        int Query = 0;
        int Path = 1;
        int Extra = 2;
    }
}
