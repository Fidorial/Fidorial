package fr.fidorial.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies which thread(s) may read, reassign, or modify the annotated
 * field, accessor, or parameter. Can be used by tools for thread-ownership
 * analysis
 * <p>
 * Thread contract has the following syntax:<br>
 * <pre>{@code
 *  contract ::= (clause ';')* clause | role
 *  clause ::= op '->' role
 *  op ::= 'get' | 'set' | 'modify'
 *  role ::= 'any' | 'owner'}</pre> <p>
 *
 * The operations denote the following:<br>
 * <ul>
 * <li> get - read the field/reference, or invoke a non-mutating accessor
 * <li> set - reassign what the field/reference points to
 * <li> modify - invoke a mutating operation on the referenced object itself, without reassigning the reference
 * </ul>
 * <p>
 * The roles denote the following:<br>
 * <ul>
 * <li> any - accessible from any thread
 * <li> owner - accessible only from the owning region thread
 * </ul>
 * <p>
 * Examples:<p>
 * {@code @ThreadContract("get -> any; set -> owner")} - readable from any thread, but only the owning region thread may reassign it<br>
 * {@code @ThreadContract("get -> any; modify -> owner")} - the reference may be obtained from any thread, but the referenced object must only be mutated from the owning region thread<br>
 * {@code @ThreadContract("owner")} - get, set, and modify are all confined to the owning region thread<br>
 *
 * @apiNote When not present, the thread contract is implicitly assumed to be {@code any}
 * @since 0.1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface ThreadContract {

    /**
     * Contains the contract clauses describing which thread(s) may perform
     * each operation on the annotated element.
     */
    String value();
}
