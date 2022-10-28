package among.construct;

import among.ReportHandler;
import among.ReportType;
import among.exception.Sussy;
import among.obj.Among;
import among.obj.AmongList;
import among.obj.AmongObject;
import among.obj.AmongPrimitive;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base interface for object construction.<br>
 * Constructors transform one parameter object to another. Although constructors are designed to encapsulate parsing
 * {@link Among} objects to various objects, constructors are applicable to all type of inputs.<br>
 * Constructors expect successful parsing result to be nonnull. If {@code null} is returned, the action is considered
 * as failure or aborted.<br>
 * Constructors are often used with report handler attached to collect any errors that may occur. Using provided report
 * handler is strongly recommended, rather than using external debugging interface, for example console logging. No
 * report handler being passed means this action is silent. If any error occurs, the action should return {@code
 * null} without passing error information.<br>
 * Exception should not be thrown except for the case of parameter being {@code null}. Any constructors that may
 * produce exception should be handled appropriately before use, with methods like {@link
 * Constructor#tryConstruct(Constructor, ErrorReporter)}.
 *
 * @param <A> Type of Among value parameter
 * @param <T> Type of the resulting object
 */
@FunctionalInterface
public interface Constructor<A extends Among, T>{
	/**
	 * Construct an object using {@code instance} as parameter. If it fails, {@code null} is returned. An appropriate
	 * message will be logged to {@code reportHandler} if it's supplied.<br>
	 * No report handler being passed means this action is silent. If any error occurs, the action should return {@code
	 * null} without passing error information.
	 *
	 * @param instance      Parameter value
	 * @param reportHandler Optional report handler
	 * @return Constructed object, or {@code null} if it failed
	 * @throws NullPointerException If {@code instance == null}
	 */
	@Nullable T construct(A instance, @Nullable ReportHandler reportHandler);

	/**
	 * Construct an object {@code instance} as parameter. If it fails, or if {@code null} is constructed, an exception
	 * is thrown.
	 *
	 * @param instance      Parameter value
	 * @param reportHandler Optional report handler
	 * @return Constructed object
	 * @throws NullPointerException If {@code instance == null}
	 * @throws Sussy                If constructing via {@link Constructor#construct(Among, ReportHandler)} fails
	 * @see Constructor#construct(Among, ReportHandler)
	 */
	default T constructExpect(A instance, @Nullable ReportHandler reportHandler){
		T t = construct(instance, reportHandler);
		if(t==null) throw new Sussy("Construct failed");
		return t;
	}

	/**
	 * Construct an object using {@code instance} as parameter. If it fails, or if {@code null} is constructed, {@code
	 * fallback} is returned. If failed, an appropriate message will be logged to {@code reportHandler} if it's
	 * supplied.
	 *
	 * @param instance      Parameter value
	 * @param fallback      Fallback value
	 * @param reportHandler Optional report handler
	 * @return Constructed object, or {@code fallback} if it failed
	 * @throws NullPointerException If {@code instance == null}
	 */
	default T constructOr(A instance, T fallback, @Nullable ReportHandler reportHandler){
		T t = construct(instance, reportHandler);
		return t!=null ? t : fallback;
	}

	/**
	 * Wraps given constructor inside try-catch block. Any RuntimeExceptions thrown will be caught and logged with
	 * generic error message.<br>
	 *
	 * @param delegate The constructor that might throw an exception
	 * @param <A>      Type of Among value parameter
	 * @param <T>      Type of the resulting object
	 * @return Wrapped constructor
	 * @throws NullPointerException If {@code delegate == null}
	 * @see Constructor#tryConstruct(Constructor, String, boolean)
	 * @see Constructor#tryConstruct(Constructor, ErrorReporter)
	 */
	static <A extends Among, T> Constructor<A, T> tryConstruct(Constructor<? super A, T> delegate){
		return tryConstruct(delegate, "Cannot construct object due to an unexpected exception", true);
	}
	/**
	 * Wraps given constructor inside try-catch block. Any RuntimeExceptions thrown will be caught and logged with
	 * custom error message.<br>
	 *
	 * @param constructor  The constructor that might throw an exception
	 * @param errorMessage Message to report
	 * @param logException Whether to include exception in report or not
	 * @param <A>          Type of Among value parameter
	 * @param <T>          Type of the resulting object
	 * @return Wrapped constructor
	 * @throws NullPointerException If {@code constructor == null}
	 * @see Constructor#tryConstruct(Constructor, ErrorReporter)
	 */
	static <A extends Among, T> Constructor<A, T> tryConstruct(Constructor<? super A, T> constructor, String errorMessage, boolean logException){
		return tryConstruct(constructor, (reportHandler, inst, e) -> reportHandler.report(ReportType.ERROR, errorMessage, inst.sourcePosition(), logException ? e : null));
	}
	/**
	 * Wraps given constructor inside try-catch block. Any RuntimeExceptions thrown will be caught and logged with
	 * {@link ErrorReporter}.<br>
	 *
	 * @param constructor      The constructor that might throw an exception
	 * @param exceptionHandler An exception handler
	 * @param <A>              Type of Among value parameter
	 * @param <T>              Type of the resulting object
	 * @return Wrapped constructor
	 * @throws NullPointerException If {@code constructor == null}
	 */
	static <A extends Among, T> Constructor<A, T> tryConstruct(Constructor<? super A, T> constructor, @Nullable ErrorReporter<? super A> exceptionHandler){
		Objects.requireNonNull(constructor);
		return (instance, reportHandler) -> {
			try{
				return constructor.construct(instance, reportHandler);
			}catch(RuntimeException ex){
				if(reportHandler!=null&&exceptionHandler!=null)
					exceptionHandler.reportError(reportHandler, instance, ex);
				return null;
			}
		};
	}

	/**
	 * Create new constructor which produces list of given element. {@link AmongList} is expected for input.
	 *
	 * @param elementConstructor Constructor for elements
	 * @param <T>                Type of the resulting object
	 * @return Constructor of list
	 * @see Constructor#listOf(Constructor, boolean)
	 */
	static <T> Constructor<AmongList, List<T>> listOf(Constructor<Among, T> elementConstructor){
		return listOf(elementConstructor, false);
	}
	/**
	 * Create new constructor which produces list of given element. {@link AmongList} is expected for input.
	 *
	 * @param elementConstructor  Constructor for elements
	 * @param requiresUnnamedList If {@code true}, matching list is required to be unnamed.
	 * @param <T>                 Type of the resulting object
	 * @return Constructor of list
	 */
	static <T> Constructor<AmongList, List<T>> listOf(
			Constructor<Among, T> elementConstructor,
			boolean requiresUnnamedList){
		Objects.requireNonNull(elementConstructor);
		return (instance, reportHandler) -> {
			if(requiresUnnamedList&&instance.hasName()){
				if(reportHandler!=null) reportHandler.reportError("List should be unnamed", instance.sourcePosition());
				return null;
			}
			List<T> list = new ArrayList<>(instance.size());
			for(int i = 0; i<instance.size(); i++)
				list.add(elementConstructor.construct(instance.get(i), reportHandler));
			return list;
		};
	}

	/**
	 * Generify given constructor. Simple type checking is inserted before calling the constructor.
	 *
	 * @param constructor Constructor to generify
	 * @param <T>         Type of the resulting object
	 * @return Generified constructor
	 */
	static <T> Constructor<Among, T> generifyList(Constructor<AmongList, T> constructor){
		return (instance, reportHandler) -> {
			if(instance.isList()) return constructor.construct(instance.asList(), reportHandler);
			if(reportHandler!=null) reportHandler.reportError("Expected list", instance.sourcePosition());
			return null;
		};
	}

	/**
	 * Generify given constructor. Simple type checking is inserted before calling the constructor.
	 *
	 * @param constructor Constructor to generify
	 * @param <T>         Type of the resulting object
	 * @return Generified constructor
	 */
	static <T> Constructor<Among, T> generifyObject(Constructor<AmongObject, T> constructor){
		return (instance, reportHandler) -> {
			if(instance.isObj()) return constructor.construct(instance.asObj(), reportHandler);
			if(reportHandler!=null) reportHandler.reportError("Expected object", instance.sourcePosition());
			return null;
		};
	}

	/**
	 * Generify given constructor. Simple type checking is inserted before calling the constructor.
	 *
	 * @param constructor Constructor to generify
	 * @param <T>         Type of the resulting object
	 * @return Generified constructor
	 */
	static <T> Constructor<Among, T> generifyValue(Constructor<AmongPrimitive, T> constructor){
		return (instance, reportHandler) -> {
			if(instance.isPrimitive()) return constructor.construct(instance.asPrimitive(), reportHandler);
			if(reportHandler!=null) reportHandler.reportError("Expected value", instance.sourcePosition());
			return null;
		};
	}

	/**
	 * Error reporter for {@link Constructor#tryConstruct(Constructor, ErrorReporter)}.
	 *
	 * @param <A> Type of Among value parameter
	 */
	@FunctionalInterface
	interface ErrorReporter<A extends Among>{
		void reportError(ReportHandler reportHandler, A instance, Throwable exception);
	}
}
