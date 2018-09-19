package com.zoiner.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zoiner.annotation.DontConvert;
import com.zoiner.annotation.InstanceCrawler;
import com.zoiner.annotation.ZoinerField;
import com.zoiner.annotation.ZoinerTo;
import com.zoiner.observer.CustomProcessor;
import com.zoiner.observer.ZoinerLogger;

/**
 * rename class to Zoner rename ParentField to something less confusing
 * 
 * @author anurag.awasthi
 *
 */
public class ZoinerMapper {

	private static final Set<String> CONSOLE_PRINTABLE_TYPES;

	private final ZinService zinService;
	private final ZinReflect zinReflect;
	private InstanceCrawler crawler;
	private final Object objToConvert;
	private final Object convertedObject;
	private final Class<?> convertedObjectClass;
	private final List<Field> convertObjectAllFields;
	private final Map<String, Object> fieldNameFieldValueCache;
	private final CustomProcessor customProcessor;
	private final ZoinerLogger logger;

	private final boolean shouldProcessStaticVariables;

	static {
		Class<?>[] obfuscateTypesArr = { int.class, double.class, float.class, long.class, boolean.class, char.class,
				Integer.class, Double.class, Float.class, Long.class, Boolean.class, Character.class, BigInteger.class,
				BigDecimal.class, String.class, StringBuilder.class, StringBuffer.class, Date.class };
		CONSOLE_PRINTABLE_TYPES = new HashSet<>();
		for (Class<?> class1 : obfuscateTypesArr) {
			CONSOLE_PRINTABLE_TYPES.add(class1.getName());
		}
	}

	private ZoinerMapper(Object objToConvert, boolean shouldProcessStaticVariables,
			Map<String, Object> fieldNameFieldValueCache, CustomProcessor processor, ZoinerLogger logger) {
		this.zinReflect = new ZinReflect();
		this.zinService = new ZinService();
		this.objToConvert = objToConvert;
		this.customProcessor = processor;
		this.logger = logger;
		this.fieldNameFieldValueCache = fieldNameFieldValueCache;
		try {
			this.convertedObjectClass = zinService.getZonerToClass(objToConvert);
			this.convertObjectAllFields = zinReflect.getAllFields(convertedObjectClass);
			this.convertedObject = zinService.getZonerToClass(objToConvert).newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(
					"Can't create instance of " + zinService.getZonerToClass(objToConvert).getCanonicalName()
							+ " annotated on top of " + objToConvert.getClass().getCanonicalName(),
					e);
		}
		this.shouldProcessStaticVariables = shouldProcessStaticVariables;
	}

	public static class Builder {
		private boolean shouldProcessStaticVariables = false;
		private Map<String, Object> fieldNameFieldValueCache;
		private ZoinerLogger logger;
		private CustomProcessor customProcessor = new CustomProcessor() {

			@Override
			public void process(Field field, Object fieldValue, Object convertedObject) {
			}
		};

		public Builder(ZoinerLogger logger) {
			this.fieldNameFieldValueCache = new HashMap<>();
			this.logger = logger;
		}

		private Builder(Map<String, Object> fieldNameFieldValueCache) {
			this.fieldNameFieldValueCache = fieldNameFieldValueCache;
		}

		public Builder customProcessor(CustomProcessor customProcessor) {
			this.customProcessor = customProcessor;
			return this;
		}

		/**
		 * @param shouldProcessStaticVariables
		 *            : default value false
		 * @return
		 */
		public Builder shouldProcessStaticVariables(boolean shouldProcessStaticVariables) {
			this.shouldProcessStaticVariables = shouldProcessStaticVariables;
			return this;
		}

		/**
		 * @param objToConvert
		 *            : Whatever object you want to convert
		 * @return
		 */
		public ZoinerMapper build(Object objToConvert) {
			return new ZoinerMapper(objToConvert, shouldProcessStaticVariables, fieldNameFieldValueCache,
					customProcessor, logger);
		}
	}

	public <T> T convert() {
		crawler = new InstanceCrawler() {

			@Override
			public boolean shouldCrawlInsideField(Field field, Object fieldValue) {
				return fieldValue != null && !zinReflect.isItCollection(field.getType())
						&& !field.getType().isAssignableFrom(Map.class);
			}

			@Override
			public void processNode(Field field, Object fieldValue, Object... params) {
				zinService.processNode(field, fieldValue, params);
			}

			@Override
			public void preCrawl(Field field, Object fieldValue, Object... params) {
			}

			@Override
			public void postCrawl(Field field, Object fieldValue, Object... params) {
			}
		};
		return zinService.convert(objToConvert);
	}

	private class ZinService {

		public void processNode(Field field, Object fieldValue, Object... params) {
			// First get the value from the cache and set it in the field
			setFromCache(field);
			if (shouldConvertThisField(field)) {
				set(getToFieldName(field), fieldValue);
			}
			putInCache(field, fieldValue);
			customProcessor.process(field, fieldValue, convertedObject);
		}

		private void putInCache(Field field, Object fieldValue) {
			if (shouldCache(field)) {
				fieldNameFieldValueCache.put(getKeyToPutInCache(field), fieldValue);
				logger.log("Class " + objToConvert.getClass().getName() + "'s field " + field.getName()
						+ "'s value is cached");
				logger.verboseLog("Cached value is-", fieldValue);
			}
		}

		private String getKeyToPutInCache(Field field) {
			ZoinerField zonerField = field.getDeclaredAnnotation(ZoinerField.class);
			String key = "";
			if (zonerField != null)
				key = zonerField.cacheWithThisName();
			if (zonerField == null || key == null || key.isEmpty())
				return field.getName();
			return key;
		}

		private void setFromCache(Field field) {
			Object objInCache = fieldNameFieldValueCache.get(getKeyToFetchFromCache(field));
			if (objInCache != null) {
				try {
					field.set(objToConvert, objInCache);
					logger.log("This field got it's value set from cache " + field.getType().getName() + " "
							+ field.getName());
					logger.verboseLog("Fetched cached value is-", objInCache);
				} catch (Exception e) {
					throw new RuntimeException("Failed to set value " + objInCache + " in "
							+ objToConvert.getClass().getName() + "'s field " + field.getName(), e);
				}
			}
		}

		private String getKeyToFetchFromCache(Field field) {
			ZoinerField zonerField = field.getDeclaredAnnotation(ZoinerField.class);
			String key = "";
			if (zonerField != null)
				key = zonerField.getFromCacheUsingThisName();
			if (zonerField == null || key == null || key.isEmpty())
				return field.getName();
			return key;
		}

		private boolean shouldCache(Field field) {
			ZoinerField zonerField = field.getDeclaredAnnotation(ZoinerField.class);
			if (zonerField == null)
				return false;
			String cacheWithThisFieldName = zonerField.cacheWithThisName();
			if (cacheWithThisFieldName == null || cacheWithThisFieldName.isEmpty())
				return false;
			return true;
		}

		private void set(String fieldName, Object fieldValue) {
			for (Field field : convertObjectAllFields) {
				field.setAccessible(true);
				if (field.getName().equals(fieldName)) {
					try {
						field.set(convertedObject, fieldValue);
						logger.log("Set the field " + fieldName + " in " + convertedObject.getClass().getName());
						logger.verboseLog("Field value is-", fieldValue);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new RuntimeException("Failed to set the value " + fieldValue + "s in "
								+ convertedObjectClass.getName() + "'s field " + fieldName, e);
					}
					return;
				}
			}
			throw new RuntimeException("No such field " + fieldName + " in " + convertedObjectClass.getCanonicalName());
		}

		@SuppressWarnings("unchecked")
		private <T> T convert(Object obj) {
			try {
				logger.log("Converting " + obj.getClass().getName() + " to " + convertedObjectClass.getName());
				logger.verboseLog("Object being converted is-", obj);
				zinReflect.crawl(obj);
				return (T) convertedObject;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		private String getToFieldName(Field field) {
			ZoinerField zonerField = field.getDeclaredAnnotation(ZoinerField.class);
			if (zonerField == null || zonerField.toFieldName() == null || zonerField.toFieldName().isEmpty()) {
				return field.getName();
			} else {
				return zonerField.toFieldName();
			}
		}

		private boolean shouldConvertThisField(Field field) {
			return field.getDeclaredAnnotation(DontConvert.class) == null;
		}

		/**
		 * @param obj
		 * @return : returns null if there's no {@link ZoinerTo} annotation on obj's
		 *         class
		 */
		private Class<?> getZonerToClass(Object obj) {
			Class<?> objClass = obj.getClass();
			ZoinerTo toClass = objClass.getDeclaredAnnotation(ZoinerTo.class);
			if (toClass == null)
				return null;
			return toClass.to();
		}

	}

	private class ZinReflect {

		private <T> void crawl(final T value) {
			Class<?> type = value.getClass();
			logger.log("Crawling " + type.getName());
			for (Field field : zinReflect.getAllFields(type)) {
				try {
					field.setAccessible(true);
					if (zinReflect.isFieldStatic(field) && !shouldProcessStaticVariables) {
						logger.log("static field " + field.getName() + " is not processed");
						continue;
					}
					Object fieldValue = field.get(value);
					Class<?> fieldClass = field.getType();
					logger.log("Processing Field => " + fieldClass.getName() + " " + field.getName());
					logger.verboseLog(fieldValue);
					/**
					 * whether it's console printable or not, whether it's null/List or not. It's
					 * processed Crawl after you are done processing the field
					 */
					crawler.processNode(field, fieldValue);
					if (!zinReflect.isFieldConsolePrintable(fieldClass)) {
						if (crawler.shouldCrawlInsideField(field, fieldValue)) {
							crawler.preCrawl(field, fieldValue);
							crawl(fieldValue);
							crawler.postCrawl(field, fieldValue);
						}
					}
				} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException("Crawling exception for the field " + field.getName() + " in class "
							+ type.getCanonicalName(), e);
				}
			}
		}

		private boolean isFieldStatic(final Field field) {
			return Modifier.isStatic(field.getModifiers());
		}

		private boolean isFieldConsolePrintable(final Class<?> fieldType) {
			return CONSOLE_PRINTABLE_TYPES.contains(fieldType.getName());
		}

		private boolean isItCollection(final Class<?> fieldType) {
			return Collection.class.isAssignableFrom(fieldType);
		}

		/**
		 * {@link Class#getFields()} only gives all the public fields from class
		 * hierarchy <br>
		 * {@link Class#getDeclaredFields()} only gives all the fields of that class,
		 * not from superclass <br>
		 * It gives all the public, private, static fields from the class hierarchy
		 * 
		 * @param classOfWhichFieldsAreNeeded
		 * @return
		 */
		public List<Field> getAllFields(Class<?> classOfWhichFieldsAreNeeded) {
			List<Field> output = new ArrayList<>();
			if (classOfWhichFieldsAreNeeded == null || classOfWhichFieldsAreNeeded == Object.class)
				return output;
			output.addAll(Arrays.asList(classOfWhichFieldsAreNeeded.getDeclaredFields()));
			output.addAll(getAllFields(classOfWhichFieldsAreNeeded.getSuperclass()));
			return output;
		}
	}
}