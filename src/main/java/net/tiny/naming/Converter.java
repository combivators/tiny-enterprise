package net.tiny.naming;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public interface Converter {

	String LIST_REGEX = "[ ]*,[ ]*";
	String HEX_REGEX  = "^0[x, X][0-9A-Fa-f]{2}$";

	String format(Object value);
	Object parse(String value);

	abstract class AbstractConverter implements Converter {
		final DataType dataType;
		AbstractConverter(DataType type) {
			this.dataType = type;
		}
		@Override
		public String format(Object value) {
			if(null == value) {
				return null;
			} else {
				return value.toString();
			}
		}
	}
	Converter BYTE = new AbstractConverter(DataType.BYTE) {
		@Override
		public String format(Object value) {
			if(null != value && value instanceof Byte) {
				StringBuilder sb = new StringBuilder("0x");
				Byte data = (Byte)value;
				sb.append(Integer.toString(data.intValue(), 16));
				return sb.toString();
			} else {
				return null;
			}
		}

		@Override
		public Object parse(String value) {
			if(null == value)
				return null;
			return Byte.valueOf(value.substring(2), 16);
		}
	};

	Converter CHAR = new AbstractConverter(DataType.CHAR) {
		@Override
		public String format(Object value) {
			if(null != value && value instanceof Character) {
				StringBuilder sb = new StringBuilder("'");
				Character c = (Character)value;
				sb.append(c);
				sb.append("'");
				return sb.toString();
			} else {
				return null;
			}
		}

		@Override
		public Object parse(String value) {
			if(null == value)
				return null;
			if (value.length() == 1) {
				return new Character(value.charAt(0));
			} else {
				return value.substring(1,value.length()-1).charAt(0);
			}
		}
	};

	Converter STRING = new AbstractConverter(DataType.STRING) {
		@Override
		public Object parse(String value) {
			return value;
		}
	};
	Converter BOOLEAN = new AbstractConverter(DataType.BOOLEAN) {
		@Override
		public Object parse(String value) {
			if(value == null) {
				return false;
			}
			String val = value.toLowerCase();
			if("true".equals(val)
				|| "yes".equals(val)) {
				return true;
			} else {
				return false;
			}
		}
	};
	Converter INTEGER = new AbstractConverter(DataType.INTEGER) {
		@Override
		public Object parse(String value) {
			if(null == value)
				return null;
			return Integer.valueOf(value);
		}
	};
	Converter SHORT = new AbstractConverter(DataType.SHORT) {
		@Override
		public Object parse(String value) {
			if(null == value)
				return null;
			return Short.valueOf(value);
		}
	};
	Converter LONG = new AbstractConverter(DataType.LONG) {
		@Override
		public Object parse(String value) {
			if(null == value)
				return null;
			return Long.valueOf(value);
		}
	};
	Converter FLOAT = new AbstractConverter(DataType.FLOAT) {
		@Override
		public Object parse(String value) {
			if(null == value)
				return null;
			return Float.valueOf(value);
		}
	};
	Converter DOUBLE = new AbstractConverter(DataType.DOUBLE) {
		@Override
		public Object parse(String value) {
			if(null == value)
				return null;
			return Double.valueOf(value);
		}
	};
	Converter NUMBER = new AbstractConverter(DataType.NUMBER) {
		@Override
		public Object parse(String value) {
			if(null == value)
				return null;
			return Double.valueOf(value);
		}
	};

	Converter DATE = new AbstractConverter(DataType.DATE) {
		final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
		@Override
		public String format(Object value) {
			if(null != value && value instanceof java.util.Date) {
            	java.util.Date date = (java.util.Date)value;
	            synchronized(DATE_FORMAT) {
	                return DATE_FORMAT.format(date);
	            }
			} else {
				return null;
			}
		}
		@Override
		public Object parse(String value) {
			if(null == value)
				return null;
            try {
                synchronized(DATE_FORMAT) {
                    return DATE_FORMAT.parse(value);
                }
            } catch (ParseException e) {
            	return null;
            }
		}
	};

	Converter DATETIME = new AbstractConverter(DataType.DATETIME) {
		final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
		@Override
		public String format(Object value) {
			if(null != value && value instanceof java.sql.Date) {
            	java.sql.Date date = (java.sql.Date)value;
	            synchronized(DATETIME_FORMAT) {
	                return DATETIME_FORMAT.format(date);
	            }
			} else {
				return null;
			}
		}
		@Override
		public Object parse(String value) {
			if(null == value)
				return null;
            try {
	            synchronized(DATETIME_FORMAT) {
					java.util.Date date = DATETIME_FORMAT.parse(value);
					return new java.sql.Date(date.getTime());
	            }
			} catch (ParseException e) {
				return null;
			}
		}
	};

	Converter TIMESTAMP = new AbstractConverter(DataType.TIMESTAMP) {
		final SimpleDateFormat TS_FORMAT   = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		@Override
		public String format(Object value) {
			if(null != value && value instanceof Timestamp) {
				Timestamp date = (Timestamp)value;
	            synchronized(TS_FORMAT) {
	                return TS_FORMAT.format(date);
	            }
			} else {
				return null;
			}
		}
		@Override
		public Object parse(String value) {
			if(null == value)
				return null;
            try {
	            synchronized(TS_FORMAT) {
	            	java.util.Date date = TS_FORMAT.parse(value);
	            	return new Timestamp(date.getTime());
	            }
			} catch (ParseException e) {
				return null;
			}
		}
	};

	Converter CALENDAR = new AbstractConverter(DataType.CALENDAR) {
		final SimpleDateFormat RFC1123_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
		@Override
		public String format(Object value) {
			if(null != value && value instanceof java.util.Calendar) {
            	java.util.Calendar calendar = (java.util.Calendar)value;
	            synchronized(RFC1123_FORMAT) {
	                return RFC1123_FORMAT.format(calendar.getTime());
	            }
			} else {
				return null;
			}
		}
		@Override
		public Object parse(String value) {
			if(null == value)
				return null;
            try {
                synchronized(RFC1123_FORMAT) {
                	java.util.Calendar calendar = Calendar.getInstance();
                	calendar.setTime(RFC1123_FORMAT.parse(value));
                    return calendar;
                }
            } catch (ParseException e) {
            	return null;
            }
		}
	};

	Converter LIST = new AbstractConverter(DataType.LIST) {
		@Override
		public String format(Object value) {
			if(null != value && value instanceof List) {
				List<?> list = (List<?>)value;
				StringBuilder sb = new StringBuilder("[");
				for(Object obj : list) {
					if(sb.length() > 1) {
						sb.append(", ");
					}
					sb.append("'");
					sb.append(obj.toString());
					sb.append("'");
				}
				sb.append("]");
				return sb.toString();
			} else {
				return null;
			}
		}

		@Override
		public Object parse(String value) {
			if(null == value) {
				return null;
			}
			String[] values;
			if(value.startsWith("[") && value.endsWith("]")) {
				values = value.substring(1, value.length()-1).split(LIST_REGEX);
			} else {
				values = value.split(LIST_REGEX);
			}
			List<String> list = new ArrayList<String>();
			for(int n=0; n<values.length; n++) {
				values[n] = values[n].trim();
				if(values[n].length() > 2 && values[n].startsWith("'") && values[n].endsWith("'")) {
					values[n] = values[n].substring(1, values[n].length()-1);
				}
				list.add(values[n]);

			}
			return list;
		}
	};

	Converter PROPERTIES = new AbstractConverter(DataType.PROPERTIES) {
		@Override
		public String format(Object value) {
			if(null != value && value instanceof Properties) {
				Properties properties = (Properties)value;
				StringBuilder sb = new StringBuilder();
				Enumeration<?> names = properties.propertyNames();
				while(names.hasMoreElements()) {
					String key = names.nextElement().toString();
					if(sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(key);
					sb.append(":");
					sb.append("'");
					sb.append(properties.getProperty(key));
					sb.append("'");
				}
				return sb.toString();
			} else {
				return null;
			}
		}

		@Override
		public Object parse(String value) {
			if(null == value) {
				return null;
			}
			Properties properties = new Properties();
			String[] values = value.toString().split(LIST_REGEX);
			for(int n=0; n<values.length; n++) {
				values[n] = values[n].trim();
				int pos = values[n].indexOf(":");
				if(pos > 0) {
					String key = values[n].substring(0, pos);
					String data = values[n].substring(pos+1);
					if(data.length() > 2 && data.startsWith("'") && data.endsWith("'")) {
						data = data.substring(1, data.length()-1);
					}
					properties.setProperty(key, data);
				}
			}
			return properties;
		}
	};

	Converter MAP = new AbstractConverter(DataType.MAP) {
		@SuppressWarnings("unchecked")
		@Override
		public String format(Object value) {
			if(null != value && value instanceof Map) {
				Map<String,Object> map = (Map<String,Object>)value;
				StringBuilder sb = new StringBuilder();
				Set<String> names = map.keySet();
				sb.append("{");
				for(String key : names) {
					if(sb.length() > 1) {
						sb.append(", ");
					}
					sb.append("\"");
					sb.append(key);
					sb.append("\"");
					sb.append(":");
					sb.append("\"");
					sb.append(map.get(key));
					sb.append("\"");
				}
				sb.append("}");
				return sb.toString();
			} else {
				return null;
			}
		}

		@Override
		public Object parse(String value) {
			if(null == value) {
				return null;
			}
			Map<String,Object> map = new HashMap<String,Object>();
			String[] values;
			if(value.startsWith("{") && value.endsWith("}")) {
				values = value.toString().substring(1, value.length()-1).split(LIST_REGEX);
			} else {
				values = value.toString().split(LIST_REGEX);
			}
			for(int n=0; n<values.length; n++) {
				values[n] = values[n].trim();
				int pos = values[n].indexOf(":");
				if(pos > 0) {
					String key = values[n].substring(0, pos);
					if(key.length() > 2 && key.startsWith("\"") && key.endsWith("\"")) {
						key = key.substring(1, key.length()-1);
					}
					String data = values[n].substring(pos+1);
					if(data.length() > 2 && data.startsWith("\"") && data.endsWith("\"")) {
						data = data.substring(1, data.length()-1);
					}
					map.put(key, data);
				}
			}
			return map;
		}
	};

	abstract class ArrayConverter extends AbstractConverter {
		ArrayConverter(DataType type) {
			super(type);
		}

		boolean isTargetValue(String value) {
			if(null != value) {
				return (value.startsWith("[") && value.endsWith("]"));
			} else {
				return false;
			}
		}
	}

	Converter STRING_ARRAY = new ArrayConverter(DataType.STRING_ARRAY) {
		@Override
		public Object parse(String value) {
			if(null == value) {
				return null;
			}
			String[] values = new String[0];
			if(isTargetValue(value)) {
				values = value.substring(1, value.length()-1).split(LIST_REGEX);
				for(int i=0; i<values.length; i++) {
					if(values[i].length() > 2 && values[i].startsWith("'") && values[i].endsWith("'")) {
						values[i] = values[i].substring(1, values[i].length()-1);
					}
				}
			}
			return values;
		}
		@Override
		public String format(Object value) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			if(null != value && value instanceof String[]) {
				String[] values = (String[])value;
				for(String v : values) {
					if(sb.length() > 1) {
						sb.append(", ");
					}
					sb.append("'");
					sb.append(v);
					sb.append("'");
				}
			}
			sb.append("]");
			return sb.toString();
		}
	};

	Converter BOOLEAN_ARRAY = new ArrayConverter(DataType.BOOLEAN_ARRAY) {
		@Override
		public Object parse(String value) {
			if(null == value) {
				return null;
			}
			boolean[] values = new boolean[0];
			if(isTargetValue(value)) {
				String[] array = value.substring(1, value.length()-1).split(LIST_REGEX);
				values = new boolean[array.length];
				for(int i=0; i<array.length; i++) {
					values[i] = (boolean)BOOLEAN.parse(array[i]);
				}
			}
			return values;
		}

		@Override
		public String format(Object value) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			if(null != value && value instanceof boolean[]) {
				boolean[] values = (boolean[])value;
				for(boolean v : values) {
					if(sb.length() > 1) {
						sb.append(", ");
					}
					sb.append(v);
				}
			}
			sb.append("]");
			return sb.toString();
		}
	};
	Converter INTEGER_ARRAY = new ArrayConverter(DataType.INTEGER_ARRAY) {
		@Override
		public Object parse(String value) {
			if(null == value) {
				return null;
			}
			int[] values = new int[0];
			if(isTargetValue(value)) {
				String[] array = value.substring(1, value.length()-1).split(LIST_REGEX);
				values = new int[array.length];
				for(int i=0; i<array.length; i++) {
					values[i] = (int)INTEGER.parse(array[i]);
				}
			}
			return values;
		}
		@Override
		public String format(Object value) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			if(null != value && value instanceof int[]) {
				int[] values = (int[])value;
				for(int v : values) {
					if(sb.length() > 1) {
						sb.append(", ");
					}
					sb.append(v);
				}
			}
			sb.append("]");
			return sb.toString();
		}
	};
	Converter LONG_ARRAY = new ArrayConverter(DataType.LONG_ARRAY) {
		@Override
		public Object parse(String value) {
			if(null == value) {
				return null;
			}
			long[] values = new long[0];
			if(isTargetValue(value)) {
				String[] array = value.substring(1, value.length()-1).split(LIST_REGEX);
				values = new long[array.length];
				for(int i=0; i<array.length; i++) {
					values[i] = (long)LONG.parse(array[i]);
				}
			}
			return values;
		}
		@Override
		public String format(Object value) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			if(null != value && value instanceof long[]) {
				long[] values = (long[])value;
				for(long v : values) {
					if(sb.length() > 1) {
						sb.append(", ");
					}
					sb.append(v);
				}
			}
			sb.append("]");
			return sb.toString();
		}
	};
	Converter FLOAT_ARRAY = new ArrayConverter(DataType.FLOAT_ARRAY) {
		@Override
		public Object parse(String value) {
			if(null == value) {
				return null;
			}
			float[] values = new float[0];
			if(isTargetValue(value)) {
				String[] array = value.substring(1, value.length()-1).split(LIST_REGEX);
				values = new float[array.length];
				for(int i=0; i<array.length; i++) {
					values[i] = (float)FLOAT.parse(array[i]);
				}
			}
			return values;
		}
		@Override
		public String format(Object value) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			if(null != value && value instanceof float[]) {
				float[] values = (float[])value;
				for(float v : values) {
					if(sb.length() > 1) {
						sb.append(", ");
					}
					sb.append(v);
				}
			}
			sb.append("]");
			return sb.toString();
		}
	};
	Converter DOUBLE_ARRAY = new ArrayConverter(DataType.DOUBLE_ARRAY) {
		@Override
		public Object parse(String value) {
			if(null == value) {
				return null;
			}
			double[] values = new double[0];
			if(isTargetValue(value)) {
				String[] array = value.substring(1, value.length()-1).split(LIST_REGEX);
				values = new double[array.length];
				for(int i=0; i<array.length; i++) {
					values[i] = (double)DOUBLE.parse(array[i]);
				}
			}
			return values;
		}
		@Override
		public String format(Object value) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			if(null != value && value instanceof double[]) {
				double[] values = (double[])value;
				for(double v : values) {
					if(sb.length() > 1) {
						sb.append(", ");
					}
					sb.append(v);
				}
			}
			sb.append("]");
			return sb.toString();
		}
	};

	Converter BYTE_ARRAY = new ArrayConverter(DataType.BYTE_ARRAY) {
		@Override
		public String format(Object value) {
			return DatatypeConverter.printBase64Binary((byte[])value);
		}
		@Override
		public Object parse(String value) {
			if(null == value) {
				return null;
			}
			return DatatypeConverter.parseBase64Binary(value);
		}
	};

	Converter CHAR_ARRAY = new ArrayConverter(DataType.CHAR_ARRAY) {
		@Override
		public String format(Object value) {
			StringBuilder sb = new StringBuilder("\"");
			sb.append((char[])value);
			sb.append("\"");
			return sb.toString();
		}

		@Override
		public Object parse(String value) {
			if(null == value) {
				return null;
			}
			if(value.length() > 2 && value.startsWith("\"") && value.endsWith("\"")) {
				return value.substring(1, value.length()-1).toCharArray();
			} else {
				return value.toCharArray();
			}
		}
	};

	abstract class AbstractDataConverter implements Converter {
		final XmlAdapter<String, Object> adapter;
		AbstractDataConverter(XmlAdapter<String, Object> adapter) {
			this.adapter = adapter;
		}
		@Override
		public String format(Object value) {
    		try {
    			return adapter.marshal(value);
			} catch (Exception ex) {
				throw new IllegalArgumentException("Can not format object '" + value.toString() +"'", ex);
			}
		}

		@Override
		public Object parse(String value) {
			try {
				return adapter.unmarshal(value);
			} catch (Exception ex) {
				throw new IllegalArgumentException("Can not parse object '" + value +"'", ex);
			}
		}
	}
	Converter CDATA = new AbstractDataConverter(new CDATAAdapter()) {
	};
	Converter XML_DATA = new AbstractDataConverter(new XmlDataAdapter()) {
	};
	/*
	Converter BEAN = new Converter() {
		@Override
		public String format(Object value) {
			// TODO
			return null;
		}
		@Override
		public Object parse(String value) {
			// TODO
			return null;
		}
	};
	*/

}
