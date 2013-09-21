package com.zotca.vbc.crypt;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ArthurDecodeStream extends InputStream {

	private final InputStream mSource;
	private final SecretKeySpec mKey;
	private final Cipher mCipherAlgo;
	
	private byte[] mBuffer;
	private int mPosition;
	
	public ArthurDecodeStream(InputStream stream) throws NoSuchAlgorithmException
	{
		mSource = stream;
		mKey = new SecretKeySpec("A1dPUcrvur2CRQyl".getBytes(), "AES");
		try {
			mCipherAlgo = Cipher.getInstance("AES/ECB/PKCS5Padding");
			mCipherAlgo.init(2, mKey);
		} catch (Exception e) {
			throw new NoSuchAlgorithmException(e);
		}
		
		mBuffer = null;
		mPosition = 0;
	}
	
	@Override
	public void close() throws IOException {
		mSource.close();
	}
	
	@Override
	public int read() throws IOException {
		while (mBuffer == null || mPosition >= mBuffer.length) // reads more
		{
			byte[] buf = new byte[256];
			int read = mSource.read(buf);
			if (read < 0)
			{
				try {
					mBuffer = mCipherAlgo.doFinal();
					if (mBuffer == null || mPosition >= mBuffer.length) return -1;
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
			else
				mBuffer = mCipherAlgo.update(buf, 0, read);
			mPosition = 0;
		}
		return mBuffer[mPosition++];
	}

}
