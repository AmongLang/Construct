package among.construct;

import among.obj.Among;
import among.obj.AmongList;
import among.obj.AmongObject;
import among.obj.AmongPrimitive;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ConstructRuleBuilder<T>{
	@Nullable private final ConstructRule<? extends T> baseRule;

	@Nullable private Map<String, Supplier<T>> primitiveRules;
	@Nullable private Constructor<AmongPrimitive, T> genericPrimitiveRule;

	@Nullable private Map<String, Constructor<AmongList, T>> listRules;
	@Nullable private Constructor<AmongList, T> genericListRule;

	@Nullable private Map<String, Constructor<AmongObject, T>> objectRules;
	@Nullable private Constructor<AmongObject, T> genericObjectRule;

	@Nullable private Constructor<Among, T> genericValueRule;

	@Nullable private String errorMessage;

	public ConstructRuleBuilder(@Nullable ConstructRule<? extends T> baseRule){
		this.baseRule = baseRule;
	}

	public ConstructRuleBuilder<T> primitive(String value, Supplier<T> constructor){
		if(primitiveRules==null) primitiveRules = new HashMap<>();
		else if(primitiveRules.containsKey(value))
			throw new IllegalStateException("Construct rule for primitive value '"+value+"' already defined");
		primitiveRules.put(value, constructor);
		return this;
	}

	public ConstructRuleBuilder<T> primitive(Constructor<AmongPrimitive, T> constructor){
		if(genericPrimitiveRule!=null)
			throw new IllegalStateException("Generic construct rule for primitives already defined");
		genericPrimitiveRule = constructor;
		return this;
	}

	public ConstructRuleBuilder<T> list(String name, Constructor<AmongList, T> constructor){
		if(listRules==null) listRules = new HashMap<>();
		if(listRules.putIfAbsent(name, constructor)!=null)
			throw new IllegalStateException("Construct rule for list '"+name+"' already defined");
		return this;
	}

	public ConstructRuleBuilder<T> list(String[] names, Constructor<AmongList, T> constructor){
		for(String name : names) list(name, constructor);
		return this;
	}

	public ConstructRuleBuilder<T> genericList(Constructor<AmongList, T> constructor){
		if(genericListRule!=null)
			throw new IllegalStateException("Generic construct rule for lists already defined");
		this.genericListRule = constructor;
		return this;
	}

	public ConstructRuleBuilder<T> obj(String name, Constructor<AmongObject, T> constructor){
		if(objectRules==null) objectRules = new HashMap<>();
		if(objectRules.putIfAbsent(name, constructor)!=null)
			throw new IllegalStateException("Construct rule for object '"+name+"' already defined");
		return this;
	}

	public ConstructRuleBuilder<T> obj(String[] names, Constructor<AmongObject, T> constructor){
		for(String name : names) obj(name, constructor);
		return this;
	}

	public ConstructRuleBuilder<T> genericObj(Constructor<AmongObject, T> constructor){
		if(genericObjectRule!=null)
			throw new IllegalStateException("Generic construct rule for objects already defined");
		this.genericObjectRule = constructor;
		return this;
	}

	public ConstructRuleBuilder<T> all(Constructor<Among, T> constructor){
		if(genericValueRule!=null)
			throw new IllegalStateException("Generic construct rule already defined");
		genericValueRule = constructor;
		return this;
	}

	public ConstructRuleBuilder<T> errorMessage(String message){
		if(errorMessage!=null)
			throw new IllegalStateException("Error message already defined");
		errorMessage = message;
		return this;
	}

	public ConstructRule<T> build(){
		return new ConstructRule<>(baseRule,
				primitiveRules,
				genericPrimitiveRule,
				listRules,
				genericListRule,
				objectRules,
				genericObjectRule,
				genericValueRule,
				errorMessage);
	}
}
