package test.data;

import among.TypeFlags;
import among.construct.Constructor;
import among.construct.Constructors;
import among.obj.Among;
import among.obj.AmongList;

import java.util.Arrays;

import static among.construct.ConditionedConstructor.listCondition;
import static among.construct.Constructor.generifyList;

public final class Matrix33{
	private final double[][] matrix = new double[3][3];

	public Matrix33(){
		matrix[0][2] = 1;
		matrix[1][1] = 1;
		matrix[2][0] = 1;
	}
	public Matrix33(
			double x1y1, double x2y1, double x3y1,
			double x1y2, double x2y2, double x3y2,
			double x1y3, double x2y3, double x3y3
	){
		matrix[0][0] = x1y1;
		matrix[1][0] = x1y2;
		matrix[2][0] = x1y3;
		matrix[0][1] = x2y1;
		matrix[1][1] = x2y2;
		matrix[2][1] = x2y3;
		matrix[0][2] = x3y1;
		matrix[1][2] = x3y2;
		matrix[2][2] = x3y3;
	}
	public Matrix33(double[][] ints){
		for(int i = 0; i<3; i++) System.arraycopy(ints[i], 0, matrix[i], 0, 3);
	}

	public void set(int x, int y, double value){
		matrix[y][x] = value;
	}
	public double get(int x, int y){
		return matrix[y][x];
	}

	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		Matrix33 matrix33 = (Matrix33)o;
		return Arrays.deepEquals(matrix, matrix33.matrix);
	}
	@Override public int hashCode(){
		return Arrays.deepHashCode(matrix);
	}

	@Override public String toString(){
		return Arrays.deepToString(matrix);
	}

	private static final Constructor<AmongList, double[]> row = listCondition(c -> c
					.minSize(3).elementType(TypeFlags.PRIMITIVE).warnIfWrongSize(3),
			(l, r) -> {
				double[] doubles = new double[3];
				for(int i = 0; i<3; i++){
					Double d = Constructors.DOUBLE.construct(l.get(i), r);
					if(d==null) return null;
					doubles[i] = d;
				}
				return doubles;
			});
	public static final Constructor<Among, Matrix33> CONSTRUCTOR = generifyList(listCondition(c -> c
					.minSize(3).elementType(TypeFlags.UNNAMED_LIST).warnIfWrongSize(3),
			(l, r) -> {
				if(l.hasName()){
					if(r!=null) r.reportError("Expected unnamed list");
					return null;
				}
				double[][] doubles = new double[3][];
				for(int i = 0; i<3; i++){
					double[] d = row.construct(l.get(i).asList(), r);
					if(d==null) return null;
					doubles[i] = d;
				}
				return new Matrix33(doubles);
			}));
}
