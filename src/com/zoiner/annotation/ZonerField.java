package com.zoiner.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.zoiner.mapper.ZonerMapper;

/**
 * Suppose source class Emp has a field empId, which maps to target class Employee's employeeId field.
 * <br>
 * Then put use {@link ZonerField#toFieldName()} to specify "employeeId" above empId field.
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
 * {@link ZonerTo#to()}=Company.class<br>
 * Class Company {<br>
 * // Annotate the field with {@link ZonerField} annotation<br>
 * {@link ZonerField#cacheWithThisName()}="xyzId"<br>
 * String id;<br>
 * List<Emp> emps;<br>
 * }<br>
 * Class Emp {<br>
 * {@link ZonerField#getFromCacheUsingThisName()}="xyzId"<br>
 * String compId;<br>
 * Designation desc;<br>
 * }<br>
 * Class Designation {<br>
 * {@link ZonerField#getFromCacheUsingThisName()}="xyzId"<br>
 * String companyId;<br>
 * String jobTitle;<br>
 * }<br>
 * </code>
 * @author anurag.awasthi
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ZonerField {
	String toFieldName() default "";
	String getFromCacheUsingThisName() default "";
	String cacheWithThisName() default "";
}
