package com.mealfire;

import java.util.ArrayList;
import java.util.List;

public class PagedList<T> extends ArrayList<T> {
	private static final long serialVersionUID = 1576299212947229769L;
	private int total;
	
	public PagedList(List<T> list, int total) {
		super(list);
		this.total = total;
	}
	
	public int getTotal() { return total; }
}
