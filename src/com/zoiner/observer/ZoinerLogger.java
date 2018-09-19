package com.zoiner.observer;

public interface ZoinerLogger {
	public void logImpl(String msg);

	public boolean isLogEnabled();

	public boolean isVerboseLogEnabled();

	public void verboseLogImpl(Object obj);

	default void log(String msg) {
		if (isLogEnabled())
			logImpl("--ZOIN--" + msg);
	}

	default void verboseLog(Object obj) {
		if (isVerboseLogEnabled())
			verboseLogImpl(obj);
	}

	default void verboseLog(String msg, Object obj) {
		verboseLogStr(msg);
		verboseLog(obj);
	}

	default void verboseLogStr(String msg) {
		if (isVerboseLogEnabled())
			logImpl("--VERB--" + msg);
	}
}