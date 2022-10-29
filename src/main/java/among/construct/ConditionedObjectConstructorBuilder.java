package among.construct;

import among.construct.condition.ObjectConditionBuilder;
import among.obj.AmongObject;

public final class ConditionedObjectConstructorBuilder<T> extends ConditionedConstructorBuilder<AmongObject, ObjectConditionBuilder, T, ConditionedObjectConstructorBuilder<T>>{
	@Override protected ConditionedObjectConstructorBuilder<T> self(){
		return this;
	}
	@Override protected ObjectConditionBuilder createConditionBuilder(){
		return new ObjectConditionBuilder();
	}
}
