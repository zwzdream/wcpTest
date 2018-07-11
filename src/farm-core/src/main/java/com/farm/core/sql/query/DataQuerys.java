package com.farm.core.sql.query;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * 查询工具工具类
 * 
 * @author Administrator
 * 
 */
public class DataQuerys {
	/**
	 * 检查SQL注入风险
	 * 
	 * @param var
	 *            拼写SQL的值,仅仅用于对值的处理
	 */
	public static void wipeVirus(String var) {
		//防止sql注入的字符转义
		var = StringEscapeUtils.escapeSql(var);
		if (var.indexOf("(") + var.indexOf(")") + var.indexOf("||") + var.indexOf("||") > 0) {
			throw new RuntimeException("违反SQL注入风险约束！");
		}
	}
}
