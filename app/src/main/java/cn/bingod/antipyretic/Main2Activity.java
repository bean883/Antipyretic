package cn.bingod.antipyretic;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

import cn.bingod.antipyretic.library.Antipyretic;

@Table(value = "/main2")
public class Main2Activity extends AppCompatActivity {

    @Param
    String p;

    @Param(type = Antipyretic.Types.Path)
    String id;

    @Param(type = Antipyretic.Types.Extra)
    String e;
    @Param(type = Antipyretic.Types.Extra)
    String[] ee;
    @Param(type = Antipyretic.Types.Extra)
    int a;
    @Param(type = Antipyretic.Types.Extra)
    boolean b;
    @Param(type = Antipyretic.Types.Extra)
    float c;
    @Param(type = Antipyretic.Types.Extra)
    long d;
    @Param(type = Antipyretic.Types.Extra)
    double f;
    @Param(type = Antipyretic.Types.Extra)
    byte g;
    @Param(type = Antipyretic.Types.Extra)
    short h;
    @Param(type = Antipyretic.Types.Extra)
    Integer w;
    @Param(type = Antipyretic.Types.Extra)
    CharSequence r;
    @Param(type = Antipyretic.Types.Extra)
    int[] q;
    @Param(type = Antipyretic.Types.Extra)
    boolean[] bb1;
    @Param(type = Antipyretic.Types.Extra)
    short[] qq1;
    @Param(type = Antipyretic.Types.Extra)
    long[] q11;
    @Param(type = Antipyretic.Types.Extra)
    byte[] qq2;
    @Param(type = Antipyretic.Types.Extra)
    float[] qq3;
    @Param(type = Antipyretic.Types.Extra)
    double[] qq4;
    @Param(type = Antipyretic.Types.Extra)
    ArrayList<Integer> t;
    @Param(type = Antipyretic.Types.Extra)
    ArrayList<String> tt1;
    @Param(type = Antipyretic.Types.Extra)
    Point point;
    @Param(type = Antipyretic.Types.Extra)
    ArrayList<Point> pointsList;
    @Param(type = Antipyretic.Types.Extra)
    TestObj testObj;

    @Param(type = Antipyretic.Types.Extra)
    Parcelable[] pps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Antipyretic.bind(this);

        //Toast.makeText(this, "p=" + p + ", id=" + id, Toast.LENGTH_SHORT).show();

        Log.d("---", "onCreate: " + p);
        Log.d("---", "onCreate: " + id);
        Log.d("---", "onCreate: " + e);
        Log.d("---", "onCreate: " + ee);
        Log.d("---", "onCreate: " + a);
        Log.d("---", "onCreate: " + b);
        Log.d("---", "onCreate: " + c);
        Log.d("---", "onCreate: " + d);
        Log.d("---", "onCreate: " + f);
        Log.d("---", "onCreate: " + g);
        Log.d("---", "onCreate: " + h);
        Log.d("---", "onCreate: " + w);
        Log.d("---", "onCreate: " + r);
        Log.d("---", "onCreate: " + q);
        Log.d("---", "onCreate: " + bb1);
        Log.d("---", "onCreate: " + qq1);
        Log.d("---", "onCreate: " + q11);
        Log.d("---", "onCreate: " + qq2);
        Log.d("---", "onCreate: " + qq3);
        Log.d("---", "onCreate: " + qq4);
        Log.d("---", "onCreate: " + t);
        Log.d("---", "onCreate: " + tt1);
        Log.d("---", "onCreate: " + point);
        Log.d("---", "onCreate: " + pointsList);
        Log.d("---", "onCreate: " + pointsList);
        Log.d("---", "onCreate: " + testObj);

    }
}
