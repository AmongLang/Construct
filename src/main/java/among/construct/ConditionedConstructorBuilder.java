package among.construct;

import among.construct.condition.Condition;
import among.construct.condition.ConditionBuilder;
import among.obj.Among;

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
	private boolean firstMatch = true;

	protected abstract SELF self();

	protected SELF addConstructor(CB condition, Constructor<A, T> constructor){
		conditions.add(condition.build());
		constructors.add(Objects.requireNonNull(constructor));
		return self();
	}

	public SELF add(Consumer<CB> consumer, Constructor<A, T> constructor){
		CB b = createConditionBuilder();
		consumer.accept(b);
		return addConstructor(b, constructor);
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
}
