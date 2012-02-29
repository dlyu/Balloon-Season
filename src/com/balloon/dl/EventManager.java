package com.balloon.dl;

public class EventManager {
	static private EventManager sInstance;
	
	static synchronized public EventManager getInstance()
	{
	    if (sInstance == null)
	      sInstance = new EventManager();
	    return sInstance;
	}
}
