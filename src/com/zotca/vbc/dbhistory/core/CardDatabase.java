package com.zotca.vbc.dbhistory.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

public class CardDatabase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4752790839312737924L;
	private static int CURRENT_VERSION = 1;
	
	public static class Card implements Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 8952683716453934002L;

		private static int CURRENT_VERSION = 1;
		
		public static final int BLADE = 1;
		public static final int TECHNIQUE = 2;
		public static final int MAGIC = 3;
		public static final int FAIRY = 4;
		
		private static final String[] RARE_LEVEL_STR;
		private static final String[] RARE_LEVEL_SIMPLE_STR;
		private static final String[] RARE_LEVEL_KOREAN_STR;
		
		public static String getRareLevelString(int rareLevel)
		{
			return RARE_LEVEL_STR[rareLevel-1];
		}
		public static String getRareLevelString(int rareLevel, boolean isSimple)
		{
			if (isSimple)
				return RARE_LEVEL_SIMPLE_STR[rareLevel-1];
			else
				return RARE_LEVEL_STR[rareLevel-1];
		}
		public static int getRareLevelFromString(String rareLevel)
		{
			for (int i = 0; i < RARE_LEVEL_STR.length; i++)
			{
				String item = RARE_LEVEL_STR[i];
				String proc = item.replace('+', 'P');
				if (rareLevel.equalsIgnoreCase(item) || rareLevel.equalsIgnoreCase(proc))
					return i+1;
				
				item = RARE_LEVEL_SIMPLE_STR[i];
				proc = item.replace('+', 'P');
				if (rareLevel.equalsIgnoreCase(item) || rareLevel.equalsIgnoreCase(proc))
					return i+1;
					
				item = RARE_LEVEL_KOREAN_STR[i];
				if (rareLevel.equalsIgnoreCase(item))
					return i+1;
			}
			return -1;
		}
		static
		{
			RARE_LEVEL_STR = new String[] { "NORMAL", "NORMAL+", "RARE", "RARE+",
					"SUPER RARE", "SUPER RARE+", "MILLION RARE", "MILLION RARE+" };
			RARE_LEVEL_SIMPLE_STR = new String[] { "N", "N+", "R", "R+",
					"SR", "SR+", "MR", "MR+" };
			RARE_LEVEL_KOREAN_STR = new String[] { "노멀", "노플", "레어", "레플",
					"슈레", "슈레플", "밀레", "밀레플" };
		}
		
		private String name;
		private int id;
		private int category;
		private int cost;
		private int rareLevel;
		private int normalIllust;
		private int arousalIllust;
		private boolean isFemale;
		
		private String description;
		private String illustrator;
		
		private String mainSkillName;
		private String subSkillName;
		private String skillDescription;
		
		public Card(ByteBuffer in)
		{
			id = in.getInt();
			category = in.getInt();
			name = ReadUtility.readMAString(in);
			description = ReadUtility.readMAString(in);
			subSkillName = ReadUtility.readMAString(in);
			mainSkillName = ReadUtility.readMAString(in);
			skillDescription = ReadUtility.readMAString(in);
			illustrator = ReadUtility.readMAString(in);
			cost = in.getInt();
			rareLevel = in.getInt();
			for (int i = 0; i < 8; i++) in.getInt();
			normalIllust = in.getInt();
			arousalIllust = in.getInt();
			for (int i = 0; i < 4; i++) in.getInt();
			for (int i = 0; i < 2; i++) ReadUtility.readMAString(in);
			for (int i = 0; i < 2; i++) in.getInt();
			isFemale = in.getInt() == 2;
		}
		
		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			int version = in.readInt();
			switch (version)
			{
			case 1:
				id = in.readInt();
				category = in.readInt();
				cost = in.readInt();
				rareLevel = in.readInt();
				isFemale = in.readBoolean();
				name = (String) in.readObject();
				description = (String) in.readObject();
				mainSkillName = (String) in.readObject();
				subSkillName = (String) in.readObject();
				skillDescription = (String) in.readObject();
				illustrator = (String) in.readObject();
				normalIllust = in.readInt();
				arousalIllust = in.readInt();
				break;
			default:
				throw new ClassNotFoundException("Wrong format");
			}
		}
		private void writeObject(ObjectOutputStream out) throws IOException {
			out.writeInt(CURRENT_VERSION); // version 1
			
			out.writeInt(id);
			out.writeInt(category);
			out.writeInt(cost);
			out.writeInt(rareLevel);
			out.writeBoolean(isFemale);
			out.writeObject(name);
			out.writeObject(description);
			out.writeObject(mainSkillName);
			out.writeObject(subSkillName);
			out.writeObject(skillDescription);
			out.writeObject(illustrator);
			out.writeInt(normalIllust);
			out.writeInt(arousalIllust);
		}
		
		public String getName()
		{
			return name;
		}
		public int getId()
		{
			return id;
		}
		public int getCategory()
		{
			return category;
		}
		public int getCost()
		{
			return cost;
		}
		public int getRareLevel()
		{
			return rareLevel;
		}
		public int getNormalIllustId()
		{
			return normalIllust;
		}
		public int getArousalIllustId()
		{
			return arousalIllust;
		}
		public boolean isFemale()
		{
			return isFemale;
		}
		
		public String getDescription()
		{
			return description;
		}
		public String getIllustrator()
		{
			return illustrator;
		}
		
		public String getSkillName()
		{
			return mainSkillName;
		}
		public String getSubSkillName()
		{
			return subSkillName;
		}
		public String getSkillDescription()
		{
			return skillDescription;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Card)) return false;
			Card c = (Card) o;
			
			boolean ret = true;
			ret &= (id == c.id);
			ret &= (category == c.category);
			ret &= (cost == c.cost);
			ret &= (rareLevel == c.rareLevel);
			ret &= (isFemale == c.isFemale);
			ret &= (name.equals(c.name));
			ret &= (description.equals(c.description));
			ret &= (mainSkillName.equals(c.mainSkillName));
			ret &= (subSkillName.equals(c.subSkillName));
			ret &= (skillDescription.equals(c.skillDescription));
			ret &= (illustrator.equals(c.illustrator));
			ret &= (normalIllust == c.normalIllust);
			ret &= (arousalIllust == c.arousalIllust);
			return ret;
		}
		
		@Override
		public String toString() {
			return String.format(Locale.getDefault(), "[#%d] %s", id, name);
		}
	}
	
	private int mCardCount;
	private Date mCreatedAt;
	private Card[] mCards;
	private Hashtable<Integer, Card> mIdCardDict;
	
	public CardDatabase(InputStream in, Date createdAt)
	{
		mCreatedAt = createdAt;
		ByteBuffer buf = ByteBuffer.wrap(ReadUtility.readStreamContent(in));
		int len = mCardCount = buf.getInt();
		int[] offsets = new int[len];
		mCards = new Card[len];
		mIdCardDict = new Hashtable<Integer, Card>(len);
		for (int i = 0; i < len; i++)
		{
			offsets[i] = buf.getInt();
		}
		for (int i = 0; i < len; i++)
		{
			int offset = offsets[i];
			buf.position(offset);
			Card card = new Card(buf);
			mCards[i] = card;
			mIdCardDict.put(card.getId(), card);
		}
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(CURRENT_VERSION); // version 1
		out.writeObject(mCreatedAt);
		out.writeInt(mCardCount);
		for (Card card : mCards)
		{
			out.writeObject(card);
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int version = in.readInt();
		switch (version)
		{
		case 1:
			readVersion1(in);
			break;
		default:
			throw new ClassNotFoundException();
		}
	}
	private void readVersion1(ObjectInputStream in) throws IOException, ClassNotFoundException {
		Object o = in.readObject();
		if (!(o instanceof Date))
			throw new ClassNotFoundException();
		mCreatedAt = (Date) o;
		
		int len = mCardCount = in.readInt();
		mCards = new Card[len];
		mIdCardDict = new Hashtable<Integer, Card>(len);
		for (int i = 0; i < len; i++)
		{
			Object temp = in.readObject();
			if (!(temp instanceof Card))
				throw new ClassNotFoundException("Invalid format or corrupted database");
			Card card = (Card) temp;
			mCards[i] = card;
			mIdCardDict.put(card.getId(), card);
		}
	}
	
	public DatabaseDelta makeDelta(CardDatabase previous) {
		DatabaseDelta ret = new DatabaseDelta(mCreatedAt);
		Hashtable<Integer, Card> cdb = this.mIdCardDict;
		Set<Entry<Integer, Card>> kcdb = cdb.entrySet();
		if (previous == null) // initial
		{
			for (Entry<Integer, Card> item : kcdb)
				ret.addFirst(item.getValue());
			return ret;
		}
		Hashtable<Integer, Card> pdb = previous.mIdCardDict;
		Set<Entry<Integer, Card>> kpdb = pdb.entrySet();
		boolean hasDelta = false;
		// deleted first
		for (Entry<Integer, Card> item : kpdb)
		{
			if (!cdb.containsKey(item.getKey())) // deleted
			{
				ret.addDeleted(item.getValue());
				hasDelta = true;
			}
		}
		// then, modified / added
		for (Entry<Integer, Card> item : kcdb)
		{
			if (!pdb.containsKey(item.getKey())) // added
			{
				ret.addNew(item.getValue());
				hasDelta = true;
			}
			else
			{
				Card cc = item.getValue();
				Card pc = pdb.get(item.getKey());
				if (!cc.equals(pc)) // modified
				{
					ret.addModified(cc);
					hasDelta = true;
				}
			}
		}
		if (!hasDelta) return null;
		return ret;
	}
}
