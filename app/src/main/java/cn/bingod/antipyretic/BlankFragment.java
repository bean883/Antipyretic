package cn.bingod.antipyretic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.bingod.antipyretic.library.Antipyretic;


@Table("/blank")
public class BlankFragment extends Fragment {
    private static final String ARG_PARAM1 = "id";
    private static final String ARG_PARAM2 = "param";
    private static final String ARG_PARAM3 = "extra";

    @Param(value = ARG_PARAM1, type = Antipyretic.Types.Path)
    String mParam1;
    @Param(ARG_PARAM2)
    String mParam2;
    @Param(value = ARG_PARAM3, type = Antipyretic.Types.Extra)
    TestObj mParam3;

    public BlankFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Antipyretic.bind(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tv = (TextView) view.findViewById(R.id.tv);
        tv.setText("id=" + mParam1 + "\n"
                + "param=" + mParam2+ "\n"
                + "obj=" + mParam3);
    }


}
