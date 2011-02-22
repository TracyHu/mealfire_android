package com.mealfire;

public class UserException extends Exception {
	private static final long serialVersionUID = 9067182884350966847L;
	private boolean cancelActivity = false;

	public UserException(String string) {
		super(string);
	}
	
	public UserException(String string, boolean cancelActivity) {
		super(string);
		this.cancelActivity = cancelActivity;
	}
	
	public boolean shouldCancelActivity() {
		return cancelActivity;
	}
}
