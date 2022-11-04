package among.construct;

import among.construct.ConditionedListConstructorBuilder.BinaryConstructor;
import among.construct.condition.Condition;
import among.construct.condition.ListConditionBuilder;
import among.construct.condition.ObjectConditionBuilder;
import among.obj.Among;
import among.obj.AmongList;
import among.obj.AmongObject;
import among.report.ReportHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An implementation for {@link Constructor} to check for simple conditions of {@link AmongList} or {@link AmongObject}
 * before calling the actual constructor. Each constructor is paired with {@link Condition}, which checks given object
 * and reports error. This class can be used with only one condition, or multiple conditions.<br>
 * If multiple conditions were supplied, it is expected that only one condition will be matched at a time; in other
 * words, each condition is expected to be mutually exclusive. If multiple conditions qualify for given parameter, the
 * first condition in declaration order is chosen. This behavior can be changed with {@link
 * ConditionedConstructorBuilder#useOnlyMatch()}, which makes the constructor to report an error.
 *
 * @param <A> Type of Among value parameter
 * @param <T> Type of the resulting object
 */
public final class ConditionedConstructor<A extends Among, T> implements Constructor<A, T>{
	/**
	 * Make conditioned constructor for {@link AmongList}s.
	 *
	 * @param consumer Builder consumer
	 * @param <T>      Type of the resulting object
	 * @return Newly created constructor instance
	 */
	public static <T> ConditionedConstructor<AmongList, T> listConditions(Consumer<ConditionedListConstructorBuilder<T>> consumer){
		ConditionedListConstructorBuilder<T> b = new ConditionedListConstructorBuilder<>();
		consumer.accept(b);
		return b.build();
	}
	/**
	 * Make conditioned constructor for {@link AmongObject}s.
	 *
	 * @param consumer Builder consumer
	 * @param <T>      Type of the resulting object
	 * @return Newly created constructor instance
	 */
	public static <T> ConditionedConstructor<AmongObject, T> objectConditions(Consumer<ConditionedObjectConstructorBuilder<T>> consumer){
		ConditionedObjectConstructorBuilder<T> b = new ConditionedObjectConstructorBuilder<>();
		consumer.accept(b);
		return b.build();
	}
	/**
	 * Make conditioned constructor for {@link AmongList}s. The constructor will have only one condition.
	 *
	 * @param consumer    Builder consumer
	 * @param constructor The constructor to be called after successful condition checking
	 * @param <T>         Type of the resulting object
	 * @return Newly created constructor instance
	 */
	public static <T> ConditionedConstructor<AmongList, T> listCondition(Consumer<ListConditionBuilder> consumer, Constructor<AmongList, T> constructor){
		return new ConditionedListConstructorBuilder<T>().add(consumer, constructor).build();
	}
	/**
	 * Make conditioned constructor for {@link AmongObject}s. The constructor will have only one condition.
	 *
	 * @param consumer    Builder consumer
	 * @param constructor The constructor to be called after successful condition checking
	 * @param <T>         Type of the resulting object
	 * @return Newly created constructor instance
	 */
	public static <T> ConditionedConstructor<AmongObject, T> objectCondition(Consumer<ObjectConditionBuilder> consumer, Constructor<AmongObject, T> constructor){
		return new ConditionedObjectConstructorBuilder<T>().add(consumer, constructor).build();
	}
	/**
	 * Make conditioned constructor for single-element {@link AmongList}s. The constructor will have only one condition.
	 *
	 * @param constructor The constructor to be called after successful condition checking
	 * @param <T>         Type of the resulting object
	 * @return Newly created constructor instance
	 */
	public static <T> ConditionedConstructor<AmongList, T> unaryCondition(Constructor<Among, T> constructor){
		return new ConditionedListConstructorBuilder<T>().addUnary(c -> {}, constructor).build();
	}
	/**
	 * Make conditioned constructor for single-element {@link AmongList}s. The constructor will have only one condition.
	 *
	 * @param consumer    Builder consumer
	 * @param constructor The constructor to be called after successful condition checking
	 * @param <T>         Type of the resulting object
	 * @return Newly created constructor instance
	 */
	public static <T> ConditionedConstructor<AmongList, T> unaryCondition(Consumer<ListConditionBuilder> consumer, Constructor<Among, T> constructor){
		return new ConditionedListConstructorBuilder<T>().addUnary(consumer, constructor).build();
	}
	/**
	 * Make conditioned constructor for single-element {@link AmongList}s. The constructor will have only one condition.
	 *
	 * @param constructor The constructor to be called after successful condition checking
	 * @param <T>         Type of the resulting object
	 * @return Newly created constructor instance
	 */
	public static <T> ConditionedConstructor<AmongList, T> binaryCondition(BinaryConstructor<T> constructor){
		return new ConditionedListConstructorBuilder<T>().addBinary(c -> {}, constructor).build();
	}
	/**
	 * Make conditioned constructor for single-element {@link AmongList}s. The constructor will have only one condition.
	 *
	 * @param consumer    Builder consumer
	 * @param constructor The constructor to be called after successful condition checking
	 * @param <T>         Type of the resulting object
	 * @return Newly created constructor instance
	 */
	public static <T> ConditionedConstructor<AmongList, T> binaryCondition(Consumer<ListConditionBuilder> consumer, BinaryConstructor<T> constructor){
		return new ConditionedListConstructorBuilder<T>().addBinary(consumer, constructor).build();
	}

	private final Condition<A>[] conditions;
	private final Constructor<A, T>[] constructors;
	private final boolean firstMatch;

	@SuppressWarnings("unchecked") ConditionedConstructor(List<Condition<A>> conditions, List<Constructor<A, T>> constructors, boolean firstMatch){
		this.conditions = conditions.toArray(new Condition[0]);
		this.constructors = constructors.toArray(new Constructor[0]);
		if(this.conditions.length!=this.constructors.length)
			throw new IllegalArgumentException("conditions.size() != constructors.size()");
		for(Condition<A> c : this.conditions) Objects.requireNonNull(c);
		for(Constructor<A, T> c : this.constructors) Objects.requireNonNull(c);
		this.firstMatch = firstMatch;
	}

	@Override @Nullable public T construct(A instance, @Nullable ReportHandler reportHandler){
		switch(conditions.length){
			case 0:
				if(reportHandler!=null)
					reportHandler.reportError(
							"Invalid conditioned constructor: no rules specified.",
							instance.sourcePosition());
				return null;
			case 1:
				return conditions[0].test(instance, reportHandler) ?
						constructors[0].construct(instance, reportHandler) :
						null;
		}
		if(firstMatch){
			for(int i = 0; i<conditions.length; i++)
				if(conditions[i].test(instance))
					return constructors[i].construct(instance, reportHandler);
		}else{
			int match = -1;
			List<Integer> ambiguous = null;
			for(int i = 0; i<conditions.length; i++){
				if(conditions[i].test(instance)){
					if(match<0) match = i;
					else if(reportHandler==null) return null;
					else{
						if(ambiguous==null){
							ambiguous = new ArrayList<>();
							ambiguous.add(match);
						}
						ambiguous.add(i);
					}
				}
			}
			if(ambiguous!=null){
				StringBuilder stb = new StringBuilder();
				stb.append("Matched by multiple conditions:");
				for(int i : ambiguous)
					stb.append("\n  ").append(conditions[i]);
				reportHandler.reportError(stb.toString(), instance.sourcePosition());
			}
			if(match>=0) return constructors[match].construct(instance, reportHandler);
		}
		if(reportHandler!=null){
			StringBuilder stb = new StringBuilder();
			stb.append("None of the defined constructor rules match the parameter");
			for(Condition<A> c : conditions)
				stb.append("\n  ").append(c);
			reportHandler.reportError(stb.toString(), instance.sourcePosition());
		}
		return null;
	}
}
