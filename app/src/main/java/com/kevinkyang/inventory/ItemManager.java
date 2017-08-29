package com.kevinkyang.inventory;

import android.content.Context;

import java.util.ArrayList;

/**
 * singleton class that holds the item data
 */

public class ItemManager {
	private ArrayList<Item> items = null;
	private DBManager dbManager = DBManager.getInstance();

	private static ItemManager instance = null;
	private Context context = null;

	public static ItemManager getInstance() {
		if (instance == null) {
			instance = new ItemManager();
		}
		return instance;
	}

	private ItemManager() {

	}

	public void init(Context context) {
		this.context = context;
		if (!dbManager.isInitialized()) {
			dbManager.init(context);
		}
		items = dbManager.getItems();
	}

	public boolean isInitialized() {
		return context != null;
	}

	public void addItem(Item item) {
		items.add(item);
		dbManager.addItem(item);
	}

	public void updateItem(Item item) {
		dbManager.updateItem(item);
	}

	public boolean removeItem(Item item) {
		if (dbManager.removeItem(item) == 1) {
			items.remove(item);
			return true;
		}
		else return false;
	}

	/**
	 * Get all items except for those in the
	 * grocery list.
	 * @return List of items in the inventory.
	 */
	public ArrayList<Item> getInventoryItems() {
		ArrayList<Item> results = new ArrayList<Item>();
		for (Item i : items) {
			if (!i.isInGroceryList()) {
				results.add(i);
			}
		}
		return results;
	}

	/**
	 * TODO description here
	 * @param inventory the name of the inventory the items
	 *                  are located in, or null to return
	 *                  all items.
	 * @return a subset of the inventory items based on the
	 * inventory it belongs to.
	 */
	public ArrayList<Item> getItemsByInventory(String inventory) {
		return getItemsByInventory(inventory, 3);
	}

	public ArrayList<Item> getItemsByInventory(
			String inventory, int expirationInterval) {
		if (inventory == null) {
			return getInventoryItems();
		} else if (inventory.equals("Expiring")) {
			return getExpiringItems(expirationInterval);
		}

		ArrayList<Item> results = new ArrayList<Item>();
		for (Item i : items) {
			if (!i.isInGroceryList() &&
					i.getInventory().equals(inventory)) {
				results.add(i);
			}
		}

		return results;
	}

	/**
	 * TODO inefficient alg, description needed
	 * @param inventory the name of the inventory the items
	 *                  are located in, or null for all items.
	 * @return the number of items in the specified inventory
	 */
	public int getInventoryItemCount(String inventory) {
		return getItemsByInventory(inventory).size();
	}

	public ArrayList<Item> getGroceryListItems() {
		ArrayList<Item> results = new ArrayList<Item>();
		for (Item i : items) {
			if (i.isInGroceryList()) {
				results.add(i);
			}
		}

		return results;
	}

	public ArrayList<Item> getExpiringItems(int rangeInDays) {
		ArrayList<Item> expiring = new ArrayList<Item>();
		for (Item item : items) {
			if (item.isInGroceryList()) {
				// ignore grocery items
				continue;
			}

			String expiresDate = item.getExpiresDate();
			if (expiresDate.isEmpty()) {
				// item has no expiration set
				continue;
			}

			int daysUntilExpiration =
					TimeManager.getDateDifferenceInDays(
							TimeManager.getDateTimeLocal(),
							item.getExpiresDate());
			item.setDaysUntilExpiration(daysUntilExpiration);
			if (daysUntilExpiration <= rangeInDays) {
				expiring.add(item);
			}
		}

		return expiring;
	}
}