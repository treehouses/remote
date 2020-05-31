package io.treehouses.remote.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Locale;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;

public class ViewHolderWifiCountry {

    private TextInputEditText editText;
    private BluetoothChatService mChatService;
    private Context c;

    ViewHolderWifiCountry(View v, Context context, HomeInteractListener listener) {
        String[] countriesCode = Locale.getISOCountries();
        String[] countriesName = new String[countriesCode.length];
        for (int i = 0; i < countriesCode.length; i++) {
            Locale l = new Locale("",countriesCode[i]);
            String countryName = l.getDisplayCountry();
            countriesName[i] = countryName +" (" +countriesCode[i] + ")";
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (context,android.R.layout.select_dialog_item,countriesName);


        c = context;
        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);

       editText = v.findViewById(R.id.editText22);
       editText.setOnClickListener(v3->{
           AlertDialog.Builder builder = new AlertDialog.Builder(context);
           builder.setTitle("Choose a country");
           String[] animals = {"horse", "cow", "camel", "sheep", "goat"};

           EditText myEditText = new EditText(context);
            builder.setCustomTitle(myEditText);

           builder.setItems(animals, new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                   switch (which) {
                       case 0: // horse
                       case 1: // cow
                       case 2: // camel
                       case 3: // sheep
                       case 4: // goat
                   }
               }
           });
           AlertDialog dialog = builder.create();

           dialog.show();
       });
//        editTextSSHKey.setEnabled(false);


    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MESSAGE_READ) {
                String readMessage = (String) msg.obj;
                if (readMessage.contains("Added to 'pi' and 'root' user's authorized_keys")){
                    Toast.makeText(c, "Added to 'pi' and 'root' user's authorized_keys", Toast.LENGTH_LONG).show();
                }


            }
        }
    };
}
