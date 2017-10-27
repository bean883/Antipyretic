package cn.bingod.antipyretic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import cn.bingod.antipyretic.library.Antipyretic;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Antipyretic.Config cfg = new Antipyretic.Config();
        cfg.intentAction = BuildConfig.APPLICATION_ID + ".action";
        //cfg.packageName = "cn.bingod.example";
        cfg.packageName = getPackageName();
        Antipyretic.get().config(cfg).init(RoutingMap.get());


        Antipyretic.Interceptor interceptor = new Antipyretic.Interceptor() {
            @Override
            public boolean intercept(Uri uri) {
                if(uri.getHost().startsWith(BuildConfig.APPLICATION_ID + ".action")) {
                    Toast.makeText(MainActivity.this, "再点一下试试", Toast.LENGTH_SHORT).show();
                    Antipyretic.get().removeInterceptor(this);
                    return true;
                }
                Toast.makeText(MainActivity.this, uri.toString(), Toast.LENGTH_SHORT).show();
                return false;
            }
        };
        Antipyretic.get().addInterceptor(interceptor);
    }

    public void jump(View view) {
        Antipyretic.get().create(MyServices.class, this, null).main22("11", "1111");
    }

    public void forResult(View view) {
        Antipyretic.get().create(MyServices.class, this).openTestForResult(new TestObj("666"),110);
    }

    public void bundle(View view) {
        Antipyretic.get().create(MyServices.class, this).openTestWithAnim("12345", "额外的", new TestObj("666"));
    }

    public void toWeb(View view) {
        boolean r=Antipyretic.get().create(MyServices.class, this).toWeb("file:///android_asset/demo.html");
        Toast.makeText(this, r?"跳转成功":"跳转失败", Toast.LENGTH_SHORT).show();
    }

    public void customService(View view) {
        /*ImageView img = (ImageView) findViewById(R.id.imageView);
        ActivityOptionsCompat aoc = ActivityOptionsCompat.makeSceneTransitionAnimation(this, img, "img");
        Antipyretic.get().create(MyServices.class, this, aoc.toBundle()).anim();*/
        Antipyretic.get().create(MyServices.class, this).main4("123", new TestObj("666"));
    }

    public void toFragment(View view) {
        BlankFragment fragment = Antipyretic.get().create(MyServices.class, new BlankFragment()).toBlank("123", "abc", new TestObj("^_^"));
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Toast.makeText(this.getApplicationContext(), "reqcode=" + requestCode, Toast.LENGTH_SHORT).show();
    }
}
