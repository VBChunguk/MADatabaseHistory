package com.zotca.vbc.dbhistory.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Locale;

import android.os.Environment;

public class DatabaseFileManager {

	private LinkedList<Long> mDeltaChain;
	private Hashtable<Long, DatabaseDelta> mDeltaTable;
	
	public DatabaseFileManager(File f) {
		CardDatabase snapshot = makeSnapshot();
		File head = new File(f, "HEAD");
		long pHeadDelta;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(head));
			pHeadDelta = ois.readLong();
			ois.close();
		} catch (FileNotFoundException e) { // don't have HEAD, make new DB
			makeNewHead(f, snapshot);
			return;
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
			makeNewHead(f, snapshot);
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		File head_db = new File(f, String.format(Locale.getDefault(), "%ld", pHeadDelta));
		DatabaseDelta delta;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(head_db));
			ois.readLong();
			ois.readObject();
			CardDatabase db = (CardDatabase) ois.readObject();
			ois.close();
			delta = snapshot.makeDelta(db);
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
			makeNewHead(f, snapshot);
			return;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			makeNewHead(f, snapshot);
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}
		if (delta != null) // modified!
		{
			pHeadDelta = makeDelta(f, pHeadDelta, delta, snapshot);
		}
		// link all
		generateChain(f, pHeadDelta);
	}
	
	private void generateChain(File f, long pHead) {
		mDeltaChain = new LinkedList<Long>();
		mDeltaTable = new Hashtable<Long, DatabaseDelta>();
		while (pHead != 0)
		{
			mDeltaChain.add(pHead);
			File fDelta = new File(f,
					String.format(Locale.getDefault(), "%ld", pHead));
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fDelta));
				long oldHead = pHead;
				pHead = ois.readLong();
				Object o = ois.readObject();
				ois.close();
				mDeltaTable.put(oldHead, (DatabaseDelta) o);
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
				return;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			} catch (IOException e) {
				e.printStackTrace();
				return;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	private static void makeNewHead(File f, CardDatabase snapshot) {
		DatabaseDelta delta = snapshot.makeDelta(null);
		makeDelta(f, 0, delta, snapshot);
	}
	
	private static long makeDelta(File f,
			long pOldHead, DatabaseDelta delta, CardDatabase snapshot) {
		long deltaTimestamp = delta.getCreatedAt().getTime();
		try {
			File headDelta = new File(f,
					String.format(Locale.getDefault(), "%ld", deltaTimestamp));
			ObjectOutputStream delta_oos = new ObjectOutputStream(new FileOutputStream(headDelta));
			delta_oos.writeLong(pOldHead);
			delta_oos.writeObject(delta);
			delta_oos.writeObject(snapshot);
			delta_oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
		
		try {
			File head = new File(f, "HEAD");
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(head));
			oos.writeLong(deltaTimestamp);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
		return deltaTimestamp;
	}
	
	public static CardDatabase makeSnapshot() {
		File extstorage = Environment.getExternalStorageDirectory();
		File db = new File(extstorage,
				"Android/data/com.square_enix.million_kr/files/save/database/master_card");
		FileInputStream fis;
		try {
			fis = new FileInputStream(db);
		} catch (FileNotFoundException e) { // DB not found; MA not installed
			return null;
		}
		CardDatabase ret = null;
		try {
			ret = new CardDatabase(fis, new Date(db.lastModified())); // read DB and make snapshot
			fis.close();
		} catch (IOException e) { // why error?
			e.printStackTrace();
		}
		return ret;
	}
}
