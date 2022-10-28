package among.construct;

import among.TypeFlags;
import among.construct.ConditionedConstructor.OperationMode;
import among.construct.condition.Condition;
import among.construct.condition.ListCondition;
import among.construct.condition.ObjectCondition;
import among.construct.condition.ObjectCondition.PropertyCheck;
import among.obj.Among;
import among.obj.AmongList;
import among.obj.AmongObject;
import among.obj.AmongPrimitive;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class ConstructRuleBuilder<T>{
	@Nullable private final ConstructRule<? extends T> baseRule;

	@Nullable private Map<String, Supplier<T>> primitiveRules;
	@Nullable private Constructor<AmongPrimitive, T> genericPrimitiveRule;

	@Nullable private Map<String, ConditionedConstructor<AmongList, T>> listRules;
	@Nullable private ConditionedConstructor<AmongList, T> genericListRule;

	@Nullable private Map<String, ConditionedConstructor<AmongObject, T>> objectRules;
	@Nullable private ConditionedConstructor<AmongObject, T> genericObjectRule;

	@Nullable private Constructor<Among, T> genericValueRule;

	@Nullable private List<ConditionedConstructorBuilder<?, ?>> constructorBuilders;

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

	public ListConstructorBuilder list(String name){
		return addBuilder(new ListConstructorBuilder(Objects.requireNonNull(name), this::registerList));
	}

	public ListConstructorBuilder list(){
		return addBuilder(new ListConstructorBuilder(null, this::registerList));
	}

	private void registerList(ConditionedConstructor<AmongList, T> constructor, @Nullable String name){
		if(name==null){
			if(genericListRule!=null)
				throw new IllegalStateException("Generic construct rule for lists already defined");
			this.genericListRule = constructor;
		}else{
			if(listRules==null) listRules = new HashMap<>();
			else if(listRules.containsKey(name))
				throw new IllegalStateException("Construct rule for list '"+name+"' already defined");
			listRules.put(name, constructor);
		}
	}

	public ObjectConstructorBuilder obj(String name){
		return addBuilder(new ObjectConstructorBuilder(Objects.requireNonNull(name), this::registerObject));
	}

	public ObjectConstructorBuilder obj(){
		return addBuilder(new ObjectConstructorBuilder(null, this::registerObject));
	}

	private void registerObject(ConditionedConstructor<AmongObject, T> constructor, @Nullable String name){
		if(name==null){
			if(genericObjectRule!=null)
				throw new IllegalStateException("Generic construct rule for objects already defined");
			this.genericObjectRule = constructor;
		}else{
			if(objectRules==null) objectRules = new HashMap<>();
			else if(objectRules.containsKey(name))
				throw new IllegalStateException("Construct rule for object '"+name+"' already defined");
			objectRules.put(name, constructor);
		}
	}


	private <B extends ConditionedConstructorBuilder<?, ?>> B addBuilder(B builder){
		if(constructorBuilders==null) constructorBuilders = new ArrayList<>();
		constructorBuilders.add(builder);
		return builder;
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
		if(constructorBuilders!=null){
			for(ConditionedConstructorBuilder<?, ?> b : constructorBuilders) b.register();
			constructorBuilders.clear();
		}
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

	public abstract class ConditionedConstructorBuilder<A extends Among, SELF extends ConditionedConstructorBuilder<A, SELF>>{
		@Nullable protected final String name;
		private final BiConsumer<ConditionedConstructor<A, T>, String> consumer;
		private final List<Condition<A>> conditions = new ArrayList<>();
		private final List<Constructor<A, T>> constructors = new ArrayList<>();

		protected int minSize = -1;
		protected int maxSize = -1;

		protected boolean constructing;

		private OperationMode operationMode = OperationMode.ONLY_MATCH;
		@Nullable private Set<String> alias;

		private ConditionedConstructorBuilder(@Nullable String name, BiConsumer<ConditionedConstructor<A, T>, String> consumer){
			this.name = name;
			this.consumer = consumer;
		}

		protected abstract SELF self();

		public SELF minSize(int minSize){
			constructing = true;
			if(minSize<0) throw new IllegalArgumentException("Index out of bound");
			this.minSize = minSize;
			return self();
		}
		public SELF maxSize(int maxSize){
			constructing = true;
			if(maxSize<0) throw new IllegalArgumentException("Index out of bound");
			this.maxSize = maxSize;
			return self();
		}
		public SELF size(int size){
			constructing = true;
			if(size<0) throw new IllegalArgumentException("Index out of bound");
			this.maxSize = this.minSize = size;
			return self();
		}
		public SELF size(int minSize, int maxSize){
			constructing = true;
			if(minSize<0||maxSize<0) throw new IllegalArgumentException("Index out of bound");
			this.minSize = minSize;
			this.maxSize = maxSize;
			return self();
		}

		public SELF add(Constructor<A, T> constructor){
			constructing = false;
			Objects.requireNonNull(constructor);
			conditions.add(createNewCondition());
			constructors.add(constructor);
			return self();
		}

		public SELF useFirstMatch(){
			this.operationMode = OperationMode.FIRST_MATCH;
			return self();
		}

		public SELF useOnlyMatch(){
			this.operationMode = OperationMode.ONLY_MATCH;
			return self();
		}

		public SELF alias(String alias){
			if(this.name==null)
				throw new IllegalStateException("Generic rules cannot have alias");
			if(this.alias==null) this.alias = new HashSet<>();
			if(!this.alias.add(alias)){
				throw new IllegalStateException("Alias '"+alias+"' already registered");
			}
			return self();
		}

		protected Condition<A> createNewCondition(){
			validate();
			return create();
		}

		protected void validate(){
			if(minSize>=0&&maxSize>=0&&minSize>maxSize) throw new IllegalStateException("minSize > maxSize");
		}
		protected abstract Condition<A> create();

		protected void reset(){
			this.minSize = -1;
			this.maxSize = -1;
		}

		private void register(){
			if(constructing) throw new IllegalStateException("Unfinished constructor "+this);
			if(!this.conditions.isEmpty()){
				ConditionedConstructor<A, T> constructor = new ConditionedConstructor<>(this.conditions, this.constructors, this.operationMode);
				consumer.accept(constructor, name);
				if(this.alias!=null)
					for(String a : alias)
						consumer.accept(constructor, a);
			}
		}

		public ConstructRuleBuilder<T> finish(){
			return ConstructRuleBuilder.this;
		}
	}

	public final class ListConstructorBuilder extends ConditionedConstructorBuilder<AmongList, ListConstructorBuilder>{
		@Nullable private Map<Integer, Byte> elementIndexToType;
		private byte allElementType = TypeFlags.ANY;

		private ListConstructorBuilder(@Nullable String name, BiConsumer<ConditionedConstructor<AmongList, T>, String> consumer){
			super(name, consumer);
		}

		@Override protected ListConstructorBuilder self(){
			return this;
		}

		public ListConstructorBuilder elementType(int index, int type){
			return elementType(index, (byte)type);
		}
		public ListConstructorBuilder elementType(int index, byte type){
			constructing = true;
			if(index<0) throw new IllegalArgumentException("Index out of bound");
			if(elementIndexToType==null) elementIndexToType = new HashMap<>();
			else if(elementIndexToType.containsKey(index))
				throw new IllegalStateException("Index "+index+" is already registered for type checking");
			type = TypeFlags.normalize(type);
			if(type==0) throw new IllegalArgumentException("Impossible type check");
			elementIndexToType.put(index, type);
			return this;
		}
		public ListConstructorBuilder elementType(int type){
			return elementType((byte)type);
		}
		public ListConstructorBuilder elementType(byte type){
			constructing = true;
			type = TypeFlags.normalize(type);
			if(type==0) throw new IllegalArgumentException("Impossible type check");
			this.allElementType = type;
			return this;
		}

		@Override protected Condition<AmongList> create(){
			return new ListCondition(minSize, maxSize, elementIndexToType, allElementType);
		}

		@Override protected void reset(){
			super.reset();
			this.elementIndexToType = null;
			this.allElementType = TypeFlags.ANY;
		}

		@Override protected void validate(){
			super.validate();
			if(elementIndexToType!=null)
				for(int i : elementIndexToType.keySet())
					if(!Condition.isInRange(minSize, maxSize, i))
						throw new IllegalStateException("Type checking element at index out of range "+i);
		}

		@Override public String toString(){
			return name==null ? "Generic list" : "List \""+name+'"';
		}
	}

	public final class ObjectConstructorBuilder extends ConditionedConstructorBuilder<AmongObject, ObjectConstructorBuilder>{
		@Nullable private Map<String, PropertyCheck> expectedProperties;
		private byte allPropertyType = TypeFlags.ANY;

		private ObjectConstructorBuilder(@Nullable String name, BiConsumer<ConditionedConstructor<AmongObject, T>, String> consumer){
			super(name, consumer);
		}

		@Override protected ObjectConstructorBuilder self(){
			return this;
		}

		public ObjectConstructorBuilder property(String key){
			return property(key, TypeFlags.ANY);
		}
		public ObjectConstructorBuilder property(String key, int type){
			return property(key, (byte)type);
		}
		public ObjectConstructorBuilder property(String key, byte type){
			return property(key, type, true);
		}

		public ObjectConstructorBuilder optionalProperty(String key){
			return property(key, TypeFlags.ANY, false);
		}
		public ObjectConstructorBuilder optionalProperty(String key, int type){
			return property(key, (byte)type, false);
		}
		public ObjectConstructorBuilder optionalProperty(String key, byte type){
			return property(key, type, false);
		}

		public ObjectConstructorBuilder property(String key, int type, boolean expected){
			return property(key, (byte)type, expected);
		}
		public ObjectConstructorBuilder property(String key, byte type, boolean expected){
			constructing = true;
			if(expectedProperties==null) expectedProperties = new HashMap<>();
			else if(expectedProperties.containsKey(key))
				throw new IllegalStateException("Property '"+key+"' is already registered for checking");
			type = TypeFlags.normalize(type);
			if(type==0) throw new IllegalArgumentException("Impossible type check");
			expectedProperties.put(key, new PropertyCheck(expected, type));
			return this;
		}

		public ObjectConstructorBuilder property(int type){
			return property((byte)type);
		}
		public ObjectConstructorBuilder property(byte type){
			constructing = true;
			type = TypeFlags.normalize(type);
			if(type==0) throw new IllegalArgumentException("Impossible type check");
			this.allPropertyType = type;
			return this;
		}

		@Override protected Condition<AmongObject> create(){
			return new ObjectCondition(minSize, maxSize, expectedProperties, allPropertyType);
		}

		@Override protected void reset(){
			super.reset();
			this.expectedProperties = null;
			this.allPropertyType = TypeFlags.ANY;
		}

		@Override protected void validate(){
			super.validate();
			if(maxSize>=0&&expectedProperties!=null&&maxSize<expectedProperties.size())
				throw new IllegalStateException("Expecting more property than maximum size defined");
		}

		@Override public String toString(){
			return name==null ? "Generic object" : "Object \""+name+'"';
		}
	}
}
