package among.construct;

import among.ReportHandler;
import among.construct.condition.ListConditionBuilder;
import among.obj.Among;
import among.obj.AmongList;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class ConditionedListConstructorBuilder<T> extends ConditionedConstructorBuilder<AmongList, ListConditionBuilder, T, ConditionedListConstructorBuilder<T>>{
	@Override protected ConditionedListConstructorBuilder<T> self(){
		return this;
	}
	@Override protected ListConditionBuilder createConditionBuilder(){
		return new ListConditionBuilder();
	}

	public ConditionedListConstructorBuilder<T> addUnary(Constructor<Among, T> constructor){
		return addUnary(false, null, constructor);
	}
	public ConditionedListConstructorBuilder<T> addUnary(Consumer<ListConditionBuilder> consumer, Constructor<Among, T> constructor){
		return addUnary(false, consumer, constructor);
	}

	public ConditionedListConstructorBuilder<T> addStrictUnary(Constructor<Among, T> constructor){
		return addUnary(true, null, constructor);
	}
	public ConditionedListConstructorBuilder<T> addStrictUnary(Consumer<ListConditionBuilder> consumer, Constructor<Among, T> constructor){
		return addUnary(true, consumer, constructor);
	}

	public ConditionedListConstructorBuilder<T> addBinary(BinaryConstructor<T> constructor){
		return addBinary(false, null, constructor);
	}
	public ConditionedListConstructorBuilder<T> addBinary(Consumer<ListConditionBuilder> consumer, BinaryConstructor<T> constructor){
		return addBinary(false, consumer, constructor);
	}

	public ConditionedListConstructorBuilder<T> addStrictBinary(BinaryConstructor<T> constructor){
		return addBinary(true, null, constructor);
	}
	public ConditionedListConstructorBuilder<T> addStrictBinary(Consumer<ListConditionBuilder> consumer, BinaryConstructor<T> constructor){
		return addBinary(true, consumer, constructor);
	}

	public ConditionedListConstructorBuilder<T> addTernary(TernaryConstructor<T> constructor){
		return addTernary(false, null, constructor);
	}
	public ConditionedListConstructorBuilder<T> addTernary(Consumer<ListConditionBuilder> consumer, TernaryConstructor<T> constructor){
		return addTernary(false, consumer, constructor);
	}

	public ConditionedListConstructorBuilder<T> addStrictTernary(TernaryConstructor<T> constructor){
		return addTernary(true, null, constructor);
	}
	public ConditionedListConstructorBuilder<T> addStrictTernary(Consumer<ListConditionBuilder> consumer, TernaryConstructor<T> constructor){
		return addTernary(true, consumer, constructor);
	}

	private ConditionedListConstructorBuilder<T> addUnary(
			boolean strict,
			@Nullable Consumer<ListConditionBuilder> consumer,
			Constructor<Among, T> constructor){
		ListConditionBuilder b = createConditionBuilder();
		if(strict) b.size(1);
		else b.minSize(1).warnIfBigger(1, i -> "Unary operations only need one element, "+i+" provided");
		if(consumer!=null) consumer.accept(b);
		return addConstructor(b, (l, r) -> constructor.construct(l.get(0), r));
	}

	private ConditionedListConstructorBuilder<T> addBinary(
			boolean strict,
			@Nullable Consumer<ListConditionBuilder> consumer,
			BinaryConstructor<T> constructor){
		ListConditionBuilder b = createConditionBuilder();
		if(strict) b.size(2);
		else b.minSize(2).warnIfBigger(2, i -> "Binary operations only need two elements, "+i+" provided");
		if(consumer!=null) consumer.accept(b);
		return addConstructor(b, (l, r) -> constructor.construct(l.get(0), l.get(1), r));
	}

	private ConditionedListConstructorBuilder<T> addTernary(
			boolean strict,
			@Nullable Consumer<ListConditionBuilder> consumer,
			TernaryConstructor<T> constructor){
		ListConditionBuilder b = createConditionBuilder();
		if(strict) b.size(3);
		else b.minSize(3).warnIfBigger(3, i -> "Ternary operations only need three elements, "+i+" provided");
		if(consumer!=null) consumer.accept(b);
		return addConstructor(b, (l, r) -> constructor.construct(l.get(0), l.get(1), l.get(2), r));
	}

	@FunctionalInterface
	public interface BinaryConstructor<OUT>{
		OUT construct(Among a, Among b, @Nullable ReportHandler reportHandler);
	}

	@FunctionalInterface
	public interface TernaryConstructor<OUT>{
		OUT construct(Among a, Among b, Among c, @Nullable ReportHandler reportHandler);
	}
}
