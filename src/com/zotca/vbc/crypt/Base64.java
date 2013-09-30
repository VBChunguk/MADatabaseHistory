package com.zotca.vbc.crypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import android.util.Base64OutputStream;

public abstract class Base64 {

	public static String encodeFromStream(InputStream stream) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Base64OutputStream b64 =
				new Base64OutputStream(
						out,
						android.util.Base64.NO_CLOSE | android.util.Base64.NO_WRAP);
		byte[] buffer = new byte[64];
		while (true)
		{
			try {
				int read = stream.read(buffer);
				if (read < 0) break;
				b64.write(buffer, 0, read);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		try {
			stream.close();
			b64.close();
		} catch (IOException e) {
			return null;
		}
		byte[] resBytes = out.toByteArray();
		String ret = null;
		try {
			out.close();
			ret = new String(resBytes, "ASCII");
		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
		}
		return ret;
	}
}
