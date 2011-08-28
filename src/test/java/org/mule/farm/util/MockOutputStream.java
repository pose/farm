package org.mule.farm.util;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.lang3.Validate;

public final class MockOutputStream extends OutputStream {
	private StringBuffer stringBuffer = new StringBuffer();
	private OutputStream outputStream;

	public static MockOutputStream createWireTap(OutputStream outputStream) {
		return new MockOutputStream(outputStream);
	}
	
	private MockOutputStream(OutputStream outputStream) {
		Validate.notNull(outputStream);
		this.outputStream = outputStream;
	}
	
	@Override
	public void write(int b) throws IOException {
		stringBuffer.append((char) b);
		outputStream.write(b);
	}

	public String getContent() {
		return stringBuffer.toString();
	}
}