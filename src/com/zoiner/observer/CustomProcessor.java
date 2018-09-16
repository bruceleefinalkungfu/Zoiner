package com.zoiner.observer;

import java.lang.reflect.Field;

public interface CustomProcessor {
	public void process(Field field, Object fieldValue, Object convertedObject);
}
