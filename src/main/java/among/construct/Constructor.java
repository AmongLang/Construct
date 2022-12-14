package among.construct;

import among.exception.Sussy;
import among.obj.Among;
import among.obj.AmongList;
import among.obj.AmongObject;
import among.obj.AmongPrimitive;
import among.report.ReportHandler;
import among.report.ReportType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
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
 * Constructor#tryConstruct(Constructor, ExceptionReporter)}.
 *
 * @param <IN>  Type of the parameter object
 * @param <OUT> Type of the resulting object
 */
@FunctionalInterface
public interface Constructor<IN, OUT>{
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
	@Nullable OUT construct(IN instance, @Nullable ReportHandler reportHandler);

	/**
	 * Construct an object {@code instance} as parameter. If it fails, or if {@code null} is constructed, an exception
	 * is thrown.
	 *
	 * @param instance      Parameter value
	 * @param reportHandler Optional report handler
	 * @return Constructed object
	 * @throws NullPointerException If {@code instance == null}
	 * @throws Sussy                If constructing via {@link Constructor#construct(Object, ReportHandler)} fails
	 * @see Constructor#construct(Object, ReportHandler)
	 */
	default OUT constructExpect(IN instance, @Nullable ReportHandler reportHandler){
		OUT OUT = construct(instance, reportHandler);
		if(OUT==null) throw new Sussy("Construct failed");
		return OUT;
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
	default OUT constructOr(IN instance, OUT fallback, @Nullable ReportHandler reportHandler){
		OUT OUT = construct(instance, reportHandler);
		return OUT!=null ? OUT : fallback;
	}

	/**
	 * Appends given constructor after this constructor. Returned constructor calls {@code this} and {@code other}
	 * sequentially.
	 *
	 * @param other The constructor to append
	 * @param <R>   Type of the new resulting object
	 * @return New constructor
	 */
	default <R> Constructor<IN, R> then(Constructor<OUT, R> other){
		return (instance, reportHandler) -> {
			OUT out = this.construct(instance, reportHandler);
			return out==null ? null : other.construct(out, reportHandler);
		};
	}

	/**
	 * Wraps given constructor inside try-catch block. Any RuntimeExceptions thrown will be caught and logged with
	 * generic error message.<br>
	 *
	 * @param delegate The constructor that might throw an exception
	 * @param <IN>     Type of the parameter object
	 * @param <OUT>    Type of the resulting object
	 * @return Wrapped constructor
	 * @throws NullPointerException If {@code delegate == null}
	 * @see Constructor#tryConstruct(Constructor, String, boolean)
	 * @see Constructor#tryConstruct(Constructor, ExceptionReporter)
	 */
	static <IN, OUT> Constructor<IN, OUT> tryConstruct(Constructor<? super IN, OUT> delegate){
		return tryConstruct(delegate, "Cannot construct object due to an unexpected exception", true);
	}
	/**
	 * Wraps given constructor inside try-catch block. Any RuntimeExceptions thrown will be caught and logged with
	 * custom error message.<br>
	 *
	 * @param constructor  The constructor that might throw an exception
	 * @param errorMessage Message to report
	 * @param logException Whether to include exception in report or not
	 * @param <IN>         Type of the parameter object
	 * @param <OUT>        Type of the resulting object
	 * @return Wrapped constructor
	 * @throws NullPointerException If {@code constructor == null}
	 * @see Constructor#tryConstruct(Constructor, ExceptionReporter)
	 */
	static <IN, OUT> Constructor<IN, OUT> tryConstruct(Constructor<? super IN, OUT> constructor, String errorMessage, boolean logException){
		return tryConstruct(constructor, (reportHandler, inst, e) ->
				reportHandler.report(ReportType.ERROR, errorMessage, inst instanceof Among ? ((Among)inst).sourcePosition() : -1, logException ? e : null));
	}
	/**
	 * Wraps given constructor inside try-catch block. Any RuntimeExceptions thrown will be caught and logged with
	 * {@link ExceptionReporter}.<br>
	 *
	 * @param constructor       The constructor that might throw an exception
	 * @param exceptionReporter Optional exception reporter
	 * @param <IN>              Type of Among value parameter
	 * @param <OUT>             Type of the resulting object
	 * @return Wrapped constructor
	 * @throws NullPointerException If {@code constructor == null}
	 */
	static <IN, OUT> Constructor<IN, OUT> tryConstruct(Constructor<? super IN, OUT> constructor, @Nullable ExceptionReporter<? super IN> exceptionReporter){
		Objects.requireNonNull(constructor);
		return (instance, reportHandler) -> {
			try{
				return constructor.construct(instance, reportHandler);
			}catch(RuntimeException ex){
				if(reportHandler!=null&&exceptionReporter!=null)
					exceptionReporter.reportException(reportHandler, instance, ex);
				return null;
			}
		};
	}

	/**
	 * Create new constructor which produces list of given element. {@link AmongList} is expected for input.
	 *
	 * @param elementConstructor Constructor for elements
	 * @param <E>                Type of the elements
	 * @return Constructor of list
	 * @see Constructor#listOf(Constructor, boolean)
	 */
	static <E> Constructor<AmongList, List<E>> listOf(Constructor<Among, E> elementConstructor){
		return listOf(elementConstructor, false);
	}
	/**
	 * Create new constructor which produces list of given element. {@link AmongList} is expected for input.
	 *
	 * @param elementConstructor  Constructor for elements
	 * @param requiresUnnamedList If {@code true}, matching list is required to be unnamed.
	 * @param <E>                 Type of the elements
	 * @return Constructor of list
	 */
	static <E> Constructor<AmongList, List<E>> listOf(
			Constructor<Among, E> elementConstructor,
			boolean requiresUnnamedList){
		Objects.requireNonNull(elementConstructor);
		return (instance, reportHandler) -> {
			if(requiresUnnamedList&&instance.hasName()){
				if(reportHandler!=null) reportHandler.reportError("List should be unnamed", instance.sourcePosition());
				return null;
			}
			List<E> list = new ArrayList<>(instance.size());
			for(int i = 0; i<instance.size(); i++){
				E e = elementConstructor.construct(instance.get(i), reportHandler);
				if(e==null) return null;
				list.add(e);
			}
			return list;
		};
	}
	/**
	 * Create new constructor which produces list of given element.<br>
	 * If an unnamed {@link AmongList} is provided as input, it will be read as list, with each element read with
	 * {@code
	 * elementConstructor}.<br>
	 * If the list is named, or is not a list, then it will be read as element. The element read will be returned
	 * wrapped inside singleton list.
	 *
	 * @param elementConstructor Constructor for elements
	 * @param <E>                Type of the elements
	 * @return Constructor of list
	 * @see Constructor#listOf(Constructor, boolean)
	 */
	static <E> Constructor<Among, List<E>> listOrElementOf(Constructor<Among, E> elementConstructor){
		Objects.requireNonNull(elementConstructor);
		return (instance, reportHandler) -> {
			if(instance.isList()){
				AmongList l = instance.asList();
				if(!l.hasName()){
					List<E> list = new ArrayList<>(l.size());
					for(int i = 0; i<l.size(); i++){
						E e = elementConstructor.construct(l.get(i), reportHandler);
						if(e==null) return null;
						list.add(e);
					}
					return list;
				}
			}
			E e = elementConstructor.construct(instance, reportHandler);
			return e==null ? null : Collections.singletonList(e);
		};
	}

	/**
	 * Generify given constructor. Simple type checking is inserted before calling the constructor.
	 *
	 * @param constructor Constructor to generify
	 * @param <OUT>       Type of the resulting object
	 * @return Generified constructor
	 */
	static <OUT> Constructor<Among, OUT> generifyList(Constructor<AmongList, OUT> constructor){
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
	 * @param <OUT>       Type of the resulting object
	 * @return Generified constructor
	 */
	static <OUT> Constructor<Among, OUT> generifyObject(Constructor<AmongObject, OUT> constructor){
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
	 * @param <OUT>       Type of the resulting object
	 * @return Generified constructor
	 */
	static <OUT> Constructor<Among, OUT> generifyValue(Constructor<AmongPrimitive, OUT> constructor){
		return (instance, reportHandler) -> {
			if(instance.isPrimitive()) return constructor.construct(instance.asPrimitive(), reportHandler);
			if(reportHandler!=null) reportHandler.reportError("Expected value", instance.sourcePosition());
			return null;
		};
	}

	/**
	 * Exception reporter for {@link Constructor#tryConstruct(Constructor, ExceptionReporter)}.
	 *
	 * @param <IN> Type of the parameter object
	 */
	@FunctionalInterface
	interface ExceptionReporter<IN>{
		void reportException(ReportHandler reportHandler, IN instance, Throwable exception);
	}
}
