package com.cqsd.spring.core.util;

/**
 * @author caseycheng
 * @date 2022/12/10-09:49
 **/
public abstract class StringUtil {
	//大写第一个单词的字母
	public static String toUpperCase(String name) {
		return checkUpperCase(name) ? name : modify(name, Character::toUpperCase);
	}
	
	//小写第一个单词的字母
	public static String toLowerCase(String name) {
		return checkLowerCase(name) ? name : modify(name, Character::toLowerCase);
	}
	
	//提供一个字符串，第二个参数定义如何处理字符串的第一位
	public static String modify(String name, SFunction<Character, Character> action) {
		final var character = action.apply(name.charAt(0));
		return character + name.substring(1);
	}
	
	//检查大写
	public static boolean checkUpperCase(String name) {
		return checkPattern(name, Character::isUpperCase);
	}
	
	//检查小写
	public static boolean checkLowerCase(String name) {
		return checkPattern(name, Character::isLowerCase);
	}
	
	//模式检查
	public static boolean checkPattern(String name, SFunction<Character, Boolean> action) {
		if (name == null || name.length() == 0) {
			return false;
		}
		return action.apply(name.charAt(0));
	}
	
	// 干掉字符串开始中的get或set
	public static String removeGetOrSet(String name) {
		if (name.startsWith("set") || name.startsWith("get")) {
			return name.substring(3);
		}
		return name;
	}
	
	//如果有值为true，无值为false
	public static boolean hasLength(String str) {
		return str != null && str.length() != 0;
	}
	
	//判断是否是${}开始的表达式
	public static boolean isInjectExpress(String express) {
		final var check = hasLength(express);
		//如果有值
		if (check) {
			final var chars = express.toCharArray();
			//TODO 这东西好麻烦
			
		} else {
			throw new NullPointerException("缺少表达式");
		}
		
		return false;
	}
	
	public static String subExpr(String str) {
		return str.substring(str.indexOf("${") + 2, str.indexOf("}"));
	}
	
	/**
	 * 这个方法检查是否有表达式外部的字符
	 *
	 * @param express 1${hello}2
	 * @return
	 */
	public static boolean isExtra(String express) {
		boolean ret = false;
		try {
			ret |= express.startsWith("${");
			ret &= express.endsWith("}");
			return ret;
		}catch (Exception e){
			return false;
		}
	}
	
	public static String getExtra(String express) {
		if (isExtra(express)) {
			final var expr = express.substring(express.indexOf("${"), express.indexOf("}"));
			return express.substring(expr.indexOf(expr));
		}
		return express;
	}
	
	@FunctionalInterface
	public interface SFunction<T, R> {
		R apply(T t);
	}
}
