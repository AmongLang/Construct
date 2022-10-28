package test;

import among.TypeFlags;
import among.construct.ConstructRule;
import among.construct.Constructor;
import among.construct.Constructors;

import java.util.Collections;
import java.util.List;

public final class SampleData{
	public final String name;
	public final String description;
	public final double height;
	public final double weight;
	public final List<String> notes;

	public SampleData(String name, String description, double height, double weight, List<String> notes){
		this.name = name;
		this.description = description;
		this.height = height;
		this.weight = weight;
		this.notes = notes;
	}

	@Override public String toString(){
		return "SampleData{"+
				"name='"+name+'\''+
				", description='"+description+'\''+
				", height="+height+
				", weight="+weight+
				", notes="+notes+
				'}';
	}

	public static final ConstructRule<SampleData> RULE = ConstructRule.make(b -> b.obj()
			.property("name", TypeFlags.PRIMITIVE)
			.property("description", TypeFlags.PRIMITIVE)
			.property("height", TypeFlags.PRIMITIVE)
			.property("weight", TypeFlags.PRIMITIVE)
			.optionalProperty("notes", TypeFlags.LIST)
			.add((instance, reportHandler) -> {
				Double height = Constructors.DOUBLE.construct(instance.expectProperty("height"), reportHandler);
				if(height==null) return null;
				Double weight = Constructors.DOUBLE.construct(instance.expectProperty("weight"), reportHandler);
				if(weight==null) return null;
				List<String> notes = instance.hasProperty("notes") ?
						Constructor.listOf(Constructors.VALUE)
								.construct(instance.expectProperty("notes").asList(), reportHandler) :
						Collections.emptyList();
				return new SampleData(
						instance.expectProperty("name").asPrimitive().getValue(),
						instance.expectProperty("description").asPrimitive().getValue(),
						height, weight, notes);
			}));
}
