package com.zotca.vbc.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ArthurDecodeStream extends InputStream {

	private final InputStream mSource;
	
	public ArthurDecodeStream(InputStream stream) throws NoSuchAlgorithmException, IOException
	{
		final SecretKeySpec mKey;
		final Cipher mCipherAlgo;
		mKey = new SecretKeySpec("A1dPUcrvur2CRQyl".getBytes(), "AES");
		try {
			mCipherAlgo = Cipher.getInstance("AES/ECB/PKCS5Padding");
			mCipherAlgo.init(2, mKey);
		} catch (Exception e) {
			throw new NoSuchAlgorithmException(e);
		}
		ByteArrayOutputStream bufstream = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		while (true)
		{
			int read = stream.read(buf);
			if (read < 0) break;
			bufstream.write(buf, 0, read);
		}
		stream.close();
		byte[] alread = bufstream.toByteArray();
		try {
			mSource = new ByteArrayInputStream(mCipherAlgo.doFinal(alread));
		} catch (Exception e) {
			throw new IOException();
		}
	}
	
	@Override
	public void close() throws IOException {
		mSource.close();
	}
	
	@Override
	public int read() throws IOException {
		return mSource.read();
	}

}
