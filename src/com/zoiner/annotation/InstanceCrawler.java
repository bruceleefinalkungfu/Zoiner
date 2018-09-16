package com.zoiner.annotation;

import java.lang.reflect.Field;

public interface InstanceCrawler {
	
	/**
	 * @param field : The field that's found when crawling 
	 * @param fieldValue : Value of the said field. Can be null
	 * @param params : Any extra params you wanna pass 
	 */
	public void processNode(Field field, Object fieldValue, Object...params);

	/**
	 * It will have List, null, Map, SimpleEntry, Pair fields etc too.
	 * It's up to you, what to do with them
	 * @param field : The field that's found when crawling 
	 * @param fieldValue : Value of the said field. Can be null
	 * @return : for List, null, Map, SimpleEntry etc, you shouldn't crawl inside. so return false
	 */
	public boolean shouldCrawlInsideField(Field field, Object fieldValue);
	
	/**
	 * Suppose you wanna prepare a tree explaining the whole origin of which field belongs to which class
	 * and that class belongs to which class etc.
	 * <br>
	 * add the field to some sort of tree
	 * @param field
	 * @param fieldValue
	 * @param params
	 */
	public void preCrawl(Field field, Object fieldValue, Object...params);

	/**
	 * Suppose you wanna prepare a tree explaining the whole origin of which field belongs to which class
	 * and that class belongs to which class etc.
	 * <br>
	 * remove the field from the tree
	 * @param field
	 * @param fieldValue
	 * @param params
	 */
	public void postCrawl(Field field, Object fieldValue, Object...params);	
}
