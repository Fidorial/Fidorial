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
 *  op ::= 'get' | 'set' | 'modify' | 'call'
 *  role ::= 'any' | 'owner'}</pre> <p>
 *
 * The operations denote the following:<br>
 * <ul>
 * <li> get - read the field/reference, or invoke a non-mutating accessor
 * <li> set - reassign what the field/reference points to
 * <li> modify - invoke a mutating operation on the referenced object itself, without reassigning the reference
 * <li> call - the method invocation itself requires the given role, independent of any specific field being
 * read, reassigned, or mutated (e.g. the entire method body assumes it is running on the owning thread).
 * Used on methods that aren't simple accessors/mutators; mixing {@code call} with {@code get}/{@code set}/
 * {@code modify} in the same contract is redundant and should be avoided
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
 * {@code @ThreadContract("call -> owner")} - the referenced method must only be called from the owning region thread<br>
 * {@code @ThreadContract("owner")} - get, set, modify and call are all confined to the owning region thread<br>
 *
 * @apiNote When not present, the thread contract is implicitly assumed to be {@code any}
 * @since 0.1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
public @interface ThreadContract {

    /**
     * Contains the contract clauses describing which thread(s) may perform
     * each operation on the annotated element.
     */
    String value();

    /**
     * Whether this contract overrides the per-member contracts of whatever it's applied to,
     * rather than only constraining direct get/set/modify/call access to this element itself.
     * <p>
     * Set this only when the surrounding context already guarantees the stated role for every
     * operation reachable through this reference — e.g. a field on an event that is only ever
     * posted from within a scheduled, owner-thread callback, where nothing downstream can violate
     * a stricter contract declared elsewhere because the whole call chain is already confined to
     * that thread. Misapplying this on an unscheduled context silently masks real violations that
     * the per-member contracts would otherwise catch.
     */
    boolean transitive() default false;
}
