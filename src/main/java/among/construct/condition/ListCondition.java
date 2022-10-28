package among.construct.condition;

import among.ReportHandler;
import among.TypeFlags;
import among.obj.AmongList;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class ListCondition extends Condition<AmongList>{
	@Nullable private final Map<Integer, Byte> elementIndexToType;
	private final byte allElementType;

	public ListCondition(int minSize, int maxSize, @Nullable Map<Integer, Byte> elementIndexToType, byte allElementType){
		super(minSize, maxSize);
		this.elementIndexToType = elementIndexToType;
		this.allElementType = allElementType;
	}

	@Override public boolean test(AmongList list){
		if(!checkSize(list, list.size(), null)) return false;
		for(int i = 0; i<list.size(); i++){
			byte type;
			if(elementIndexToType!=null&&elementIndexToType.containsKey(i)) type = elementIndexToType.get(i);
			else if(allElementType!=TypeFlags.ANY) type = allElementType;
			else continue;
			if(!TypeFlags.matches(type, list.get(i))) return false;
		}
		return true;
	}

	@Override public boolean test(AmongList list, @Nullable ReportHandler reportHandler){
		if(reportHandler==null) return test(list);
		if(!checkSize(list, list.size(), reportHandler)) return false;
		boolean invalid = false;
		for(int i = 0; i<list.size(); i++){
			byte type;
			if(elementIndexToType!=null&&elementIndexToType.containsKey(i)) type = elementIndexToType.get(i);
			else if(allElementType!=TypeFlags.ANY) type = allElementType;
			else continue;
			if(!TypeFlags.matches(type, list.get(i))){
				invalid = true;
				reportHandler.reportError("Expected "+TypeFlags.toString(type)+" at "+i+
								", provided "+TypeFlags.from(list.get(i)),
						list.get(i).sourcePosition());
			}
		}
		return !invalid;
	}

	@Override public String toString(){
		StringBuilder stb = new StringBuilder().append("List: {");
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
		if(elementIndexToType!=null){
			if(first) first = false;
			else stb.append(", ");
			stb.append("Element Index to Type: {");
			boolean first2 = true;
			for(Map.Entry<Integer, Byte> e : elementIndexToType.entrySet()){
				if(first2) first2 = false;
				else stb.append(", ");
				stb.append(e.getKey()).append(": ").append(TypeFlags.toString(e.getValue()));
			}
			stb.append("}");
		}
		if(allElementType!=TypeFlags.ANY){
			if(!first) stb.append(", ");
			stb.append("All Element Type: ").append(TypeFlags.toString(allElementType));
		}
		return stb.append("}").toString();
	}
}
