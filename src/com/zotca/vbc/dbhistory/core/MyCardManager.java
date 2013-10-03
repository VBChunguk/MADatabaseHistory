package com.zotca.vbc.dbhistory.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.zotca.vbc.dbhistory.core.MyCardManager.ServerCard.Attributes;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import android.util.SparseArray;

public class MyCardManager {

	private static final String DEBUG_TAG = "MyCardManager";
	
	private static MyCardManager instance;
	
	public static MyCardManager getInstance() {
		return instance;
	}
	
	public static class ParseException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7227125286313807947L;
	}
	
	public static class ServerCard implements Parcelable {

		public static enum Attributes {
			SERIAL_ID, MASTER_ID, HOLOGRAPHY,
			HP, ATK, CRITICAL,
			LEVEL, LEVEL_MAX, EXP, EXP_MAX, EXP_NEXT, EXP_LEFT, EXP_PER,
			PRICE_SALE, PRICE_MATERIAL, PRICE_EVOLUTION,
			LIMIT_BREAK_LEFT, LIMIT_BROKEN,
			UNKNOWN
		}
		public static Attributes getAttributeByName(String attributeName) {
			if ("serial_id".equals(attributeName)) return Attributes.SERIAL_ID;
			if ("master_id".equals(attributeName)) return Attributes.MASTER_ID;
			if ("holography".equals(attributeName)) return Attributes.HOLOGRAPHY;
			
			if ("hp".equals(attributeName)) return Attributes.HP;
			if ("power".equals(attributeName)) return Attributes.ATK;
			if ("critical".equals(attributeName)) return Attributes.CRITICAL;
			
			if ("lv".equals(attributeName)) return Attributes.LEVEL;
			if ("lv_max".equals(attributeName)) return Attributes.LEVEL_MAX;
			
			if ("exp".equals(attributeName)) return Attributes.EXP;
			if ("max_exp".equals(attributeName)) return Attributes.EXP_MAX;
			if ("next_exp".equals(attributeName)) return Attributes.EXP_NEXT;
			if ("exp_diff".equals(attributeName)) return Attributes.EXP_LEFT;
			if ("exp_per".equals(attributeName)) return Attributes.EXP_PER;
			
			if ("sale_price".equals(attributeName)) return Attributes.PRICE_SALE;
			if ("material_price".equals(attributeName)) return Attributes.PRICE_MATERIAL;
			if ("evolution_price".equals(attributeName)) return Attributes.PRICE_EVOLUTION;
			
			if ("plus_limit_count".equals(attributeName)) return Attributes.LIMIT_BREAK_LEFT;
			if ("limit_over".equals(attributeName)) return Attributes.LIMIT_BROKEN;
			
			return Attributes.UNKNOWN;
		}
		
		private EnumMap<Attributes, Object> attrData;
		
		public ServerCard() {
			attrData = new EnumMap<Attributes, Object>(Attributes.class);
		}
		public ServerCard(XmlPullParser parser)
				throws IOException, XmlPullParserException, ParseException {
			this();
			
			int eventType = parser.getEventType();
			if (eventType != XmlPullParser.START_TAG)
				throw new ParseException();
			int startDepth = parser.getDepth();
			Attributes attr = null;
			while (true)
			{
				eventType = parser.next();
				if (eventType == XmlPullParser.START_TAG)
				{
					if (attr != null)
						throw new ParseException();
					String name = parser.getName();
					attr = getAttributeByName(name);
					if (attr == Attributes.UNKNOWN)
						throw new ParseException();
				}
				else if (eventType == XmlPullParser.END_TAG)
				{
					if (startDepth == parser.getDepth()) break;
					attr = null;
				}
				else if (eventType == XmlPullParser.TEXT)
				{
					String text = parser.getText();
					Object res = null;
					switch (attr)
					{
					case HOLOGRAPHY:
					case LIMIT_BROKEN:
						res = text.equals("1");
						break;
					case SERIAL_ID:
						res = Long.parseLong(text);
						break;
					default:
						res = Integer.parseInt(text);
						break;
					}
					attrData.put(attr, res);
				}
			}
		}
		
		public int getIntAttribute(Attributes attr) {
			return ((Integer) attrData.get(attr)).intValue();
		}
		
		public long getLongAttribute(Attributes attr) {
			return ((Long) attrData.get(attr)).longValue();
		}
		
		public boolean getBooleanAttribute(Attributes attr) {
			return ((Boolean) attrData.get(attr)).booleanValue();
		}
		
		public Object getAttribute(Attributes attr) {
			return attrData.get(attr);
		}
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeSerializable(attrData);
		}
		
		public static final Parcelable.Creator<ServerCard> CREATOR =
				new Creator<MyCardManager.ServerCard>() {
			
					@Override
					public ServerCard[] newArray(int size) {
						return new ServerCard[size];
					}
					
					@Override
					public ServerCard createFromParcel(Parcel source) {
						ServerCard ret = new ServerCard();
						Object o = source.readSerializable();
						if (o instanceof EnumMap)
						{
							EnumMap<?, ?> map = (EnumMap<?, ?>) o;
							for (Entry<?, ?> item : map.entrySet())
							{
								if (!(item.getKey() instanceof Attributes))
									return null;
								ret.attrData.put((Attributes) item.getKey(), item.getValue());
							}
							return ret;
						}
						else return null;
					}
				};
	}
	
	private LongSparseArray<ServerCard> cards;
	private SparseArray<ServerCard> bestCard;
	
	public MyCardManager(byte[] data) {
		instance = null;
		try {
			cards = new LongSparseArray<ServerCard>();
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
			parser.setInput(dataStream, "UTF-8");
			
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT)
			{
				if (eventType == XmlPullParser.START_TAG)
				{
					if (parser.getName().equals("user_card"))
					{
						ServerCard card = new ServerCard(parser);
						long serialId = card.getLongAttribute(Attributes.SERIAL_ID);
						int masterId = card.getIntAttribute(Attributes.MASTER_ID);
						int level = card.getIntAttribute(Attributes.LEVEL);
						cards.put(serialId, card);
						
						ServerCard target = bestCard.get(masterId);
						int targetLevel = target.getIntAttribute(Attributes.LEVEL);
						if (targetLevel < level)
							bestCard.put(masterId, card);
						else if (targetLevel == level)
						{
							boolean holo = card.getBooleanAttribute(Attributes.HOLOGRAPHY);
							boolean targetHolo = target.getBooleanAttribute(Attributes.HOLOGRAPHY);
							if (!targetHolo && holo)
								bestCard.put(masterId, card);
						}
					}
				}
				eventType = parser.next();
			}
			dataStream.close();
			instance = this;
		} catch (XmlPullParserException e) {
			Log.wtf(DEBUG_TAG, "Could not create XmlPullParser");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			Log.e(DEBUG_TAG, "Wrong response");
			e.printStackTrace();
		}
	}
}
