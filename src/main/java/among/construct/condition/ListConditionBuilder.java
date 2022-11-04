package among.construct.condition;

import among.TypeFlags;
import among.obj.AmongList;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

public final class ListConditionBuilder extends ConditionBuilder<AmongList, ListCondition, ListConditionBuilder>{
	@Nullable private Map<Integer, Byte> elementIndexToType;
	private byte allElementType = TypeFlags.ANY;
	private int warnMinSize = -1;
	private int warnMaxSize = -1;
	@Nullable private IntFunction<String> sizeWarningText;

	@Override protected ListConditionBuilder self(){
		return this;
	}

	public ListConditionBuilder elementType(int index, int type){
		return elementType(index, (byte)type);
	}
	public ListConditionBuilder elementType(int index, byte type){
		if(index<0) throw new IllegalArgumentException("Index out of bound");
		if(elementIndexToType==null) elementIndexToType = new HashMap<>();
		else if(elementIndexToType.containsKey(index))
			throw new IllegalStateException("Index "+index+" is already registered for type checking");
		type = TypeFlags.normalize(type);
		if(type==0) throw new IllegalArgumentException("Impossible type check");
		elementIndexToType.put(index, type);
		return this;
	}

	public ListConditionBuilder elementType(int type){
		return elementType((byte)type);
	}
	public ListConditionBuilder elementType(byte type){
		type = TypeFlags.normalize(type);
		if(type==0) throw new IllegalArgumentException("Impossible type check");
		this.allElementType = type;
		return this;
	}

	public ListConditionBuilder warnIfSmaller(int minSize){
		return warnIfSmaller(minSize, null);
	}
	public ListConditionBuilder warnIfSmaller(int minSize, @Nullable IntFunction<String> sizeWarningText){
		if(minSize<0) throw new IllegalArgumentException("Index out of bound");
		this.warnMinSize = minSize;
		this.sizeWarningText = sizeWarningText;
		return this;
	}

	public ListConditionBuilder warnIfBigger(int maxSize){
		return warnIfBigger(maxSize, null);
	}
	public ListConditionBuilder warnIfBigger(int maxSize, @Nullable IntFunction<String> sizeWarningText){
		if(maxSize<0) throw new IllegalArgumentException("Index out of bound");
		this.warnMaxSize = maxSize;
		this.sizeWarningText = sizeWarningText;
		return this;
	}

	public ListConditionBuilder warnIfWrongSize(int size){
		return warnIfOutOfRange(size, size, null);
	}
	public ListConditionBuilder warnIfWrongSize(int size, @Nullable IntFunction<String> sizeWarningText){
		return warnIfOutOfRange(size, size, sizeWarningText);
	}

	public ListConditionBuilder warnIfOutOfRange(int minSize, int maxSize){
		return warnIfOutOfRange(minSize, maxSize, null);
	}
	public ListConditionBuilder warnIfOutOfRange(int minSize, int maxSize, @Nullable IntFunction<String> sizeWarningText){
		if(minSize<0||maxSize<0) throw new IllegalArgumentException("Index out of bound");
		this.warnMinSize = minSize;
		this.warnMaxSize = maxSize;
		this.sizeWarningText = sizeWarningText;
		return this;
	}

	@Override protected void validate(){
		super.validate();
		if(elementIndexToType!=null)
			for(int i : elementIndexToType.keySet())
				if(maxSize>=0&&maxSize<=i)
					throw new IllegalStateException("Type checking element at index out of range "+i);
	}

	@Override protected ListCondition make(){
		return new ListCondition(minSize,
				maxSize,
				warnMinSize,
				warnMaxSize,
				sizeWarningText,
				elementIndexToType,
				allElementType);
	}
}
