package com.cqsd.spring.util;

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
		final var c = name.charAt(0);
		final var character = action.apply(c);
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
	
	@FunctionalInterface
	public interface SFunction<T, R> {
		R apply(T t);
	}
}
