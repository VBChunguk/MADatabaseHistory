package com.zotca.vbc.dbhistory.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public final class ReadUtility {

	public static String readMAString(ByteBuffer in) {
		int tmp = in.getInt();
		byte[] dst = new byte[tmp];
		in.get(dst);
		String ret = Charset.forName("UTF-8").decode(ByteBuffer.wrap(dst)).toString();
		return ret;
	}
	public static byte[] readStreamContent(InputStream stream)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		
		while (true)
		{
			try {
				int read = stream.read(buf);
				if (read == -1) break;
				bos.write(buf, 0, read);
			} catch (IOException e) {
				return null;
			}
		}
		return bos.toByteArray();
	}
	
}
