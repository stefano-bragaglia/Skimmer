 /**
 * 
 */
package bragaglia.skimmer.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.writer.SingleStreamCodeWriter;
import com.sun.tools.internal.xjc.api.S2JJAXBModel;
import com.sun.tools.internal.xjc.api.SchemaCompiler;
import com.sun.tools.internal.xjc.api.XJC;

/**
 * @author stefano
 *
 */
public class XJCTest {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		
		File file = new File("iFF708.xml");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		long time;
		System.err.print("Reading XML file... ");
		time = System.nanoTime();
		DocumentBuilder xmlBuilder = factory.newDocumentBuilder();
		Document xmlDocument = xmlBuilder.parse(file);
		Element xmlRoot = xmlDocument.getDocumentElement();
		URL xsdURL = new URL(xmlRoot.getAttribute("xmlns"));
		time = System.nanoTime() - time;
		System.err.println((time / 1_000_000) + "ms");

		int read;
		byte[] buffer = new byte[8192];
		InputStream source = xsdURL.openStream();
		ByteArrayOutputStream target = new ByteArrayOutputStream();
		while ((read = source.read(buffer)) > 0)
			target.write(buffer, 0, read);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(target.toByteArray(), 0, target.size());
		InputSource inputSource = new InputSource(inputStream);
		
        SchemaCompiler xsdCompiler = XJC.createSchemaCompiler();
        xsdCompiler.forcePackageName("com.xyz.schema.generated");
        xsdCompiler.parseSchema(inputSource);
        S2JJAXBModel model = xsdCompiler.bind();
        JCodeModel jCodeModel = model.generateCode(null, null);

        OutputStream outputStream = new ByteArrayOutputStream();
        jCodeModel.build(new SingleStreamCodeWriter(outputStream));

        String content = outputStream.toString();
        System.out.println(content);
		
	}
	
}
