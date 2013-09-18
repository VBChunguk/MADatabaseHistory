package com.zotca.vbc.dbhistory.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map.Entry;

public class DatabaseDelta implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3200239727294074719L;
	private static int CURRENT_VERSION = 1;
	
	public enum DeltaType {
		MODIFIED,
		ADDED,
		DELETED,
	}
	
	private Hashtable<Integer, DeltaType> mDeltas;
	private Date mCreatedAt;
	
	public DatabaseDelta() {
		mDeltas = new Hashtable<Integer, DeltaType>();
		mCreatedAt = Calendar.getInstance().getTime();
	}
	public DatabaseDelta(Date createdAt) {
		mDeltas = new Hashtable<Integer, DeltaType>();
		mCreatedAt = createdAt;
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int version = in.readInt();
		switch (version)
		{
		case 1:
			readVersion1(in);
			break;
		default:
			throw new ClassNotFoundException("Unsupported version " + version);
		}
	}
	private void readVersion1(ObjectInputStream in) throws IOException, ClassNotFoundException {
		Object o = in.readObject();
		if (!(o instanceof Date))
			throw new ClassNotFoundException("Wrong v1 format or corrupted delta");
		mCreatedAt = (Date) o;
		
		int len = in.readInt();
		mDeltas = new Hashtable<Integer, DeltaType>(len);
		for (int i = 0; i < len; i++)
		{
			int id = in.readInt();
			byte rtype = in.readByte();
			DeltaType type = DeltaType.ADDED;
			switch (rtype)
			{
			case 0:
				type = DeltaType.MODIFIED;
				break;
			case 1:
				type = DeltaType.ADDED;
				break;
			case 2:
				type = DeltaType.DELETED;
				break;
			default:
				throw new ClassNotFoundException("Invalid DeltaType");
			}
			mDeltas.put(id, type);
		}
	}
	
	public void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(CURRENT_VERSION);
		out.writeObject(mCreatedAt);
		int len = mDeltas.size();
		out.writeInt(len);
		for (Entry<Integer, DeltaType> item : mDeltas.entrySet())
		{
			out.writeInt(item.getKey());
			switch (item.getValue())
			{
			case MODIFIED:
				out.writeByte(0);
				break;
			case ADDED:
				out.writeByte(1);
				break;
			case DELETED:
				out.writeByte(2);
				break;
			default:
				out.writeByte(0xff);
				break;
			}
		}
	}
	
	public Date getCreatedAt() {
		return mCreatedAt;
	}
	
	public void add(int id, DeltaType type) {
		mDeltas.put(id, type);
	}
	public void addModified(int id) {
		mDeltas.put(id, DeltaType.MODIFIED);
	}
	public void addNew(int id) {
		mDeltas.put(id, DeltaType.ADDED);
	}
	public void addDeleted(int id) {
		mDeltas.put(id, DeltaType.DELETED);
	}
}
