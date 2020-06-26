package io.treehouses.remote.adapter;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;

public class ViewHolderBlocker {

    private RadioGroup radioGroup;
    private RadioButton rbBtn1;
    private RadioButton rbBtn2;
    private RadioButton rbBtn3;
    private RadioButton rbBtn4;
    private RadioButton rbBtn5;
    private RadioButton rbBtn6;

    ViewHolderBlocker(View v, Context context, HomeInteractListener listener) {
        radioGroup = v.findViewById(R.id.radioGroup);
        rbBtn1 = v.findViewById(R.id.radioButton1);
        rbBtn2 = v.findViewById(R.id.radioButton2);
        rbBtn3 = v.findViewById(R.id.radioButton3);
        rbBtn4 = v.findViewById(R.id.radioButton4);
        rbBtn5 = v.findViewById(R.id.radioButton5);
        rbBtn6 = v.findViewById(R.id.radioButton6);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i){
                    case R.id.radioButton1:
                        Toast.makeText(context, "Disabled", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.radioButton2:
                        Toast.makeText(context, "Level 1", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.radioButton3:
                        Toast.makeText(context, "Level 2", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.radioButton4:
                        Toast.makeText(context, "Level 3", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.radioButton5:
                        Toast.makeText(context, "Level 4", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.radioButton6:
                        Toast.makeText(context, "Max", Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        });
    }
}
