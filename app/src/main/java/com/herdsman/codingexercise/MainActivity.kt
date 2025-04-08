package com.herdsman.codingexercise

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.herdsman.codingexercise.databinding.ActivityMainBinding
import com.herdsman.codingexercise.databinding.ItemChildLayoutBinding
import com.herdsman.codingexercise.databinding.ItemParentLayoutBinding
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DATA_URL = "https://fetch-hiring.s3.amazonaws.com/"
    }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAll()
    }

    private fun initAll() {
        val retrofit = Retrofit.Builder().baseUrl(DATA_URL).addConverterFactory(GsonConverterFactory.create()).build()
        val apiService = retrofit.create(ApiService::class.java)

        lifecycleScope.launch {
            try {
                val items = apiService.getItems()
                binding.loadingLayout.visibility = View.GONE
                binding.expandableListView.visibility = View.VISIBLE

                binding.expandableListView.setAdapter(ItemExpandableListAdapter(parseItems(items)))
            } catch (e: Throwable) {
                e.printStackTrace()
                binding.loadingTextView.text = getString(R.string.network_error)
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    data class Item(val id: Int, val listId: Int, val name: String?)

    data class ParentItem(val listId: Int, val children: List<ChildItem>, var isExpanded: Boolean = false) {
        fun childCount(): Int = children.size
    }

    data class ChildItem(val id: Int, val name: String)

    interface ApiService {
        @GET("hiring.json")
        suspend fun getItems(): List<Item>
    }

    private fun parseItems(flatList: List<Item>): List<ParentItem> {
        return flatList.filter { !it.name.isNullOrBlank() }.sortedWith(compareBy({ it.listId }, { it.id })).groupBy { it.listId }.map { (listId, items) ->
            ParentItem(
                listId = listId, children = items.map { ChildItem(it.id, it.name!!) })
        }
    }

    class ItemExpandableListAdapter(private val parentItems: List<ParentItem>) : BaseExpandableListAdapter() {
        override fun getGroupCount(): Int = parentItems.size

        override fun getChildrenCount(groupPosition: Int): Int = parentItems[groupPosition].children.size

        override fun getGroup(groupPosition: Int): Any = parentItems[groupPosition]

        override fun getChild(groupPosition: Int, childPosition: Int): Any = parentItems[groupPosition].children[childPosition]

        override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

        override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

        override fun hasStableIds(): Boolean = false

        @SuppressLint("SetTextI18n")
        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
            val binding = ItemParentLayoutBinding.inflate(LayoutInflater.from(parent!!.context))
            binding.parentItem = parentItems[groupPosition]
            return binding.root
        }

        @SuppressLint("SetTextI18n")
        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
            val binding = ItemChildLayoutBinding.inflate(LayoutInflater.from(parent!!.context))
            binding.childItem = parentItems[groupPosition].children[childPosition]
            return binding.root
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
    }
}