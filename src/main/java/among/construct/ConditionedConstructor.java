package among.construct;

import among.ReportHandler;
import among.construct.ConditionedConstructorBuilder.ListConstructorBuilder;
import among.construct.ConditionedConstructorBuilder.ObjectConstructorBuilder;
import among.construct.condition.Condition;
import among.construct.condition.ListConditionBuilder;
import among.construct.condition.ObjectConditionBuilder;
import among.obj.Among;
import among.obj.AmongList;
import among.obj.AmongObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class ConditionedConstructor<A extends Among, T> implements Constructor<A, T>{
	public static <T> ConditionedConstructor<AmongList, T> listConditions(Consumer<ListConstructorBuilder<T>> consumer){
		ListConstructorBuilder<T> b = new ListConstructorBuilder<>();
		consumer.accept(b);
		return b.build();
	}

	public static <T> ConditionedConstructor<AmongObject, T> objectConditions(Consumer<ObjectConstructorBuilder<T>> consumer){
		ObjectConstructorBuilder<T> b = new ObjectConstructorBuilder<>();
		consumer.accept(b);
		return b.build();
	}

	public static <T> ConditionedConstructor<AmongList, T> listCondition(Consumer<ListConditionBuilder> consumer, Constructor<AmongList, T> constructor){
		return new ListConstructorBuilder<T>().add(consumer, constructor).build();
	}

	public static <T> ConditionedConstructor<AmongObject, T> objectCondition(Consumer<ObjectConditionBuilder> consumer, Constructor<AmongObject, T> constructor){
		return new ObjectConstructorBuilder<T>().add(consumer, constructor).build();
	}

	private final Condition<A>[] conditions;
	private final Constructor<A, T>[] constructors;
	private final OperationMode mode;

	@SuppressWarnings("unchecked")
	public ConditionedConstructor(List<Condition<A>> conditions, List<Constructor<A, T>> constructors, OperationMode mode){
		this.conditions = conditions.toArray(new Condition[0]);
		this.constructors = constructors.toArray(new Constructor[0]);
		if(this.conditions.length!=this.constructors.length)
			throw new IllegalArgumentException("conditions.size() != constructors.size()");
		for(Condition<A> c : this.conditions) Objects.requireNonNull(c);
		for(Constructor<A, T> c : this.constructors) Objects.requireNonNull(c);
		this.mode = Objects.requireNonNull(mode);
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
		if(mode==OperationMode.FIRST_MATCH){
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
				reportHandler.reportError(stb.toString());
			}
			if(match>=0) return constructors[match].construct(instance, reportHandler);
		}
		if(reportHandler!=null){
			StringBuilder stb = new StringBuilder();
			stb.append("None of the defined constructor rules match the parameter");
			for(Condition<A> c : conditions)
				stb.append("\n  ").append(c);
			reportHandler.reportError(stb.toString());
		}
		return null;
	}

	public enum OperationMode{
		FIRST_MATCH,
		ONLY_MATCH
	}
}
