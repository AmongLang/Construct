package among.construct.condition;

import among.TypeFlags;
import among.obj.AmongList;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ListConditionBuilder extends ConditionBuilder<AmongList, ListCondition, ListConditionBuilder>{
	@Nullable private Map<Integer, Byte> elementIndexToType;
	private byte allElementType = TypeFlags.ANY;

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

	@Override protected void validate(){
		super.validate();
		if(elementIndexToType!=null)
			for(int i : elementIndexToType.keySet())
				if(!Condition.isInRange(minSize, maxSize, i))
					throw new IllegalStateException("Type checking element at index out of range "+i);
	}

	@Override public ListCondition build(){
		return new ListCondition(minSize,
				maxSize,
				warnMinSize,
				warnMaxSize,
				sizeWarningText,
				elementIndexToType,
				allElementType);
	}
}
