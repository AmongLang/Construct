package among.construct.condition;

import among.TypeFlags;
import among.obj.AmongObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ObjectConditionBuilder extends ConditionBuilder<AmongObject, ObjectCondition, ObjectConditionBuilder>{
	@Nullable private Map<String, ObjectCondition.PropertyCheck> expectedProperties;
	private byte allPropertyType = TypeFlags.ANY;

	@Override protected ObjectConditionBuilder self(){
		return this;
	}

	public ObjectConditionBuilder property(String key){
		return property(key, TypeFlags.ANY);
	}
	public ObjectConditionBuilder property(String key, int type){
		return property(key, (byte)type);
	}
	public ObjectConditionBuilder property(String key, byte type){
		return property(key, type, true);
	}

	public ObjectConditionBuilder optionalProperty(String key){
		return property(key, TypeFlags.ANY, false);
	}
	public ObjectConditionBuilder optionalProperty(String key, int type){
		return property(key, (byte)type, false);
	}
	public ObjectConditionBuilder optionalProperty(String key, byte type){
		return property(key, type, false);
	}

	public ObjectConditionBuilder property(String key, int type, boolean expected){
		return property(key, (byte)type, expected);
	}
	public ObjectConditionBuilder property(String key, byte type, boolean expected){
		if(expectedProperties==null) expectedProperties = new HashMap<>();
		else if(expectedProperties.containsKey(key))
			throw new IllegalStateException("Property '"+key+"' is already registered for checking");
		type = TypeFlags.normalize(type);
		if(type==0) throw new IllegalArgumentException("Impossible type check");
		expectedProperties.put(key, new ObjectCondition.PropertyCheck(expected, type));
		return this;
	}

	public ObjectConditionBuilder property(int type){
		return property((byte)type);
	}
	public ObjectConditionBuilder property(byte type){
		type = TypeFlags.normalize(type);
		if(type==0) throw new IllegalArgumentException("Impossible type check");
		this.allPropertyType = type;
		return this;
	}

	@Override protected void validate(){
		super.validate();
		if(maxSize>=0&&expectedProperties!=null&&maxSize<expectedProperties.size())
			throw new IllegalStateException("Expecting more property than maximum size defined");
	}

	@Override public ObjectCondition build(){
		return new ObjectCondition(minSize,
				maxSize,
				warnMinSize,
				warnMaxSize ,
				sizeWarningText,
				expectedProperties,
				allPropertyType);
	}
}
