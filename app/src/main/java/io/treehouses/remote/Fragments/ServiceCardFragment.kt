package io.treehouses.remote.Fragments

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import io.treehouses.remote.R
import io.treehouses.remote.callback.ServiceAction
import io.treehouses.remote.pojo.ServiceInfo

class ServiceCardFragment(private val serviceData: ServiceInfo) : Fragment(), View.OnClickListener {
    private var logo: ImageView? = null
    private var serviceInfo: TextView? = null
    private var install: Button? = null
    private var start: Button? = null
    private var openLink: Button? = null
    private var actionListener: ServiceAction? = null
    private var autorunCheck: CheckBox? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.service_card, container, false) as ViewGroup
        logo = view.findViewById(R.id.service_logo)
        serviceInfo = view.findViewById(R.id.service_info)
        serviceInfo.setMovementMethod(LinkMovementMethod.getInstance())
        serviceInfo.setFocusable(true)
        install = view.findViewById(R.id.install_button)
        start = view.findViewById(R.id.start_button)
        openLink = view.findViewById(R.id.openLink)
        autorunCheck = view.findViewById(R.id.autorun_checked)
        if (!serviceData.isHeader) {
            setServiceInfo(serviceData.info)
            showIcon(serviceData.icon)
            updateButtons(serviceData.serviceStatus)
            setAutorun(serviceData.autorun)
            install.setOnClickListener(this)
            start.setOnClickListener(this)
            openLink.setOnClickListener(this)
            autorunCheck.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean -> actionListener!!.onClickAutorun(serviceData, isChecked) })
        }
        return view
    }

    private fun setAutorun(autorun: String?) {
        if (autorun!!.contains("true")) {
            autorunCheck!!.isChecked = true
        } else {
            autorunCheck!!.isChecked = false
        }
    }

    private fun setButtons(started: Boolean, installed: Boolean, three: Boolean) {
        if (started) {
            start!!.text = "Stop"
            openLink!!.visibility = View.VISIBLE
        } else {
            start!!.text = "Start"
            openLink!!.visibility = View.GONE
        }
        if (installed) {
            install!!.text = "Uninstall"
            start!!.visibility = View.VISIBLE
            autorunCheck!!.visibility = View.VISIBLE
        } else {
            install!!.text = "Install"
            start!!.visibility = View.GONE
            autorunCheck!!.visibility = View.GONE
        }
        //restart.setEnabled(three);
    }

    private fun updateButtons(statusCode: Int) {
        when (statusCode) {
            ServiceInfo.SERVICE_AVAILABLE -> setButtons(false, false, false)
            ServiceInfo.SERVICE_INSTALLED -> setButtons(false, true, false)
            ServiceInfo.SERVICE_RUNNING -> setButtons(true, true, true)
        }
    }

    private fun showIcon(s: String?) {
        try {
            Log.d(serviceData.name, "showIcon:" + serviceData.icon)
            val svg = SVG.getFromString(s)
            val pd = PictureDrawable(svg.renderToPicture())
            logo!!.setImageDrawable(pd)
        } catch (e: SVGParseException) {
            e.printStackTrace()
        }
    }

    private fun setServiceInfo(s: String?) {
        val spannableString = SpannableString(s)
        Linkify.addLinks(spannableString, Linkify.ALL)
        serviceInfo!!.text = s
        serviceInfo!!.movementMethod = LinkMovementMethod.getInstance()
    }

    //    private void onClickRestart(ServiceInfo selected) {
    //        if (selected.serviceStatus != ServiceInfo.SERVICE_AVAILABLE) performService("Restarting", "treehouses services " + selected.name + " restart\n", selected.name);
    //
    //    }
    //    private void setOnClick(View v, int id, String command, AlertDialog alertDialog) {
    //        v.findViewById(id).setOnClickListener(new View.OnClickListener() {
    //            @Override
    //            public void onClick(View v) {
    //                writeToRPI(command);
    //                alertDialog.dismiss();
    ////                progressBar.setVisibility(View.VISIBLE);
    //            }
    //        });
    //    }
    override fun onClick(v: View) {
        when (v.id) {
            R.id.install_button -> actionListener!!.onClickInstall(serviceData)
            R.id.start_button -> actionListener!!.onClickStart(serviceData)
            R.id.openLink -> actionListener!!.onClickLink(serviceData)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        actionListener = parentFragment as ServiceAction?
    }

}