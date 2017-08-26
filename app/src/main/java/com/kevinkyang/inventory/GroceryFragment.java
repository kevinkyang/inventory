package com.kevinkyang.inventory;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GroceryFragment extends Fragment implements CustomFragment {
	private MainActivity parent;

	private ItemManager itemManager = null;

	private RecyclerView itemRecyclerView;
	private GroceryItemRVAdapter itemRVAdapter;
	private LinearLayoutManager layoutManager;
	private ItemTouchHelper itemTouchHelper;

	private boolean initFinished = false;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_inventory, container, false);
//		inventoryListView = (ListView) view.findViewById(R.id.inventory_listview); TODO
		itemRecyclerView = (RecyclerView) view.findViewById(R.id.inventory_rv);
		registerForContextMenu(itemRecyclerView);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		// this fragment can only be attached to MainActivity
		parent = (MainActivity) getActivity();

		itemManager = ItemManager.getInstance();

		itemRVAdapter =
				new GroceryItemRVAdapter(
						itemManager.getGroceryListItems(), this);
		itemRecyclerView.setAdapter(itemRVAdapter);
		layoutManager = new LinearLayoutManager(parent);
		itemRecyclerView.setLayoutManager(layoutManager);
		DividerItemDecoration divider = new DividerItemDecoration(parent, DividerItemDecoration.VERTICAL);
		itemRecyclerView.addItemDecoration(divider);
		itemRecyclerView.setHasFixedSize(false);
		itemTouchHelper = new ItemTouchHelper(
				new ListItemTouchHelperCallback(
						getContext(), itemRVAdapter, true));
		itemTouchHelper.attachToRecyclerView(itemRecyclerView);

		initFinished = true;
		if (savedInstanceState != null) {
			parent.changeToCurrentList();
		}
		super.onActivityCreated(savedInstanceState);
	}
// TODO solve this context menu problem, this fragment has a different context menu than the invfragment but there's no layout for that menu atm
//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//		if (parent == null) {
//			return;
//		}
//
//		super.onCreateContextMenu(menu, v, menuInfo);
//		MenuInflater inflater = parent.getMenuInflater();
//		inflater.inflate(R.menu.list_item_context_menu, menu);
//	}
//
//	@Override
//	public boolean onContextItemSelected(MenuItem item) {
//		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//		switch (item.getItemId()) {
//			case R.id.list_item_delete:
//				ItemBase it = itemAdapter.getItem(menuInfo.position);
//				if (itemManager.swapList(it)) {
//					itemAdapter.notifyDataSetChanged();
//					return true;
//				}
//				else return false;
//			default:
//				return super.onContextItemSelected(item);
//		}
//		return super.onContextItemSelected(item);
//	}

	/**
	 * Call this when changes occur in other parts of the
	 * app that affect the inventory list.
	 */
	@Override
	public void refresh() {
		itemRVAdapter.setItemsList(itemManager.getGroceryListItems());
	}

	@Override
	public MainActivity getParent() {
		return parent;
	}

	@Override
	public void itemAdded(Item item) {
		itemRVAdapter.addItem(item, itemRVAdapter.getItemCount());
		layoutManager.scrollToPosition(itemRVAdapter.getItemCount() - 1);
	}

	@Override
	public void itemSaved(int position) {
		itemRVAdapter.changeItem(position);
	}

	@Override
	public void removeItem(Item item, int position) {
		if (itemManager.removeItem(item)) {
			itemRVAdapter.removeItem(position);
		}
	}

	/**
	 * Removes the item from the grocery list and
	 * adds it to the inventory it belongs to.
	 * @param item the item to be removed.
	 */
	@Override
	public void swapList(Item item, int position) {
		item.setInGroceryList(false);
		itemManager.updateItem(item);
		itemRVAdapter.removeItem(position);
	}

	@Override
	public void undoDelete(Item item, Integer position) {
		itemManager.addItem(item);
		itemRVAdapter.addItem(item, position);
	}

	@Override
	public void undoSwapList(Item item, Integer position) {
		item.setInGroceryList(true);
		itemManager.updateItem(item);
		itemRVAdapter.addItem(item, position);
	}

	public boolean isInitFinished() {
		return initFinished;
	}
}
