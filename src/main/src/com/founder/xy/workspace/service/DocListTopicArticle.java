package com.founder.xy.workspace.service;

import com.founder.e5.commons.ResourceMgr;
import com.founder.e5.commons.StringUtils;
import com.founder.e5.context.Context;
import com.founder.e5.db.DBSession;
import com.founder.e5.db.IResultSet;
import com.founder.e5.dom.DocTypeField;
import com.founder.e5.workspace.service.DocListHelper;
import com.founder.xy.commons.InfoHelper;
import com.founder.xy.workspace.MainHelper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.ArrayUtils;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;


public class DocListTopicArticle extends DocListService{
	//固定查询字段
	protected static final String[] FIELDS_IN_RELTABLE = new String[]{
			"SYS_DOCUMENTID","a_type","a_location","SYS_TOPIC","SYS_AUTHORS","a_column","a_pubTime","a_countClick","a_countClick0",
			"a_countClick1","a_countClick2","a_countShare","a_countShare0","a_countShare1","a_countShare2",
			"a_countDiscuss","a_countDiscuss0","a_countDiscuss1","a_countDiscuss2","a_location","SYS_DOCLIBID"
	};
	//查关联表时固定的SQL语句的前半部分。select SYS_DOCUMENTID...
	protected static final String SQL_PREFIX_IN_RELTABLE;
	static {
		String sql = "select ";
		for (int i = 0; i < FIELDS_IN_RELTABLE.length; i++) {
			if (i > 0) sql += ",";
			if(FIELDS_IN_RELTABLE[i].equals("a_pubTime")){
                sql += "DATE_FORMAT(a_pubTime, '%Y-%m-%d %H:%i:%S') as a_pubTime";
            }else{
				sql += FIELDS_IN_RELTABLE[i];
			}
		}
		SQL_PREFIX_IN_RELTABLE = sql;
	}
	
	@Override
	public String getDocList() {
		throw new RuntimeException("TwoStepsDocList: NO getDocList() SUPPORT!");
	}

	/**
	 * 两段式列表查询：关联表时，或检索时
	 * @return
	 */
	public String getDocList(boolean activityRel) {
		JSONObject result = null;

		//1查询表符合条件数据数量
		//无查询结果时，直接返回
		int count = getListCount();
		if (count == 0) return EMPTY_XML;

		//2查看表数据
        JSONArray list = getListFromRelTable();

		String xml = assembleXML(count, list);
		return xml;
	}


	/**
	 *查询稿件表和app稿件表中符合话题条件的数据
	 */
	private JSONArray getListFromRelTable() {
	    String orderSql;
	    if(StringUtils.isBlank(getOrderBy())){
            orderSql = " sys_documentid asc,a_location asc ";
        }else{
            orderSql = getOrderBy();
        }
        String selectSql = SQL_PREFIX_IN_RELTABLE.replace("a_type","a.a_type");
        String whereSql = getWhere().replace("a_status","a.a_status").replace("a_siteID","a.a_siteID");
		String sql = selectSql.replace("a_location", "'Web稿件' as a_location") + " from xy_article a "
                + " left join xy_topicrelart b on a.sys_documentid = b.a_articleid"
                + " where b.a_channel=1 and " + whereSql
                + " union all "
                + selectSql.replace("a_location","'App稿件' as a_location") + " from xy_articleapp a "
                + " left join xy_topicrelart b on a.sys_documentid = b.a_articleid"
                + " where b.a_channel=2 and " + whereSql
                + " order by " + orderSql;
		JSONArray list = query(sql, param.getBegin(), param.getCount());
        return list;
	}


    private JSONArray query(String sql, int paramBegin, int paramCount) {
		JSONArray result = new JSONArray();

		DBSession db = null;
		IResultSet rs = null;
		try {
			db = Context.getDBSession(docLib.getDsID());
			sql = db.getDialect().getLimitString(sql, paramBegin, paramCount);
			rs = db.executeQuery(sql, null);
			while (rs.next()) {
				JSONObject json = new JSONObject();
				String trParam = "<DocLibID>1O2r3d4e5r6I7D</DocLibID><DocID>1I2D3</DocID>";
				for (String field : FIELDS_IN_RELTABLE) {
					if(!field.equals("SYS_DOCLIBID")){
						json.put(field, rs.getString(field));
					}
					if(field.equals("SYS_DOCUMENTID")){
						trParam = trParam.replace("1I2D3",rs.getString(field));
					}else if(field.equals("SYS_DOCLIBID")){
						trParam = trParam.replace("1O2r3d4e5r6I7D",rs.getString(field));
					}
				}
				json.put("trParam", trParam);
				json.put("DocOrder", String.valueOf(++paramBegin));

				result.add(json);
			}
		} catch (Exception e) {
			log.error("[TwoStepsDocList.getDocList(sql)]", e);
			log.error("[TwoStepsDocList.getDocList(sql)]--" + sql);
		} finally {
			ResourceMgr.closeQuietly(rs);
			ResourceMgr.closeQuietly(db);
		}
		return result;
	}


	private String assembleXML(int count, JSONArray list) {
		StringBuffer sbResult = new StringBuffer(1000);
		sbResult.append("<DocList><TotalSum>").append(count).append("</TotalSum>");
		try {
			//取出无权限的字段
			String[] fieldDisables = getFieldsDisabled();

			for (int i = 0; i < list.size(); i++) {
				JSONObject ele = (JSONObject)list.get(i);

				@SuppressWarnings("unchecked")
				Iterator<String> keys = (Iterator<String>)ele.keys();

				sbResult.append("<DocItem>");
                sbResult.append(ele.get("trParam"));
				while (keys.hasNext()) {
					String field = keys.next();
					sbResult.append("<").append(field).append(">");

					//只显示有权限的字段的值
					if (!ArrayUtils.contains(fieldDisables, field)) {
						sbResult.append(DocListHelper.parseXMLMark(ele.getString(field)));
					}
					sbResult.append("</").append(field).append(">");
				}
				sbResult.append("</DocItem>");
			}
			sbResult.append("</DocList>");
			return sbResult.toString();
		} catch (Exception e) {
			log.error("[TwoStepsDocList.getXML]", e);
			return EMPTY_XML;
		}
	}

    /**
     *查询稿件表和app稿件表中符合话题条件的数据数量
     */
    private int getListCount() {
        String sql1 = "select count(1) from ( ";
        String whereSql = getWhere().replace("a_status","a.a_status").replace("a_siteID","a.a_siteID");
        String sql = "select sys_documentid from xy_article a "
                + " left join xy_topicrelart b on a.sys_documentid = b.a_articleid"
                + " where b.a_topicID is not null and b.a_channel=1 and " + whereSql
                + " union all "
                + "select sys_documentid from xy_articleapp a "
                + " left join xy_topicrelart b on a.sys_documentid = b.a_articleid"
                + " where b.a_topicID is not null and b.a_channel=2 and " + whereSql;
        String sql2 = " ) f";
        int count = queryCount(sql1+sql+sql2);

        return count;
    }

    private int queryCount(String countSql) {
        int count = 0;
        DBSession db = null;
        IResultSet rs = null;
        try {
            db = Context.getDBSession(docLib.getDsID());
            rs = db.executeQuery(countSql, null);
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            log.error("[TwoStepsDocList.getDocList(sql)]", e);
            log.error("[TwoStepsDocList.getDocList(sql)]--" + countSql);
        } finally {
            ResourceMgr.closeQuietly(rs);
            ResourceMgr.closeQuietly(db);
        }

        return count;
    }
}
