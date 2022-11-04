package among.construct.condition;

import among.obj.Among;
import among.report.ReportHandler;
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
		boolean inRange = isInRange(minSize, maxSize, size);
		if(reportHandler!=null&&!inRange)
			reportHandler.reportError(buildDefaultInvalidSizeMessage(minSize, maxSize, size), instance.sourcePosition());
		return inRange;
	}

	public static boolean isInRange(int min, int max, int value){
		return (min<0||value>=min)&&(max<0||value<=max);
	}

	protected static String buildDefaultInvalidSizeMessage(int min, int max, int value){
		StringBuilder stb = new StringBuilder();
		stb.append("Invalid size: ");
		if(min==max) stb.append("expected ").append(min);
		else if(min>=0&&max>=0) stb.append("expected ").append(min).append(" ~ ").append(max);
		else if(min>=0) stb.append("minimum ").append(min).append(" expected");
		else stb.append("maximum ").append(min).append(" expected");
		return stb.append(", provided ").append(value).toString();
	}

	protected boolean appendSizeString(StringBuilder stb){
		if(minSize<0&&maxSize<0) return false;
		if(minSize==maxSize) stb.append("Size: ").append(minSize);
		else if(minSize<0) stb.append("Max Size: ").append(maxSize);
		else if(maxSize<0) stb.append("Min Size: ").append(minSize);
		else stb.append("Size: ").append(minSize).append("~").append(maxSize);
		return true;
	}
}
