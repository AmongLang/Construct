package among.construct;

import among.construct.condition.Condition;
import among.construct.condition.ConditionBuilder;
import among.construct.condition.ListConditionBuilder;
import among.construct.condition.ObjectConditionBuilder;
import among.obj.Among;
import among.obj.AmongList;
import among.obj.AmongObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class ConditionedConstructorBuilder<
		A extends Among,
		CB extends ConditionBuilder<A, ?, CB>,
		T,
		SELF extends ConditionedConstructorBuilder<A, CB, T, SELF>>{
	private final List<Condition<A>> conditions = new ArrayList<>();
	private final List<Constructor<A, T>> constructors = new ArrayList<>();
	private boolean firstMatch;

	private ConditionedConstructorBuilder(){}

	protected abstract SELF self();

	public SELF add(Consumer<CB> consumer, Constructor<A, T> constructor){
		Objects.requireNonNull(constructor);
		CB b = createConditionBuilder();
		consumer.accept(b);
		conditions.add(b.build());
		constructors.add(constructor);
		return self();
	}

	public SELF useFirstMatch(){
		this.firstMatch = true;
		return self();
	}

	public SELF useOnlyMatch(){
		this.firstMatch = false;
		return self();
	}

	protected abstract CB createConditionBuilder();

	public ConditionedConstructor<A, T> build(){
		return new ConditionedConstructor<>(conditions, constructors, firstMatch);
	}

	public static final class ListConstructorBuilder<T> extends ConditionedConstructorBuilder<AmongList, ListConditionBuilder, T, ListConstructorBuilder<T>>{
		@Override protected ListConstructorBuilder<T> self(){
			return this;
		}
		@Override protected ListConditionBuilder createConditionBuilder(){
			return new ListConditionBuilder();
		}
	}

	public static final class ObjectConstructorBuilder<T> extends ConditionedConstructorBuilder<AmongObject, ObjectConditionBuilder, T, ObjectConstructorBuilder<T>>{
		@Override protected ObjectConstructorBuilder<T> self(){
			return this;
		}
		@Override protected ObjectConditionBuilder createConditionBuilder(){
			return new ObjectConditionBuilder();
		}
	}
}
