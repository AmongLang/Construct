package among.construct;

import among.ReportHandler;
import among.obj.Among;
import among.obj.AmongList;
import among.obj.AmongObject;
import among.obj.AmongPrimitive;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Special {@link Constructor} with name-based match to provide easy parsing for complex {@link Among} DSL.<br>
 * Construct rule manages multiple possible choice of constructors with type and name(value for primitive) of the
 * parameter.<br>
 * Construct rules are beneficial when there is multiple possible input and output cases, like code expressions. If
 * there is only one schema applicable at the context, using plain {@link Constructor} or {@link
 * ConditionedConstructor} might be simpler.
 *
 * @param <T> Type of the resulting object
 */
public final class ConstructRule<T> implements Constructor<Among, T>{
	/**
	 * Make a construct rule.
	 *
	 * @param consumer Builder consumer
	 * @param <T>      Output type
	 * @return Newly created construct rule
	 */
	public static <T> ConstructRule<T> make(Consumer<ConstructRuleBuilder<T>> consumer){
		ConstructRuleBuilder<T> b = new ConstructRuleBuilder<>(null);
		consumer.accept(b);
		return b.build();
	}

	@Nullable private final ConstructRule<? extends T> baseRule;

	@Nullable private final Map<String, Supplier<T>> primitiveRules;
	@Nullable private final Constructor<AmongPrimitive, T> genericPrimitiveRule;

	@Nullable private final Map<String, Constructor<AmongList, T>> listRules;
	@Nullable private final Constructor<AmongList, T> genericListRule;

	@Nullable private final Map<String, Constructor<AmongObject, T>> objectRules;
	@Nullable private final Constructor<AmongObject, T> genericObjectRule;

	@Nullable private final Constructor<Among, T> genericValueRule;

	@Nullable private final String errorMessage;

	ConstructRule(
			@Nullable ConstructRule<? extends T> baseRule,
			@Nullable Map<String, Supplier<T>> primitiveRules,
			@Nullable Constructor<AmongPrimitive, T> genericPrimitiveRule,
			@Nullable Map<String, Constructor<AmongList, T>> listRules,
			@Nullable Constructor<AmongList, T> genericListRule,
			@Nullable Map<String, Constructor<AmongObject, T>> objectRules,
			@Nullable Constructor<AmongObject, T> genericObjectRule,
			@Nullable Constructor<Among, T> genericValueRule,
			@Nullable String errorMessage){
		this.baseRule = baseRule;
		this.primitiveRules = primitiveRules;
		this.genericPrimitiveRule = genericPrimitiveRule;
		this.listRules = listRules;
		this.genericListRule = genericListRule;
		this.objectRules = objectRules;
		this.genericObjectRule = genericObjectRule;
		this.genericValueRule = genericValueRule;
		this.errorMessage = errorMessage;
	}

	@Override @Nullable public T construct(Among instance, @Nullable ReportHandler reportHandler){
		if(instance.isPrimitive()){
			Supplier<? extends T> s = searchPrimitiveRule(instance.asPrimitive().getValue());
			if(s!=null) return s.get();
			Constructor<AmongPrimitive, ? extends T> c = searchGenericPrimitiveRule();
			if(c!=null) return c.construct(instance.asPrimitive(), reportHandler);
		}else if(instance.isList()){
			Constructor<AmongList, ? extends T> c = searchListRule(instance.asList());
			if(c==null) c = searchGenericListRule();
			if(c!=null) return c.construct(instance.asList(), reportHandler);
		}else{ // object
			Constructor<AmongObject, ? extends T> c = searchObjRule(instance.asObj());
			if(c==null) c = searchGenericObjRule();
			if(c!=null) return c.construct(instance.asObj(), reportHandler);
		}
		Constructor<Among, ? extends T> c = searchGenericValueRule();
		if(c!=null) return c.construct(instance, reportHandler);
		if(reportHandler!=null)
			reportHandler.reportError(errorMessage!=null ?
							errorMessage : "None of the rules match given object",
					instance.sourcePosition());
		return null;
	}

	/**
	 * Make a construct rule based on this rule. {@code this} will become fallback rule for the new rule; if new rule
	 * cannot find appropriate constructor to apply, {@code this} will be queried.
	 *
	 * @param consumer Builder consumer
	 * @return Newly created construct rule
	 */
	public ConstructRule<T> extend(Consumer<ConstructRuleBuilder<T>> consumer){
		ConstructRuleBuilder<T> b = new ConstructRuleBuilder<>(this);
		consumer.accept(b);
		return b.build();
	}

	@Nullable private Supplier<? extends T> searchPrimitiveRule(String value){
		if(primitiveRules!=null){
			Supplier<T> s = primitiveRules.get(value);
			if(s!=null) return s;
		}
		return baseRule!=null ? baseRule.searchPrimitiveRule(value) : null;
	}

	@Nullable private Constructor<AmongList, ? extends T> searchListRule(AmongList list){
		if(listRules!=null){
			Constructor<AmongList, T> c = listRules.get(list.getName());
			if(c!=null) return c;
		}
		return baseRule!=null ? baseRule.searchListRule(list) : null;
	}

	@Nullable private Constructor<AmongObject, ? extends T> searchObjRule(AmongObject obj){
		if(objectRules!=null){
			Constructor<AmongObject, T> c = objectRules.get(obj.getName());
			if(c!=null) return c;
		}
		return baseRule!=null ? baseRule.searchObjRule(obj) : null;
	}

	@Nullable private Constructor<AmongPrimitive, ? extends T> searchGenericPrimitiveRule(){
		return genericPrimitiveRule!=null ? genericPrimitiveRule :
				baseRule!=null ? baseRule.searchGenericPrimitiveRule() : null;
	}
	@Nullable private Constructor<AmongList, ? extends T> searchGenericListRule(){
		return genericListRule!=null ? genericListRule :
				baseRule!=null ? baseRule.searchGenericListRule() : null;
	}
	@Nullable private Constructor<AmongObject, ? extends T> searchGenericObjRule(){
		return genericObjectRule!=null ? genericObjectRule :
				baseRule!=null ? baseRule.searchGenericObjRule() : null;
	}
	@Nullable private Constructor<Among, ? extends T> searchGenericValueRule(){
		return genericValueRule!=null ? genericValueRule :
				baseRule!=null ? baseRule.searchGenericValueRule() : null;
	}
}
