package among.construct.condition;

import among.obj.Among;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;

public abstract class ConditionBuilder<A extends Among, C extends Condition<A>, SELF extends ConditionBuilder<A, C, SELF>>{
	protected int minSize = -1;
	protected int maxSize = -1;
	protected int warnMinSize = -1;
	protected int warnMaxSize = -1;
	@Nullable protected IntFunction<String> sizeWarningText;

	ConditionBuilder(){}

	protected abstract SELF self();

	public SELF minSize(int minSize){
		if(minSize<0) throw new IllegalArgumentException("Index out of bound");
		this.minSize = minSize;
		return self();
	}
	public SELF maxSize(int maxSize){
		if(maxSize<0) throw new IllegalArgumentException("Index out of bound");
		this.maxSize = maxSize;
		return self();
	}
	public SELF size(int size){
		if(size<0) throw new IllegalArgumentException("Index out of bound");
		this.maxSize = this.minSize = size;
		return self();
	}
	public SELF size(int minSize, int maxSize){
		if(minSize<0||maxSize<0) throw new IllegalArgumentException("Index out of bound");
		this.minSize = minSize;
		this.maxSize = maxSize;
		return self();
	}

	public SELF warnIfSmaller(int minSize){
		return warnIfSmaller(minSize, null);
	}
	public SELF warnIfSmaller(int minSize, @Nullable IntFunction<String> sizeWarningText){
		if(minSize<0) throw new IllegalArgumentException("Index out of bound");
		this.warnMinSize = minSize;
		this.sizeWarningText = sizeWarningText;
		return self();
	}

	public SELF warnIfBigger(int maxSize){
		return warnIfBigger(maxSize, null);
	}
	public SELF warnIfBigger(int maxSize, @Nullable IntFunction<String> sizeWarningText){
		if(maxSize<0) throw new IllegalArgumentException("Index out of bound");
		this.warnMaxSize = maxSize;
		this.sizeWarningText = sizeWarningText;
		return self();
	}

	public SELF warnIfWrongSize(int size){
		return warnIfOutOfRange(size, size, null);
	}
	public SELF warnIfWrongSize(int size, @Nullable IntFunction<String> sizeWarningText){
		return warnIfOutOfRange(size, size, sizeWarningText);
	}

	public SELF warnIfOutOfRange(int minSize, int maxSize){
		return warnIfOutOfRange(minSize, maxSize, null);
	}
	public SELF warnIfOutOfRange(int minSize, int maxSize, @Nullable IntFunction<String> sizeWarningText){
		if(minSize<0||maxSize<0) throw new IllegalArgumentException("Index out of bound");
		this.warnMinSize = minSize;
		this.warnMaxSize = maxSize;
		this.sizeWarningText = sizeWarningText;
		return self();
	}

	protected void validate(){
		if(minSize>=0&&maxSize>=0&&minSize>maxSize) throw new IllegalStateException("minSize > maxSize");
	}

	public abstract C build();
}
