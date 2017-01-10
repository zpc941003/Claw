package com.ntu.claw.receiver;

import java.util.Observable;

/**
 * 观察者模式
 */
public class ObservableObject extends Observable {
	private static ObservableObject instance = new ObservableObject();
	
	public static ObservableObject getInstance(){
		return instance;
	}
	
	private ObservableObject() {
    }
		
	public void updateValue(Object data) {
        synchronized (this) {
            setChanged();
            notifyObservers(data);
        }
    }
}
