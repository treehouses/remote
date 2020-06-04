package io.treehouses.remote.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.ListView;
import android.widget.SearchView;
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

public class ViewHolderWifiCountry implements SearchView.OnQueryTextListener{

    private TextInputEditText textBar;
    private BluetoothChatService mChatService;
    private Context c;
    ListView countryList;
    SearchView searchView;

    ViewHolderWifiCountry(View v, Context context, HomeInteractListener listener)  {
        listener.sendMessage("treehouses wificountry");
        String[] countriesCode = Locale.getISOCountries();
        String[] countriesName = new String[countriesCode.length];
        for (int i = 0; i < countriesCode.length; i++) {
            countriesName[i] = getCountryName(countriesCode[i]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (context,android.R.layout.select_dialog_item,countriesName);
        c = context;
        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);

        textBar= v.findViewById(R.id.country_display);
        textBar.setEnabled(false);
        textBar.setOnClickListener(v3-> {
                 final Dialog dialog = new Dialog(context);
                 dialog.setContentView(R.layout.dialog_wificountry);
                 countryList = dialog.findViewById(R.id.countries);
                 adapter.getFilter().filter("");
                 countryList.setAdapter(adapter);
                 countryList.setTextFilterEnabled(true);
                 countryList.setOnItemClickListener((a,v2,p,id)->{
                     String selectedString = countryList.getItemAtPosition(p).toString();
                     selectedString = selectedString.substring(selectedString.length()-4,selectedString.length()-2);
                     listener.sendMessage("treehouses wificountry " +selectedString);
                     textBar.setEnabled(false);
                     textBar.setText("Changing country");
                     dialog.dismiss();
                 });
                 searchView = dialog.findViewById(R.id.search_bar);
                 searchView.setIconifiedByDefault(false);
                 searchView.setOnQueryTextListener(this);

                 dialog.show();
             });


    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MESSAGE_READ) {
                String readMessage = (String) msg.obj;
                if (readMessage.contains("country=") || readMessage.contains("set to")){
                    int len = readMessage.length()-3;
                    String country = readMessage.substring(len).trim();
                    textBar.setText(getCountryName(country));
                    textBar.setEnabled(true);
                }
                else if(readMessage.contains("Error when")){
                    textBar.setText("try again");
                    textBar.setEnabled(true);
                    Toast.makeText(c, "Error when changing country", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private String getCountryName(String country){
        Locale l = new Locale("",country);
        String countryName = l.getDisplayCountry();
        return countryName +" ( " +country + " )";
    }
    @Override
    public boolean onQueryTextChange(String newText)
    {

        if (TextUtils.isEmpty(newText)) {
            countryList.clearTextFilter();
        } else {
            countryList.setFilterText(newText);

        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        return false;
    }
}
