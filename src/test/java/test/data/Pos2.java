package test.data;

import among.TypeFlags;
import among.construct.ConditionedConstructor;
import among.construct.ConstructRule;
import among.construct.Constructor;
import among.construct.Constructors;
import among.obj.Among;

import java.util.Objects;

public final class Pos2{
	public final int x, y;

	public Pos2(int x, int y){
		this.x = x;
		this.y = y;
	}

	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		Pos2 pos2 = (Pos2)o;
		return x==pos2.x&&y==pos2.y;
	}
	@Override public int hashCode(){
		return Objects.hash(x, y);
	}

	@Override public String toString(){
		return "Pos2{"+
				"x="+x+
				", y="+y+
				'}';
	}

	public static final Constructor<Among, Pos2> CONSTRUCTOR = ConstructRule.make(_b -> _b
			.list("", ConditionedConstructor.listCondition(
					c -> c.size(2),
					(l, r) -> {
						Integer x = Constructors.INT.construct(l.get(0), r);
						Integer y = Constructors.INT.construct(l.get(1), r);
						if(x==null||y==null) return null;
						return new Pos2(x, y);
					}))
			.obj("", ConditionedConstructor.objectCondition(c -> c
							.property("x", TypeFlags.PRIMITIVE)
							.property("y", TypeFlags.PRIMITIVE)
							.warnOtherProperties(),
					(o, r) -> {
						Integer x = Constructors.INT.construct(o.getProperty("x"), r);
						Integer y = Constructors.INT.construct(o.getProperty("y"), r);
						if(x==null||y==null) return null;
						return new Pos2(x, y);
					})));
}
