package com.zoiner.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.zoiner.mapper.ZoinerMapper;

/**
 * Suppose source class Emp has a field empId, which maps to target class Employee's employeeId field.
 * <br>
 * Then put use {@link ZoinerField#toFieldName()} to specify "employeeId" above empId field.
 * <br><br>
 * Suppose this is the class structure<br>
 * <code>
 * Class Company {<br>
 * String id;<br>
 * List<Emp> emps;<br>
 * }<br>
 * Class Emp {<br>
 * String compId;<br>
 * Designation desc;<br>
 * }<br>
 * Class Designation {<br>
 * String companyId;<br>
 * String jobTitle;<br>
 * }<br>
 * </code>
 * When preparing Company object, you have already set the value in id field.
 * Same value has to be filled in all the employees' compId and designations' companyId.
 * It can be achieved using this.<br>
 * // Annotate the class with itself<br>
 * {@link ZoinerTo#to()}=Company.class<br>
 * Class Company {<br>
 * // Annotate the field with {@link ZoinerField} annotation<br>
 * {@link ZoinerField#cacheWithThisName()}="xyzId"<br>
 * String id;<br>
 * List<Emp> emps;<br>
 * }<br>
 * Class Emp {<br>
 * {@link ZoinerField#getFromCacheUsingThisName()}="xyzId"<br>
 * String compId;<br>
 * Designation desc;<br>
 * }<br>
 * Class Designation {<br>
 * {@link ZoinerField#getFromCacheUsingThisName()}="xyzId"<br>
 * String companyId;<br>
 * String jobTitle;<br>
 * }<br>
 * </code>
 * @author anurag.awasthi
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ZoinerField {
	String toFieldName() default "";
	String getFromCacheUsingThisName() default "";
	String cacheWithThisName() default "";
}
