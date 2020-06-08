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
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import io.treehouses.remote.callback.ServiceAction
import io.treehouses.remote.databinding.ServiceCardBinding
import io.treehouses.remote.pojo.ServiceInfo

class ServiceCardFragment(private val serviceData: ServiceInfo) : Fragment(), View.OnClickListener {
    private var actionListener: ServiceAction? = null
    private var binding: ServiceCardBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ServiceCardBinding.inflate(inflater, container, false)
        binding!!.serviceInfo.movementMethod = LinkMovementMethod.getInstance()
        binding!!.serviceInfo.isFocusable = true
        if (!serviceData.isHeader) {
            setServiceInfo(serviceData.info)
            showIcon(serviceData.icon)
            updateButtons(serviceData.serviceStatus)
            setAutorun(serviceData.autorun)
            binding!!.installButton.setOnClickListener(this)
            binding!!.startButton.setOnClickListener(this)
            binding!!.openLink.setOnClickListener(this)
            binding!!.autorunChecked.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean -> actionListener!!.onClickAutorun(serviceData, isChecked) }
        }
        return binding!!.root
    }

    private fun setAutorun(autorun: String?) {
        binding!!.autorunChecked.isChecked = autorun!!.contains("true")
    }

    private fun setButtons(started: Boolean, installed: Boolean, three: Boolean) {
        if (started) {
            binding!!.startButton.text = "Stop"
            binding!!.openLink.visibility = View.VISIBLE
        } else {
            binding!!.startButton.text = "Start"
            binding!!.openLink.visibility = View.GONE
        }
        if (installed) {
            binding!!.installButton.text = "Uninstall"
            binding!!.startButton.visibility = View.VISIBLE
            binding!!.autorunChecked.visibility = View.VISIBLE
        } else {
            binding!!.installButton.text = "Install"
            binding!!.startButton.visibility = View.GONE
            binding!!.autorunChecked.visibility = View.GONE
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
            binding!!.serviceLogo.setImageDrawable(pd)
        } catch (e: SVGParseException) {
            e.printStackTrace()
        }
    }

    private fun setServiceInfo(s: String?) {
        val spannableString = SpannableString(s)
        Linkify.addLinks(spannableString, Linkify.ALL)
        binding!!.serviceInfo.text = s
        binding!!.serviceInfo.movementMethod = LinkMovementMethod.getInstance()
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
        if (binding!!.installButton == v) {
            actionListener!!.onClickInstall(serviceData)
        } else if (binding!!.startButton == v) {
            actionListener!!.onClickStart(serviceData)
        } else if (binding!!.openLink == v) {
            actionListener!!.onClickLink(serviceData)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        actionListener = parentFragment as ServiceAction?
    }

}