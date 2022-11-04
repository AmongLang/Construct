package among.construct.condition;

import among.obj.Among;

public abstract class ConditionBuilder<A extends Among, C extends Condition<A>, SELF extends ConditionBuilder<A, C, SELF>>{
	protected int minSize = -1;
	protected int maxSize = -1;

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

	protected void validate(){
		if(minSize>=0&&maxSize>=0&&minSize>maxSize) throw new IllegalStateException("minSize > maxSize");
	}

	public final C build(){
		validate();
		return make();
	}
	protected abstract C make();
}
