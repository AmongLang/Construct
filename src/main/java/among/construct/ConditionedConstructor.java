package among.construct;

import among.ReportHandler;
import among.construct.condition.Condition;
import among.obj.Among;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ConditionedConstructor<A extends Among, T> implements Constructor<A, T>{
	private final List<Condition<A>> conditions;
	private final List<Constructor<A, T>> constructors;
	private final OperationMode mode;

	public ConditionedConstructor(List<Condition<A>> conditions, List<Constructor<A, T>> constructors, OperationMode mode){
		if(conditions.size()!=constructors.size())
			throw new IllegalArgumentException("conditions.size() != constructors.size()");
		this.conditions = conditions;
		this.constructors = constructors;
		this.mode = Objects.requireNonNull(mode);
	}

	@Override @Nullable public T construct(A instance, @Nullable ReportHandler reportHandler){
		switch(conditions.size()){
			case 0:
				if(reportHandler!=null)
					reportHandler.reportError(
							"One of constructor rule has no elements registered. This shouldn't happen.",
							instance.sourcePosition());
				return null;
			case 1:
				if(conditions.get(0).test(instance, reportHandler))
					return constructors.get(0).construct(instance, reportHandler);
		}
		if(mode==OperationMode.FIRST_MATCH){
			for(int i = 0; i<conditions.size(); i++)
				if(conditions.get(i).test(instance))
					return constructors.get(i).construct(instance, reportHandler);
		}else{
			int match = -1;
			List<Integer> ambiguous = null;
			for(int i = 0; i<conditions.size(); i++){
				if(conditions.get(i).test(instance)){
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
					stb.append("\n  ").append(conditions.get(i));
				reportHandler.reportError(stb.toString());
			}
			if(match>=0) return constructors.get(match).construct(instance, reportHandler);
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
