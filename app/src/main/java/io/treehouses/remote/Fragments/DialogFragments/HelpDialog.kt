package io.treehouses.remote.Fragments.DialogFragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.treehouses.remote.Views.RecyclerViewClickListener
import io.treehouses.remote.adapter.HelpAdapter
import io.treehouses.remote.databinding.DialogHelpBinding
import io.treehouses.remote.pojo.HelpCommand
import org.json.JSONObject


class HelpDialog : DialogFragment(), android.widget.SearchView.OnQueryTextListener {
    private lateinit var bind: DialogHelpBinding
    private var jsonString = ""
    private val items = mutableListOf<HelpCommand>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogHelpBinding.inflate(inflater, container, false)
        bind.progressBar.visibility = View.VISIBLE
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.closeButton.setOnClickListener { dismiss() }
        bind.results.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = HelpAdapter()
        }
        bind.searchBar.setOnQueryTextListener(this)
        bind.results.addOnItemTouchListener(RecyclerViewClickListener(context, bind.results, object: RecyclerViewClickListener.ClickListener {
            override fun onClick(view: View?, position: Int) {
                val item = (bind.results.adapter as HelpAdapter).getitem(position)
                transitionDescription(item)
            }
            override fun onLongClick(view: View?, position: Int) {}
        }))

        bind.backButton.setOnClickListener { transitionSearch() }

        jsonString = arguments?.getString("jsonString")!!
        createJson(jsonString)


    }

    private fun transitionDescription(item: HelpCommand) {
        bind.results.visibility = View.GONE
        bind.searchBar.visibility = View.GONE
        bind.showHelp.visibility = View.VISIBLE
        bind.backButton.visibility = View.VISIBLE
        bind.titleDescription.text = item.title
        bind.description.text = item.preview
    }

    private fun transitionSearch() {
        bind.results.visibility = View.VISIBLE
        bind.searchBar.visibility = View.VISIBLE
        bind.showHelp.visibility = View.GONE
        bind.backButton.visibility = View.GONE
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    private fun createJson(jsonStr: String) {
        val obj = JSONObject(jsonStr)
        obj.keys().forEach {
            (bind.results.adapter as HelpAdapter).add(HelpCommand(it, obj.get(it) as String))
            items.add(HelpCommand(it, obj.get(it) as String))
        }
        bind.progressBar.visibility = View.GONE
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        val filteredList = items.filter { it.title.contains(newText) || it.preview.contains(newText) }
        (bind.results.adapter as HelpAdapter).replaceAll(filteredList)
        bind.results.scrollToPosition(0)
        return false
    }
}