package com.kevinkyang.inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Kevin on 3/2/2017.
 */

public class DrawerAdapter extends BaseExpandableListAdapter {
	private Context context;
	private ArrayList<String> titles;
	private Map<String, ArrayList<String>> childrenMap;
	private ExpandableListView parentListView;

	public DrawerAdapter(Context context,
						 ArrayList<String> titles,
						 Map<String, ArrayList<String>> childrenMap,
						 ExpandableListView parentListView) {
		this.context = context;
		this.titles = titles;
		this.childrenMap = childrenMap;
		this.parentListView = parentListView;
	}

	@Override
	public int getGroupCount() {
		return titles.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return childrenMap.get(titles.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return titles.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return childrenMap
				.get(titles.get(groupPosition))
				.get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		final String titleText = (String) getGroup(groupPosition);

		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.drawer_list_group, null);
		}

		TextView childTextView = (TextView) convertView.findViewById(R.id.drawer_group_textview);
		childTextView.setText(titleText);

		final ImageButton button = (ImageButton) convertView.findViewById(R.id.button_expand_collapse);
		button.setFocusable(false);
		if (childrenMap.get(titles.get(groupPosition)).isEmpty()) {
			button.setVisibility(View.INVISIBLE);
		} else {
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (parentListView.isGroupExpanded(groupPosition)) {
						parentListView.collapseGroup(groupPosition);
						button.setImageDrawable(context.getDrawable(R.drawable.ic_expand_more));
					} else {
						parentListView.expandGroup(groupPosition);
						button.setImageDrawable(context.getDrawable(R.drawable.ic_expand_less));
					}
				}
			});
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		final String titleText = (String) getChild(groupPosition, childPosition);

		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.drawer_list_child, null);
		}

		TextView childTextView = (TextView) convertView.findViewById(R.id.drawer_child_textview);
		childTextView.setText(titleText);
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}