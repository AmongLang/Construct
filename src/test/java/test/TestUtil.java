package test;

import among.AmongEngine;
import among.CompileResult;
import among.RootAndDefinition;
import among.Source;
import among.construct.Constructor;
import among.obj.Among;
import among.report.ReportList;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestUtil{
	public static final AmongEngine engine = new AmongEngine();

	public static RootAndDefinition make(String src){
		return make(Source.of(src));
	}
	public static RootAndDefinition make(Source src){
		CompileResult result = engine.read(src, null, null);
		result.printReports();
		result.expectSuccess();
		return result.rootAndDefinition();
	}

	public static void expectError(Source src, Constructor<Among, ?> constructor){
		RootAndDefinition rad = make(src);
		ReportList reports = new ReportList.Mutable();
		boolean error = false;
		for(Among a : rad.root())
			if(constructor.construct(a, reports)==null)
				error = true;
		reports.printReports(src);
		if(!error) throw new RuntimeException("Expected construction error");
	}

	public static <T> T[] construct(Source src, Constructor<Among, ? extends T> constructor, IntFunction<T[]> arrayProvider){
		RootAndDefinition rad = make(src);
		ReportList reports = new ReportList.Mutable();
		T[] arr = rad.root().values().stream()
				.map(a -> constructor.constructExpect(a, reports))
				.toArray(arrayProvider);
		reports.printReports(src);
		return arr;
	}

	public static Source expectSourceFrom(String folder, String fileName) throws IOException{
		String url = folder+"/"+fileName+".among";
		InputStream file = Thread.currentThread().getContextClassLoader().getResourceAsStream(url);
		assertNotNull(file, "File not found at '"+url+"'");
		return Source.read(new InputStreamReader(file, StandardCharsets.UTF_8));
	}
	@Nullable public static Source sourceFrom(String folder, String fileName) throws IOException{
		String url = folder+"/"+fileName+".among";
		InputStream file = Thread.currentThread().getContextClassLoader().getResourceAsStream(url);
		return file==null ? null : Source.read(new InputStreamReader(file, StandardCharsets.UTF_8));
	}
}
