package among.construct;

import among.ReportHandler;
import among.obj.Among;
import among.obj.AmongPrimitive;
import org.jetbrains.annotations.Nullable;

import static among.construct.ConditionedConstructor.*;

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
	 * Among default {@code eval} library recreated using constructors. Evaluated object is converted back to {@link
	 * Among} object, as original library does.
	 */
	public static final ConstructRule<Among> EVAL = ConstructRule.make(builder ->
			builder.list("||", binaryCondition((a, b, r) -> binaryBool(a, b, r, BinaryBoolOp.OR_SS)))
					.list("&&", binaryCondition((a, b, r) -> binaryBool(a, b, r, BinaryBoolOp.AND_SS)))
					.list("|", binaryCondition((a, b, r) -> binaryBool(a, b, r, BinaryBoolOp.OR)))
					.list("&", binaryCondition((a, b, r) -> binaryBool(a, b, r, BinaryBoolOp.AND)))
					.list(new String[]{"==", "="}, binaryCondition((a, b, r) -> eq(a, b, r, true)))
					.list("!=", binaryCondition((a, b, r) -> eq(a, b, r, false)))
					.list(">", binaryCondition((a, b, r) -> binary(a, b, r, BinaryNumOp.GT)))
					.list("<", binaryCondition((a, b, r) -> binary(a, b, r, BinaryNumOp.LT)))
					.list(">=", binaryCondition((a, b, r) -> binary(a, b, r, BinaryNumOp.GTEQ)))
					.list("<=", binaryCondition((a, b, r) -> binary(a, b, r, BinaryNumOp.LTEQ)))
					.list("+", listConditions(b2 -> b2
							.addBinary((a, b, r) -> binary(a, b, r, BinaryNumOp.ADD))
							.addStrictUnary((a, r) -> {
								Double num = EVAL_NUM.construct(a, r);
								return num==null ? null : Among.value(num);
							})))
					.list("-", listConditions(b2 -> b2
							.addBinary((a, b, r) -> binary(a, b, r, BinaryNumOp.SUB))
							.addStrictUnary((a, r) -> {
								Double num = EVAL_NUM.construct(a, r);
								return num==null ? null : Among.value(-num);
							})))
					.list("*", binaryCondition((a, b, r) -> binary(a, b, r, BinaryNumOp.MUL)))
					.list("/", binaryCondition((a, b, r) -> binary(a, b, r, BinaryNumOp.DIV)))
					.list(new String[]{"^", "**"}, binaryCondition((a, b, r) -> binary(a, b, r, BinaryNumOp.POW)))
					.list("!", unaryCondition((a, r) -> {
						Boolean bool = EVAL_BOOL.construct(a, r);
						return bool==null ? null : Among.value(!bool);
					}))
					.all(IDENTITY)
	);

	private enum BinaryBoolOp{AND, OR, AND_SS, OR_SS}
	@Nullable private static Among binaryBool(Among a, Among b, @Nullable ReportHandler reportHandler, BinaryBoolOp op){
		Boolean b1 = EVAL_BOOL.construct(a, reportHandler);
		if(b1==null) return null;
		switch(op){
			case AND_SS: if(!b1) return Among.value(false); break;
			case OR_SS: if(b1) return Among.value(true); break;
		}
		Boolean b2 = EVAL_BOOL.construct(b, reportHandler);
		if(b2==null) return null;
		switch(op){
			case AND: case AND_SS: return Among.value(b1&&b2);
			case OR: case OR_SS: return Among.value(b1||b2);
		}
		return null;
	}

	@Nullable private static Among eq(Among a, Among b, @Nullable ReportHandler reportHandler, boolean expected){
		a = EVAL.construct(a, reportHandler);
		b = EVAL.construct(b, reportHandler);
		if(a==null||b==null) return null;
		if(a.equals(b)) return Among.value(expected);
		Boolean aBool = BOOL.construct(a, null);
		if(aBool!=null) return Among.value(aBool.equals(BOOL.construct(b, null))==expected);
		Double aNum = DOUBLE.construct(a, null);
		if(aNum!=null) return Among.value(aNum.equals(DOUBLE.construct(b, null))==expected);
		return Among.value(!expected);
	}

	private enum BinaryNumOp{ADD, SUB, MUL, DIV, POW, GT, LT, GTEQ, LTEQ}
	@Nullable private static Among binary(Among a, Among b, @Nullable ReportHandler reportHandler, BinaryNumOp op){
		Double n1 = EVAL_NUM.construct(a, reportHandler);
		Double n2 = EVAL_NUM.construct(b, reportHandler);
		if(n1==null||n2==null) return null;
		switch(op){
			case ADD: return Among.value(n1+n2);
			case SUB: return Among.value(n1-n2);
			case MUL: return Among.value(n1*n2);
			case DIV: return Among.value(n1/n2);
			case POW: return Among.value(Math.pow(n1, n2));
			case GT: return Among.value(n1>n2);
			case LT: return Among.value(n1<n2);
			case GTEQ: return Among.value(n1>=n2);
			case LTEQ: return Among.value(n1<=n2);
		}
		return null;
	}
}
