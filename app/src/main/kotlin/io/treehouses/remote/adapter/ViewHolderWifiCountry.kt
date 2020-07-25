package io.treehouses.remote.adapter

import android.app.Dialog

import android.content.Context
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener
import java.util.*

class ViewHolderWifiCountry internal constructor(v: View, context: Context, listener: HomeInteractListener) : SearchView.OnQueryTextListener {
    lateinit var textBar: TextInputEditText
    private val mChatService: BluetoothChatService
    lateinit var c: Context
    private var countryList: ListView? = null
    var searchView: SearchView? = null
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == Constants.MESSAGE_READ) {
                val readMessage = msg.obj as String
                if (readMessage.contains("country=") || readMessage.contains("set to")) {
                    val len = readMessage.length - 3
                    val country = readMessage.substring(len).trim { it <= ' ' }
                    textBar.setText(getCountryName(country))
                    textBar.isEnabled = true
                } else if (readMessage.contains("Error when")) {
                    textBar.setText("try again")
                    textBar.isEnabled = true
                    Toast.makeText(c, "Error when changing country", Toast.LENGTH_LONG).show()
                }
            }
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


        listener.sendMessage("treehouses wificountry")
        val countriesCode = Locale.getISOCountries()
        val countriesName = arrayOfNulls<String>(countriesCode.size)
        for (i in countriesCode.indices) {
            countriesName[i] = getCountryName(countriesCode[i])
        }
        val adapter = ArrayAdapter(context, R.layout.select_dialog_item_countries, countriesName)
        c = context
        mChatService = listener.getChatService()
        mChatService.updateHandler(mHandler)
        textBar = v.findViewById(R.id.country_display)
        textBar.isEnabled = false
        textBar.setOnClickListener {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.dialog_wificountry)
            countryList = dialog.findViewById(R.id.countries)
            adapter.filter.filter("")
            countryList!!.adapter = adapter
            countryList!!.isTextFilterEnabled = true
            countryList!!.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, p: Int, _: Long ->
                var selectedString = countryList!!.getItemAtPosition(p).toString()
                selectedString = selectedString.substring(selectedString.length - 4, selectedString.length - 2)
                listener.sendMessage("treehouses wificountry $selectedString")
                textBar.isEnabled = false
                textBar.setText("Changing country")
                dialog.dismiss()
            }
            searchView = dialog.findViewById(R.id.search_bar)
            searchView!!.isIconifiedByDefault = false
            searchView!!.setOnQueryTextListener(this)

            dialog.show()
        }
    }
}