package io.treehouses.remote.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import bolts.Task.delay
import io.treehouses.remote.BuildConfig
import io.treehouses.remote.Constants
import io.treehouses.remote.Fragments.DialogFragments.FeedbackDialogFragment
import io.treehouses.remote.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class AboutFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_about, container, false)

        setHyperlinks(view)
        setFeedback(view)
        setVersion(view)
        setCopyright(view)

        return view
    }

    private fun setHyperlinks(view : View) {
        val gitHub = view.findViewById<Button>(R.id.btn_github)
        val images = view.findViewById<Button>(R.id.btn_image)
        val gitter = view.findViewById<Button>(R.id.btn_gitter)
        val contributors = view.findViewById<Button>(R.id.btn_contributors)

        hyperLinks(gitHub, "https://github.com/treehouses/remote")
        hyperLinks(images, "https://treehouses.io/#!pages/download.md")
        hyperLinks(gitter, "https://gitter.im/open-learning-exchange/raspberrypi")
        hyperLinks(contributors, "https://github.com/treehouses/remote/graphs/contributors")

    }

    private fun setFeedback(view : View) {
        val giveFeedback = view.findViewById<Button>(R.id.give_feedback)

        giveFeedback.setOnClickListener {
            val dialogFrag: DialogFragment = FeedbackDialogFragment()
            dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT)
            dialogFrag.show(requireActivity().supportFragmentManager.beginTransaction(), "feedbackDialogFragment")
        }
    }

    private fun setVersion(view : View) {
        val version = view.findViewById<Button>(R.id.btn_version)
        version.setOnClickListener {
            var versionName = BuildConfig.VERSION_NAME
            if (versionName == "1.0.0") {
                versionName = "latest version"
            }
            Toast.makeText(context, versionName, Toast.LENGTH_LONG).show()
        }
    }

    private fun setCopyright(view : View) {
        val tvCopyright = view.findViewById<TextView>(R.id.tv_copyright)
        val format = SimpleDateFormat("yyyy")
        tvCopyright.text = String.format(getString(R.string.copyright), format.format(Date()) + "")
    }

    private fun hyperLinks(view: View, url: String) {
        var click = true
        view.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            if (click) {
                view.setBackgroundColor(resources.getColor(R.color.colorPrimaryLight))
                click = false
            } else {
                view.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                click = true
            }
            startActivity(i)
        }



    }
}
