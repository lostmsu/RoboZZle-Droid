package com.team242.robozzle.telemetry;

import flexjson.ObjectBinder;
import flexjson.ObjectFactory;
import flexjson.transformer.AbstractTransformer;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by lost on 10/25/2015.
 */
public class Iso8601DateTransformer extends AbstractTransformer
		implements ObjectFactory {

	static final SimpleDateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	static {
		TimeZone utc = TimeZone.getTimeZone("UTC");
		iso8601.setTimeZone(utc);
	}

	@Override
	public void transform(Object object) {
		Date date = (Date)object;
		if (date == null) {
			this.getContext().write("null");
			return;
		}

		String formattedValue = iso8601.format(date);
		this.getContext().writeQuoted(formattedValue);
	}

	@Override
	public Object instantiate(ObjectBinder context, Object value, Type targetType, Class targetClass) {
		if (value == null)
			return null;

		try {
			return iso8601.parse(value.toString());
		} catch (ParseException e){
			throw new RuntimeException(e);
		}
	}
}
