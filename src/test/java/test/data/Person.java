package test.data;

import among.TypeFlags;
import among.construct.ConditionedConstructor;
import among.construct.Constructor;
import among.construct.Constructors;
import among.obj.Among;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Person{
	public final String name;
	public final String description;
	public final double height;
	public final double weight;
	public final List<String> notes;

	public Person(String name, String description, double height, double weight, List<String> notes){
		this.name = name;
		this.description = description;
		this.height = height;
		this.weight = weight;
		this.notes = notes;
	}

	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		Person person = (Person)o;
		return Double.compare(person.height, height)==0&&
				Double.compare(person.weight, weight)==0&&
				name.equals(person.name)&&
				description.equals(person.description)&&
				notes.equals(person.notes);
	}
	@Override public int hashCode(){
		return Objects.hash(name, description, height, weight, notes);
	}

	@Override public String toString(){
		return "Person{"+
				"name='"+name+'\''+
				", description='"+description+'\''+
				", height="+height+
				", weight="+weight+
				", notes="+notes+
				'}';
	}

	public static final Constructor<Among, Person> CONSTRUCTOR = Constructor.generifyObject(
			ConditionedConstructor.objectCondition(c -> c
							.property("name", TypeFlags.PRIMITIVE)
							.property("description", TypeFlags.PRIMITIVE)
							.property("height", TypeFlags.PRIMITIVE)
							.property("weight", TypeFlags.PRIMITIVE)
							.optionalProperty("notes", TypeFlags.LIST)
							.warnOtherProperties(),
					(instance, reportHandler) -> {
						Double height = Constructors.DOUBLE.construct(instance.expectProperty("height"), reportHandler);
						if(height==null) return null;
						Double weight = Constructors.DOUBLE.construct(instance.expectProperty("weight"), reportHandler);
						if(weight==null) return null;
						List<String> notes = instance.hasProperty("notes") ?
								Constructor.listOf(Constructors.VALUE)
										.construct(instance.expectProperty("notes").asList(), reportHandler) :
								Collections.emptyList();
						if(notes==null) return null;
						return new Person(
								instance.expectProperty("name").asPrimitive().getValue(),
								instance.expectProperty("description").asPrimitive().getValue(),
								height, weight, notes);
					}));
}
