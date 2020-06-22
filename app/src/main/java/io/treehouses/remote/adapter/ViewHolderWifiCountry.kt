package io.treehouses.remote.adapter

import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Handler
import android.os.Message
import android.renderscript.ScriptGroup
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.core.view.get
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.Constants
import io.treehouses.remote.Fragments.TerminalFragment
import io.treehouses.remote.MainApplication
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener
import io.treehouses.remote.databinding.ConfigureWificountryBinding
import io.treehouses.remote.databinding.DialogWificountryBinding
import io.treehouses.remote.utils.match
import kotlinx.android.synthetic.main.dialog_listview.view.*
import kotlinx.android.synthetic.main.dialog_wificountry.*
import java.util.*
import kotlin.math.log


class ViewHolderWifiCountry internal constructor(v: View, context: Context, listener: HomeInteractListener) : SearchView.OnQueryTextListener {
    private val mChatService: BluetoothChatService
    private val countryList: ListView? = null
    private val searchView: SearchView? = null
    private lateinit var binding: ConfigureWificountryBinding
    private lateinit var dialogBinding: DialogWificountryBinding

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            Log.d("new", msg.toString())
            when (msg.what) {
                Constants.MESSAGE_READ -> {
                    val readMessage = msg.obj as String
                    Log.d("mymessage", readMessage.contains("country=").toString())
                    if (readMessage.contains("country=")) {
                        val len = readMessage.length - 3
                        val country = readMessage.substring(len).trim { it <= ' ' }
                        binding.countryDisplay.setText(getCountryName(country))
                        binding.countryDisplay.isEnabled = true
                    }
                    else if(readMessage.contains("error")) {

                    }
                }

                }
//            if (msg.what == Constants.MESSAGE_READ) {
//                val readMessage = msg.obj as String
//
//                if (readMessage.contains("country=") || readMessage.contains("set to")) {
//                    val len = readMessage.length - 3
//                    val country = readMessage.substring(len).trim { it <= ' ' }
//                    binding.countryDisplay.setText(getCountryName(country))
//                    binding.countryDisplay.isEnabled = true
//                } else if (readMessage.contains("Error when")) {
//                    binding.countryDisplay.setText("try again")
//                    binding.countryDisplay.isEnabled = true
//                    Toast.makeText(context, "Error when changing country", Toast.LENGTH_LONG).show()
//                }
//            }
        }
    }


    private fun getCountryName(country: String): String {
        val l = Locale("", country)
        val countryName = l.displayCountry
        return "$countryName ( $country )"
    }

    override fun onQueryTextChange(newText: String): Boolean {
        if (TextUtils.isEmpty(newText)) {
            countryList!!.clearTextFilter()
        } else {
            countryList!!.setFilterText(newText)
        }
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    init {

        binding = ConfigureWificountryBinding.bind(v);
        listener.sendMessage("treehouses wificountry")
        val countriesCode = Locale.getISOCountries()
        val countriesName = arrayOfNulls<String>(countriesCode.size)
        for (i in countriesCode.indices) {
            countriesName[i] = getCountryName(countriesCode[i])
        }



        mChatService = listener.getChatService()
        mChatService.updateHandler(mHandler)


        binding.countryDisplay.isEnabled = false
            binding.countryDisplay.setOnClickListener { v3: View? ->
                Log.d("TAG", "my Message")
                val dialog = Dialog(context)
                dialog.setContentView(R.layout.dialog_wificountry)
                val adapter = ArrayAdapter(dialog.context, android.R.layout.select_dialog_item, countriesName)
            adapter.filter.filter("")
            dialogBinding = DialogWificountryBinding.inflate(LayoutInflater.from(dialog.context))

            dialogBinding.countries.adapter = ArrayAdapter(dialog.context, android.R.layout.select_dialog_item, countriesName)


               var control = dialog.findViewById<ListView>(R.id.countries)
                control.adapter = adapter
            dialogBinding.countries.isTextFilterEnabled = true
            dialogBinding.countries.onItemClickListener = OnItemClickListener { a: AdapterView<*>?, v2: View?, p: Int, id: Long ->
                var selectedString = dialogBinding.countries.getItemAtPosition(p).toString()
                selectedString = selectedString.substring(selectedString.length - 4, selectedString.length - 2)
                listener.sendMessage("treehouses wificountry $selectedString")
                binding.countryDisplay.isEnabled = false
                binding.countryDisplay.setText("Changing country")
                dialog.dismiss()
            }

            dialogBinding.searchBar.isIconifiedByDefault = false
            dialogBinding.searchBar.setOnQueryTextListener(this)
            dialog.show()
        }
    }
}