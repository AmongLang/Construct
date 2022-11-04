package test;

import among.CompileResult;
import among.RootAndDefinition;
import among.Source;
import among.construct.Constructors;
import among.obj.Among;
import among.report.ReportHandler;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static among.construct.Constructors.INT;

public class SampleCode{
	ReportHandler reportHandler = ReportHandler.simple();

	@Test public void sample(){
		Among rightValue = Among.value("123");
		Among wrongValue = Among.value("abcd");

		// before
		if(rightValue.isPrimitive()){
			try{
				System.out.println(rightValue.asPrimitive().getIntValue());
			}catch(NumberFormatException ex){
				reportHandler.reportError("Expected int", rightValue.sourcePosition());
			}
		}else{
			reportHandler.reportError("Invalid int", rightValue.sourcePosition());
		}
		// after
		@Nullable Integer i = INT.construct(rightValue, reportHandler);
		if(i!=null) System.out.println(i);


		if(wrongValue.isPrimitive()){
			try{
				System.out.println(wrongValue.asPrimitive().getIntValue());
			}catch(NumberFormatException ex){
				reportHandler.reportError("Expected int", wrongValue.sourcePosition());
			}
		}else{
			reportHandler.reportError("Invalid int", wrongValue.sourcePosition());
		}

		i = INT.construct(wrongValue, reportHandler);
		if(i!=null) System.out.println(i);


		final Source src = Source.of("use default_operators,((1+2+3+4)^12)");
		CompileResult result = TestUtil.engine.read(src);
		result.expectSuccess();
		Among among = result.root().single();
		System.out.println(among);
		Among evaluated = Constructors.EVAL.construct(among, ReportHandler.simple(src));
		System.out.println(evaluated);
	}


	@Test public void sampleData() throws IOException{
		Source src = TestUtil.expectSourceFrom("test", "sampleData");
		RootAndDefinition rad = TestUtil.make(src);
		ReportHandler reportHandler = ReportHandler.simple(src);
		for(Among among : rad.root().values()){
			SampleData data = SampleData.RULE.construct(among, reportHandler);
			if(data!=null) System.out.println(data);
		}
	}
}
