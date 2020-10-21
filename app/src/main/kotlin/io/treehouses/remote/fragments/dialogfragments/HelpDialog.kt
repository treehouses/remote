package io.treehouses.remote.fragments.dialogfragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import io.treehouses.remote.Constants
import io.treehouses.remote.views.RecyclerViewClickListener
import io.treehouses.remote.adapter.HelpAdapter
import io.treehouses.remote.bases.FullScreenDialogFragment
import io.treehouses.remote.databinding.DialogHelpBinding
import io.treehouses.remote.pojo.HelpCommand
import org.json.JSONObject


class HelpDialog : FullScreenDialogFragment(), android.widget.SearchView.OnQueryTextListener {
    private lateinit var bind: DialogHelpBinding
    private var jsonString = ""
    private val items = mutableListOf<HelpCommand>()
    private val excludedHelpItems = listOf<String>("anime")
    private var queryText = ""

    private var selectedItem: HelpCommand = HelpCommand()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogHelpBinding.inflate(inflater, container, false)
        bind.progressBar.visibility = View.VISIBLE
        val back = ColorDrawable(Color.TRANSPARENT)
        val inset = InsetDrawable(back, 30)
        dialog!!.window!!.setBackgroundDrawable(inset)
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
        bind.searchBar.isIconifiedByDefault = false
        bind.results.addOnItemTouchListener(RecyclerViewClickListener(context, bind.results, object: RecyclerViewClickListener.ClickListener {
            override fun onClick(view: View?, position: Int) {
                transition(position, true)
            }
            override fun onLongClick(view: View?, position: Int) {
                transition(position, false)
            }
        }))

        bind.backButton.setOnClickListener { fullTransitionSearch() }

        jsonString = arguments?.getString(Constants.JSON_STRING)!!
        createJson(jsonString)
    }

    fun transition(position: Int, full: Boolean) {
        selectedItem = getItemAtPosition(position)
        if (full) fullTransitionDescription(selectedItem)
        else transitionDescription(selectedItem)
    }

    fun getItemAtPosition(pos: Int): HelpCommand {
        return (bind.results.adapter as HelpAdapter).getitem(pos)
    }

    private fun fullTransitionDescription(item: HelpCommand) {
        bind.results.visibility = View.GONE
        bind.searchBar.visibility = View.GONE
        bind.backButton.visibility = View.VISIBLE

        transitionDescription(item)
    }

    private fun fullTransitionSearch() {
        bind.results.visibility = View.VISIBLE
        bind.searchBar.visibility = View.VISIBLE
        bind.showHelp.visibility = View.GONE
        bind.backButton.visibility = View.GONE
    }
    private fun transitionDescription(item: HelpCommand) {
        bind.showHelp.visibility = View.VISIBLE
        bind.titleDescription.text = item.title
        updateHighlight(item)
    }

    private fun highlight(original: String, word: String): Spannable? {
        val highlighted: Spannable = SpannableString(original)
        if (original.isEmpty() || word.isEmpty()) return highlighted
        var start = original.indexOf(word)
        while (start >= 0) {
            val spanStart = start.coerceAtMost(original.length)
            val spanEnd = (start + word.length).coerceAtMost(original.length)
            highlighted.setSpan(BackgroundColorSpan(Color.YELLOW), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            start = original.indexOf(word, spanEnd)
        }
        return highlighted
    }

    private fun updateHighlight(item: HelpCommand) {
        bind.description.text = highlight(item.preview, queryText)
    }

    private fun createJson(jsonStr: String) {
        val obj = JSONObject(jsonStr)
        obj.keys().forEach {
            if(excludedHelpItems.indexOf(it) == -1) {
                (bind.results.adapter as HelpAdapter).add(HelpCommand(it, obj.get(it) as String))
                items.add(HelpCommand(it, obj.get(it) as String))
            }
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
        queryText = newText
        if (bind.description.visibility == View.VISIBLE && selectedItem.title.isNotEmpty()) updateHighlight(selectedItem)
        return false
    }
}