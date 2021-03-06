package com.kevinkyang.inventory;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class DrawerRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	public static final int VIEWTYPE_GROUP = 0;
	public static final int VIEWTYPE_CHILD = 1;

	private Context mContext;

	private ArrayList<String> mGroups;
	private Map<String, ArrayList<String>> mGroupToChildrenMap;
	/**
	 * List that represents currently visible drawer items.
	 */
	private ArrayList<DrawerItem> mInternalList;

	private OnDrawerClickListener mOnDrawerClickListener;

	private TypedArray mColorArray;

	public DrawerRVAdapter(@NonNull Context context,
						   @NonNull ArrayList<String> groups,
						   @NonNull Map<String, ArrayList<String>>
								   groupToChildrenMap) {
		this.mContext = context;
		this.mGroups = groups;
		this.mGroupToChildrenMap = groupToChildrenMap;
		initInternalList(groups);
		mOnDrawerClickListener = null;
		mColorArray = context.getResources()
				.obtainTypedArray(R.array.array_inventory_colors);
	}

	private void initInternalList(ArrayList<String> groups) {
		mInternalList = new ArrayList<>();

		for (int pos = 0; pos < groups.size(); pos++) {
			mInternalList.add(new GroupItem(groups.get(pos), pos));
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
													  int viewType) {
		if (viewType == VIEWTYPE_GROUP) {
			return createGroupViewHolder(parent);
		} else if (viewType == VIEWTYPE_CHILD) {
			return createChildViewHolder(parent);
		} else {
			return null;
		}
	}

	public GroupViewHolder createGroupViewHolder(ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(
				R.layout.drawer_list_group, parent, false);

		TextView groupTitle =
				(TextView) view.findViewById(R.id.drawer_group_textview);
		ImageButton button =
				(ImageButton) view.findViewById(R.id.button_expand_collapse);
		button.setFocusable(false);

		return new GroupViewHolder(view, groupTitle, button, this);
	}

	public ChildViewHolder createChildViewHolder(ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(
				R.layout.drawer_list_child, parent, false);

		TextView childTitle =
				(TextView) view.findViewById(R.id.drawer_child_textview);
		ImageView colorTag =
				(ImageView) view.findViewById(R.id.drawer_child_color_tag);
		TextView itemCountLabel =
				(TextView) view.findViewById(R.id.label_item_count);

		return new ChildViewHolder(view, childTitle, colorTag,
				itemCountLabel, this);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder,
								 int position) {
		if (holder instanceof GroupViewHolder) {
			bindGroupViewHolder((GroupViewHolder)holder, position);
		} else if (holder instanceof ChildViewHolder) {
			bindChildViewHolder((ChildViewHolder) holder, position);
		}
	}

	public void bindGroupViewHolder(final GroupViewHolder holder, final int listPosition) {
		GroupItem item = (GroupItem) mInternalList.get(listPosition);

		holder.groupTitle.setText(item.getName());
		if (mGroupToChildrenMap.get(item.getName()).isEmpty()) {
			holder.button.setVisibility(View.INVISIBLE);
		} else {
			holder.button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DrawerRVAdapter adapter = DrawerRVAdapter.this;
					if (adapter.isGroupExpanded(listPosition)) {
						collapseAnimation(holder.button);
						adapter.collapseGroup(listPosition);
					} else {
						expandAnimation(holder.button);
						adapter.expandGroup(listPosition);
					}
				}
			});
		}
	}

	public void bindChildViewHolder(ChildViewHolder holder, int listPosition) {
		ChildItem item = (ChildItem) mInternalList.get(listPosition);

		holder.childTitle.setText(item.getName());
		if (item.getGroupPosition() == 0) {
			bindSubInventories(holder, item);
		}
	}

	private void bindSubInventories(ChildViewHolder holder, ChildItem item) {
		ArrayList<String> children =
				mGroupToChildrenMap.get(mGroups.get(item.getGroupPosition()));
		if (item.getChildPosition() != children.size() - 1) {
			holder.colorTag.setImageDrawable(null);
			holder.colorTag.getLayoutParams().width = 6;
			holder.colorTag.setBackgroundColor(
					mColorArray.getColor(item.getChildPosition(), 0));
			int count = ItemManager.getInstance()
					.getInventoryItemCount(item.getName());
			holder.itemCountLabel.setVisibility(View.VISIBLE);
			if (count < 100) {
				holder.itemCountLabel.setText(Integer.toString(count));
			} else {
				holder.itemCountLabel.setText("99+");
			}

		} else {
			Drawable addIcon = mContext.getResources()
					.getDrawable(R.drawable.ic_add, null);
			addIcon = addIcon.getConstantState().newDrawable().mutate();
			addIcon.setColorFilter(
					new PorterDuffColorFilter(
							mContext.getColor(
									android.R.color.primary_text_light),
									PorterDuff.Mode.MULTIPLY));
			holder.colorTag.setBackgroundColor(Color.TRANSPARENT);
			holder.colorTag.getLayoutParams().width = 40;
			holder.colorTag.setImageDrawable(addIcon);

			holder.itemCountLabel.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public int getItemCount() {
		return mInternalList.size();
	}

	public int getGroupCount() {
		return mGroups.size();
	}

	public int getChildrenCount(int groupPosition) {
		if (groupPosition < 0 || groupPosition >= mGroups.size()) {
			throw new IndexOutOfBoundsException();
		}

		ArrayList<String> children =
				mGroupToChildrenMap.get(mGroups.get(groupPosition));
		return children.size();
	}

	@Override
	public int getItemViewType(int position) {
		DrawerItem item = mInternalList.get(position);
		if (item instanceof GroupItem) {
			return VIEWTYPE_GROUP;
		} else if (item instanceof ChildItem) {
			return VIEWTYPE_CHILD;
		} else {
			return -1;
		}
	}

	public DrawerItem getInternalListItem(int listPosition) {
		return mInternalList.get(listPosition);
	}

	public String getGroup(int groupPosition) {
		if (groupPosition < 0 || groupPosition >= mGroups.size()) {
			throw new IndexOutOfBoundsException();
		}
		return mGroups.get(groupPosition);
	}

	public String getChild(int groupPosition, int childPosition) {
		if (groupPosition < 0 || groupPosition >= mGroups.size()) {
			throw new IndexOutOfBoundsException();
		}

		ArrayList<String> children =
				mGroupToChildrenMap.get(mGroups.get(groupPosition));
		if (childPosition < 0 || childPosition >= children.size()) {
			throw new IndexOutOfBoundsException();
		}

		return children.get(childPosition);
	}

	public boolean isGroupExpanded(int listPosition) {
		if (listPosition < 0 || listPosition >= mInternalList.size()) {
			throw new IndexOutOfBoundsException();
		}

		GroupItem item = (GroupItem) mInternalList.get(listPosition);
		if (mGroupToChildrenMap.get(item.getName()) != null &&
				!mGroupToChildrenMap.get(item.getName()).isEmpty() &&
				listPosition < mInternalList.size() - 1 &&
				mInternalList.get(listPosition + 1) instanceof ChildItem) {
			return true;
		} else {
			return false;
		}
	}

	public void expandGroup(int listPosition) {
		if (listPosition < 0 || listPosition >= mGroups.size()) {
			throw new IndexOutOfBoundsException();
		}

		if (isGroupExpanded(listPosition)) {
			return;
		}

		GroupItem groupItem = (GroupItem) mInternalList.get(listPosition);
		ArrayList<String> children =
				mGroupToChildrenMap.get(groupItem.getName());
		for (int pos = 0; pos < children.size(); pos++) {
			mInternalList.add(listPosition + pos + 1,
					new ChildItem(children.get(pos), groupItem.getGroupPosition(), pos));
			notifyItemInserted(listPosition + pos + 1);
		}
	}

	public void collapseGroup(int listPosition) {
		if (listPosition < 0 || listPosition >= mGroups.size()) {
			throw new IndexOutOfBoundsException();
		}

		if (!isGroupExpanded(listPosition)) {
			return;
		}

		GroupItem groupItem = (GroupItem) mInternalList.get(listPosition);
		ArrayList<String> children =
				mGroupToChildrenMap.get(groupItem.getName());
		for (int i = 0; i < children.size(); i++) {
			mInternalList.remove(listPosition + 1);
			notifyItemRemoved(listPosition + 1);
		}
	}

	public static class GroupViewHolder extends RecyclerView.ViewHolder
			implements View.OnClickListener{
		private DrawerRVAdapter parent;

		public View itemView;
		public TextView groupTitle;
		public ImageButton button;

		public GroupViewHolder(View itemView, TextView groupTitle,
							   ImageButton button,
							   @NonNull DrawerRVAdapter parent) {
			super(itemView);
			this.itemView = itemView;
			this.groupTitle = groupTitle;
			this.button = button;

			this.parent = parent;
			this.itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			OnDrawerClickListener listener =
					parent.getOnDrawerClickListener();
			if (listener != null) {
				GroupItem item = (GroupItem) parent
						.getInternalListItem(getAdapterPosition());
				listener.onGroupClick(item.getGroupPosition());
			}
		}
	}

	public static class ChildViewHolder extends RecyclerView.ViewHolder
			implements View.OnClickListener{
		private DrawerRVAdapter parent;

		public View itemView;
		public TextView childTitle;
		public ImageView colorTag;
		public TextView itemCountLabel;

		public ChildViewHolder(View itemView, TextView childTitle,
							   ImageView colorTag, TextView itemCountLabel,
							   @NonNull DrawerRVAdapter parent) {
			super(itemView);
			this.itemView = itemView;
			this.childTitle = childTitle;
			this.colorTag = colorTag;
			this.itemCountLabel = itemCountLabel;

			this.parent = parent;
			this.itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			OnDrawerClickListener listener =
					parent.getOnDrawerClickListener();
			if (listener != null) {
				ChildItem item = (ChildItem) parent
						.getInternalListItem(getAdapterPosition());
				listener.onChildClick(item.getGroupPosition(),
						item.getChildPosition());
			}
		}
	}

	private interface DrawerItem {
		public String getName();
	}

	private class GroupItem implements DrawerItem {
		private String name;
		private int groupPosition;

		public GroupItem(String name, int groupPosition) {
			this.name = name;
			this.groupPosition = groupPosition;
		}

		@Override
		public String getName() {
			return name;
		}

		public int getGroupPosition() {
			return groupPosition;
		}

		public void setGroupPosition(int groupPosition) {
			this.groupPosition = groupPosition;
		}
	}

	private class ChildItem implements DrawerItem {
		private String name;
		private int groupPosition;
		private int childPosition;

		public ChildItem(String name, int groupPosition, int childPosition) {
			this.name = name;
			this.groupPosition = groupPosition;
			this.childPosition = childPosition;
		}

		@Override
		public String getName() {
			return name;
		}

		public int getGroupPosition() {
			return groupPosition;
		}

		public void setGroupPosition(int groupPosition) {
			this.groupPosition = groupPosition;
		}

		public int getChildPosition() {
			return childPosition;
		}

		public void setChildPosition(int childPosition) {
			this.childPosition = childPosition;
		}
	}

	public interface OnDrawerClickListener {
		public boolean onGroupClick(int groupPosition);

		public boolean onChildClick(int groupPosition, int childPosition);
	}

	public OnDrawerClickListener getOnDrawerClickListener() {
		return mOnDrawerClickListener;
	}

	public void setOnDrawerClickListener(
			OnDrawerClickListener onDrawerClickListener) {
		this.mOnDrawerClickListener = onDrawerClickListener;
	}

	public void addInventory(String name) {
		int groupIndex = 0;
		ArrayList<String> inventories =
				mGroupToChildrenMap.get(mGroups.get(groupIndex));
		int childCount = inventories.size();
		inventories.add(childCount - 1, name);

		ChildItem item = new ChildItem(name, groupIndex, childCount - 1);
		mInternalList.add(groupIndex + childCount, item);
		notifyItemInserted(groupIndex + childCount);
	}

	public void removeInventory(String name) {
		// TODO method untested
		int groupIndex = 0;
		ArrayList<String> inventories =
				mGroupToChildrenMap.get(mGroups.get(groupIndex));
		int itemIndex = inventories.indexOf(name);
		if (itemIndex < 0) {
			return;
		}

		inventories.remove(itemIndex);
		mInternalList.remove(groupIndex + itemIndex + 1);
		notifyItemRemoved(groupIndex + itemIndex + 1);
	}

	private void expandAnimation(View view) {
		Animation expandAnimation =
				AnimationUtils.loadAnimation(mContext,
						R.anim.expand_rotation);
		expandAnimation.setFillAfter(true);
		view.startAnimation(expandAnimation);
	}

	private void collapseAnimation(View view) {
		Animation collapseAnimation =
				AnimationUtils.loadAnimation(mContext,
						R.anim.collapse_rotation);
		collapseAnimation.setFillAfter(true);
		view.startAnimation(collapseAnimation);
	}
}
