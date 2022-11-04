package among.construct.condition;

import among.ReportHandler;
import among.TypeFlags;
import among.obj.Among;
import among.obj.AmongObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ObjectCondition extends Condition<AmongObject>{
	@Nullable private final Map<String, PropertyCheck> expectedPropertyToType;
	private final byte allPropertyType;
	private final boolean warnOtherProperties;
	@Nullable private final Function<String[], String> propertiesToWarningText;

	public ObjectCondition(
			int minSize,
			int maxSize,
			@Nullable Map<String, PropertyCheck> expectedPropertyToType,
			byte allPropertyType,
			boolean warnOtherProperties,
			@Nullable Function<String[], String> propertiesToWarningText){
		super(minSize, maxSize);
		this.expectedPropertyToType = expectedPropertyToType;
		this.allPropertyType = allPropertyType;
		this.warnOtherProperties = warnOtherProperties;
		this.propertiesToWarningText = propertiesToWarningText;
	}

	@Override public boolean test(AmongObject obj){
		if(!checkSize(obj, obj.size(), null)) return false;
		if(expectedPropertyToType!=null){
			for(Map.Entry<String, PropertyCheck> e : expectedPropertyToType.entrySet()){
				Among property = obj.getProperty(e.getKey());
				if(property==null ? e.getValue().expected : !TypeFlags.matches(e.getValue().type, property))
					return false;
			}
		}
		for(Map.Entry<String, Among> e : obj.properties().entrySet()){
			if((expectedPropertyToType==null||!expectedPropertyToType.containsKey(e.getKey()))&&
					allPropertyType!=TypeFlags.ANY&&
					!TypeFlags.matches(allPropertyType, e.getValue()))
				return false;
		}
		return true;
	}

	@Override public boolean test(AmongObject obj, @Nullable ReportHandler reportHandler){
		if(reportHandler==null) return test(obj);
		if(!checkSize(obj, obj.size(), reportHandler)) return false;
		boolean invalid = false;
		if(expectedPropertyToType!=null){
			for(Map.Entry<String, PropertyCheck> e : expectedPropertyToType.entrySet()){
				Among property = obj.getProperty(e.getKey());
				if(property==null){
					if(e.getValue().expected){
						reportHandler.reportError("Missing property '"+e.getKey()+"'", obj.sourcePosition());
						invalid = true;
					}
				}else if(!TypeFlags.matches(e.getValue().type, property)){
					reportHandler.reportError("Expected "+TypeFlags.toString(e.getValue().type)+" for property '"+e.getKey()+
									"', provided "+TypeFlags.from(property),
							property.sourcePosition());
					invalid = true;
				}
			}
		}
		List<String> redundantProperties = null;
		for(Map.Entry<String, Among> e : obj.properties().entrySet()){
			if(expectedPropertyToType==null||!expectedPropertyToType.containsKey(e.getKey())){
				if(allPropertyType!=TypeFlags.ANY&&!TypeFlags.matches(allPropertyType, e.getValue())){
					reportHandler.reportError("Expected "+TypeFlags.toString(allPropertyType)+
									" for property '"+e.getKey()+"', provided "+TypeFlags.from(e.getValue()),
							e.getValue().sourcePosition());
					invalid = true;
				}else if(warnOtherProperties){
					if(redundantProperties==null) redundantProperties = new ArrayList<>();
					redundantProperties.add(e.getKey());
				}
			}
		}
		if(redundantProperties!=null){
			if(propertiesToWarningText!=null){
				reportHandler.reportWarning(propertiesToWarningText.apply(redundantProperties.toArray(new String[0])));
			}else{
				StringBuilder stb = new StringBuilder().append(redundantProperties.size()).append(" redundant properties");
				for(String s : redundantProperties) stb.append("\n  ").append(s);
				reportHandler.reportWarning(stb.toString());
			}
		}
		return !invalid;
	}

	@Override public String toString(){
		StringBuilder stb = new StringBuilder().append("Object: {");
		boolean first = true;
		if(minSize()>=0){
			first = false;
			stb.append("Min Size: ").append(minSize());
		}
		if(maxSize()>=0){
			if(first) first = false;
			else stb.append(", ");
			stb.append("Max Size: ").append(maxSize());
		}
		if(expectedPropertyToType!=null){
			if(first) first = false;
			else stb.append(", ");
			stb.append("Property to Type: {");
			boolean first2 = true;
			for(Map.Entry<String, PropertyCheck> e : expectedPropertyToType.entrySet()){
				if(first2) first2 = false;
				else stb.append(", ");
				if(!e.getValue().expected) stb.append('[');
				stb.append('\'').append(e.getKey()).append('\'');
				if(!e.getValue().expected) stb.append(']');
				stb.append(": ").append(TypeFlags.toString(e.getValue().type));
			}
			stb.append("}");
		}
		if(allPropertyType!=TypeFlags.ANY){
			if(!first) stb.append(", ");
			stb.append("All Property Type: ").append(TypeFlags.toString(allPropertyType));
		}
		return stb.toString();
	}

	public static final class PropertyCheck{
		public final boolean expected;
		public final byte type;

		public PropertyCheck(boolean expected, byte type){
			this.expected = expected;
			this.type = type;
		}
	}
}
