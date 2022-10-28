package among.construct;

import among.ReportHandler;
import among.obj.Among;
import among.obj.AmongList;
import among.obj.AmongPrimitive;
import org.jetbrains.annotations.Nullable;

import static among.construct.ConditionedConstructor.listCondition;
import static among.construct.ConditionedConstructor.listConditions;

/**
 * Default set of constructors.
 */
public final class Constructors{
	private Constructors(){}

	/**
	 * "Constructor" returning the instance itself.
	 */
	public static final Constructor<Among, Among> IDENTITY = (instance, reportHandler) -> instance;

	/**
	 * Constructor producing value of the primitive.
	 */
	public static final Constructor<Among, String> VALUE = Constructor.generifyValue(
			(instance, reportHandler) -> instance.getValue());

	/**
	 * {@link AmongPrimitive#getBoolValue()} as constructor.
	 */
	public static final Constructor<Among, Boolean> BOOL = Constructor.generifyValue(
			Constructor.tryConstruct((instance, reportHandler) ->
					instance.getBoolValue(), "Expected bool", false));
	/**
	 * {@link AmongPrimitive#getIntValue()} as constructor.
	 */
	public static final Constructor<Among, Integer> INT = Constructor.generifyValue(
			Constructor.tryConstruct((instance, reportHandler) ->
					instance.getIntValue(), "Expected int", false));
	/**
	 * {@link AmongPrimitive#getLongValue()} as constructor.
	 */
	public static final Constructor<Among, Long> LONG = Constructor.generifyValue(
			Constructor.tryConstruct((instance, reportHandler) ->
					instance.getLongValue(), "Expected long", false));
	/**
	 * {@link AmongPrimitive#getFloatValue()} as constructor.
	 */
	public static final Constructor<Among, Float> FLOAT = Constructor.generifyValue(
			Constructor.tryConstruct((instance, reportHandler) ->
					instance.getFloatValue(), "Expected number", false));
	/**
	 * {@link AmongPrimitive#getDoubleValue()} as constructor.
	 */
	public static final Constructor<Among, Double> DOUBLE = Constructor.generifyValue(
			Constructor.tryConstruct((instance, reportHandler) ->
					instance.getDoubleValue(), "Expected number", false));

	private static final Constructor<Among, Boolean> EVAL_BOOL = (instance, reportHandler) -> {
		Among a = Constructors.EVAL.construct(instance, reportHandler);
		return a!=null ? BOOL.construct(a, reportHandler) : null;
	};
	private static final Constructor<Among, Double> EVAL_NUM = (instance, reportHandler) -> {
		Among a = Constructors.EVAL.construct(instance, reportHandler);
		return a!=null ? DOUBLE.construct(a, reportHandler) : null;
	};

	/**
	 * One-to-one recreation of {@code eval} default library. Evaluated object is converted back to {@link Among}
	 * object, as original library does.
	 */
	public static final ConstructRule<Among> EVAL = ConstructRule.make(builder ->
			builder.list("||", listCondition(c -> c.minSize(2), (instance, reportHandler) -> binaryBool(instance, reportHandler, BinaryBoolOp.OR_SS)))
					.list("&&", listCondition(c -> c.minSize(2), (instance, reportHandler) -> binaryBool(instance, reportHandler, BinaryBoolOp.AND_SS)))
					.list("|", listCondition(c -> c.minSize(2), (instance, reportHandler) -> binaryBool(instance, reportHandler, BinaryBoolOp.OR)))
					.list("&", listCondition(c -> c.minSize(2), (instance, reportHandler) -> binaryBool(instance, reportHandler, BinaryBoolOp.AND)))
					.list(new String[]{"==", "="}, listCondition(c -> c.minSize(2), (instance, reportHandler) -> eq(instance, reportHandler, true)))
					.list("!=", listCondition(c -> c.minSize(2), (instance, reportHandler) -> eq(instance, reportHandler, false)))
					.list(">", listCondition(c -> c.minSize(2), (instance, reportHandler) -> binary(instance, reportHandler, BinaryNumOp.GT)))
					.list("<", listCondition(c -> c.minSize(2), (instance, reportHandler) -> binary(instance, reportHandler, BinaryNumOp.LT)))
					.list(">=", listCondition(c -> c.minSize(2), (instance, reportHandler) -> binary(instance, reportHandler, BinaryNumOp.GTEQ)))
					.list("<=", listCondition(c -> c.minSize(2), (instance, reportHandler) -> binary(instance, reportHandler, BinaryNumOp.LTEQ)))
					.list("+", listConditions(b2 -> b2
							.add(c -> c.minSize(2), (instance, reportHandler) -> binary(instance, reportHandler, BinaryNumOp.ADD))
							.add(c -> c.size(1), (instance, reportHandler) -> {
								Double num = EVAL_NUM.construct(instance.get(0), reportHandler);
								return num==null ? null : Among.value(num);
							})))
					.list("-", listConditions(b2 -> b2
							.add(c -> c.minSize(2), (instance, reportHandler) -> binary(instance, reportHandler, BinaryNumOp.SUB))
							.add(c -> c.size(1), (instance, reportHandler) -> {
								Double num = EVAL_NUM.construct(instance.get(0), reportHandler);
								return num==null ? null : Among.value(-num);
							})))
					.list("*", listCondition(c -> c.minSize(2), (instance, reportHandler) -> binary(instance, reportHandler, BinaryNumOp.MUL)))
					.list("/", listCondition(c -> c.minSize(2), (instance, reportHandler) -> binary(instance, reportHandler, BinaryNumOp.DIV)))
					.list(new String[]{"^", "**"}, listCondition(c -> c.minSize(2), (instance, reportHandler) -> binary(instance, reportHandler, BinaryNumOp.POW)))
					.list("!", listCondition(c -> c.minSize(1), (instance, reportHandler) -> {
						Boolean bool = EVAL_BOOL.construct(instance.get(0), reportHandler);
						return bool==null ? null : Among.value(!bool);
					}))
					.all(IDENTITY)
	);

	private enum BinaryBoolOp{AND, OR, AND_SS, OR_SS}
	@Nullable private static Among binaryBool(AmongList list, @Nullable ReportHandler reportHandler, BinaryBoolOp op){
		Boolean a = EVAL_BOOL.construct(list.get(0), reportHandler);
		if(a==null) return null;
		switch(op){
			case AND_SS: if(!a) return Among.value(false); break;
			case OR_SS: if(a) return Among.value(true); break;
		}
		Boolean b = EVAL_BOOL.construct(list.get(1), reportHandler);
		if(b==null) return null;
		switch(op){
			case AND: case AND_SS: return Among.value(a&&b);
			case OR: case OR_SS: return Among.value(a||b);
		}
		return null;
	}

	@Nullable private static Among eq(AmongList list, @Nullable ReportHandler reportHandler, boolean expected){
		Among a = EVAL.construct(list.get(0), reportHandler);
		Among b = EVAL.construct(list.get(1), reportHandler);
		if(a==null||b==null) return null;
		if(a.equals(b)) return Among.value(expected);
		Boolean aBool = BOOL.construct(a, null);
		if(aBool!=null) return Among.value(aBool.equals(BOOL.construct(b, null))==expected);
		Double aNum = DOUBLE.construct(a, null);
		if(aNum!=null) return Among.value(aNum.equals(DOUBLE.construct(b, null))==expected);
		return Among.value(!expected);
	}

	private enum BinaryNumOp{ADD, SUB, MUL, DIV, POW, GT, LT, GTEQ, LTEQ}
	@Nullable private static Among binary(AmongList list, @Nullable ReportHandler reportHandler, BinaryNumOp op){
		Double a = EVAL_NUM.construct(list.get(0), reportHandler);
		Double b = EVAL_NUM.construct(list.get(1), reportHandler);
		if(a==null||b==null) return null;
		switch(op){
			case ADD: return Among.value(a+b);
			case SUB: return Among.value(a-b);
			case MUL: return Among.value(a*b);
			case DIV: return Among.value(a/b);
			case POW: return Among.value(Math.pow(a, b));
			case GT: return Among.value(a>b);
			case LT: return Among.value(a<b);
			case GTEQ: return Among.value(a>=b);
			case LTEQ: return Among.value(a<=b);
		}
		return null;
	}
}
