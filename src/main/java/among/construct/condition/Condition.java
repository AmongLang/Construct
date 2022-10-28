package among.construct.condition;

import among.ReportHandler;
import among.obj.Among;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public abstract class Condition<A extends Among> implements Predicate<A>{
	private final int minSize;
	private final int maxSize;

	Condition(int minSize, int maxSize){
		this.minSize = minSize;
		this.maxSize = maxSize;
	}

	public int minSize(){
		return minSize;
	}
	public int maxSize(){
		return maxSize;
	}

	public abstract boolean test(A instance, @Nullable ReportHandler reportHandler);

	protected boolean checkSize(A instance, int size, @Nullable ReportHandler reportHandler){
		if(isInRange(minSize, maxSize, size)) return true;
		if(reportHandler!=null){
			StringBuilder stb = new StringBuilder();
			stb.append("Wrong size");
			if(minSize>=0) stb.append(", min: ").append(minSize);
			if(maxSize>=0) stb.append(", max: ").append(maxSize);
			reportHandler.reportError(stb.append(" (provided ").append(size).append(")").toString(), instance.sourcePosition());
		}
		return false;
	}

	public static boolean isInRange(int min, int max, int value){
		return (min<0||value>=min)&&(max<0||value<=max);
	}
}
