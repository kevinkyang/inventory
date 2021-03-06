package com.kevinkyang.inventory

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

import com.kevinkyang.expandableRVAdapter.*

import kotlinx.android.synthetic.main.drawer_list_group.view.*
import kotlinx.android.synthetic.main.drawer_list_child.view.*

class DrawerGroupItem(var name: String) : GroupItemBase()

class DrawerChildItem(var name: String) : ChildItemBase()

class DrawerGroupVH(itemView: View, var groupTitle: TextView,
                    var button: ImageButton)
    : ExpandableViewHolder(itemView)

class DrawerChildVH(itemView: View, var childTitle: TextView,
                    var colorTag: ImageView, var itemCountLabel: TextView)
    : ExpandableViewHolder(itemView)

class ExpandableDrawerAdapter(var context: Context,
        groups: ArrayList<DrawerGroupItem>,
        children: ArrayList<ArrayList<DrawerChildItem>>) :
        ExpandableRecyclerViewAdapter<DrawerGroupItem,
                DrawerChildItem,
                DrawerGroupVH,
                DrawerChildVH>
                (groups, children) {

    private var colorArray: TypedArray = context.resources
            .obtainTypedArray(R.array.array_inventory_colors)

    override fun createGroupViewHolder(parent: ViewGroup?): DrawerGroupVH {
        val inflater = context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.drawer_list_group, parent, false)
        val groupTitle = view.drawer_group_textview
        val button = view.button_expand_collapse
        button.isFocusable = false

        return DrawerGroupVH(view, groupTitle, button)
    }

    override fun createChildViewHolder(parent: ViewGroup?): DrawerChildVH {
        val inflater = context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.drawer_list_child, parent, false)
        val childTitle = view.drawer_child_textview
        val colorTag = view.drawer_child_color_tag
        val itemCountLabel = view.label_item_count

        return DrawerChildVH(view, childTitle, colorTag, itemCountLabel)
    }

    override fun bindGroupViewHolder(holder: DrawerGroupVH, item: DrawerGroupItem) {
        val adapter = this

        holder.groupTitle.text = item.name
        if (getChildrenCount(item.groupPosition) == 0) {
            holder.button.visibility = View.INVISIBLE
        } else {
            holder.button.setOnClickListener {
                if (adapter.isGroupExpanded(item.groupPosition)) {
                    collapseRotateAnimation(holder.button)
                    adapter.collapseGroup(item.groupPosition)
                } else {
                    expandRotateAnimation(holder.button)
                    adapter.expandGroup(item.groupPosition)
                }
            }
        }
    }

    override fun bindChildViewHolder(holder: DrawerChildVH,
                                     item: DrawerChildItem) {

        holder.childTitle.text = item.name
        if (item.groupPosition != 0) {
            return
        }

        if (item.childPosition != getChildrenCount(item.groupPosition) - 1) {
            holder.colorTag.setImageDrawable(null)
            holder.colorTag.layoutParams.width = 6
            holder.colorTag.setBackgroundColor(
                    colorArray.getColor(item.childPosition, 0))
            holder.itemCountLabel.visibility = View.VISIBLE
            val count = ItemManager.getInstance()
                    .getInventoryItemCount(item.name)
            val countString: String
            if (count < 100) {
                countString = Integer.toString(count)
            } else {
                countString = "99+"
            }
            holder.itemCountLabel.text = countString

        } else {
            var addIcon = context.resources.getDrawable(
                    R.drawable.ic_add, null)
            addIcon = addIcon.constantState.newDrawable().mutate()
            addIcon.colorFilter = PorterDuffColorFilter(context.getColor(
                    android.R.color.primary_text_light),
                    PorterDuff.Mode.MULTIPLY)
            holder.colorTag.setBackgroundColor(Color.TRANSPARENT)
            holder.colorTag.layoutParams.width = 40
            holder.colorTag.setImageDrawable(addIcon)
            holder.itemCountLabel.visibility = View.INVISIBLE
        }

        setChildAnimation(holder.itemView)
    }

    private fun expandRotateAnimation(view: View) {
        val expandAnim = AnimationUtils.loadAnimation(context, R.anim.expand_rotation)
        expandAnim.fillAfter = true
        view.startAnimation(expandAnim)
    }

    private fun collapseRotateAnimation(view: View) {
        val collapseAnim = AnimationUtils.loadAnimation(context, R.anim.collapse_rotation)
        collapseAnim.fillAfter = true
        view.startAnimation(collapseAnim)
    }

    fun setChildAnimation(viewToAnimate: View) {
            val anim = AnimationUtils.loadAnimation(context, R.anim.push_in_top)
            anim.duration = 500
            viewToAnimate.startAnimation(anim)
    }

}
