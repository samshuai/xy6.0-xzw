package com.founder.xy.statistics.service.impl;

import com.founder.e5.context.E5Exception;
import com.founder.xy.statistics.dao.StatisticsDAO;
import com.founder.xy.statistics.interfaces.IStatisticsExport;
import com.founder.xy.statistics.util.TableUtil;
import com.founder.xy.statistics.util.TimeUtil;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ethan on 2017/2/14.
 */
@Service
public class WorkExportByDepartment implements IStatisticsExport {
    @Autowired
    StatisticsDAO statisticsDAO;
    @Override
    public Map<String, Object> export(Map<String, Object> inParam) throws E5Exception {
        String tenantCode = MapUtils.getString(inParam, "tenantCode");
        String webTableName = TableUtil.getArticleTableName(tenantCode, "channelWeb");
        String appTableName = TableUtil.getArticleTableName(tenantCode, "channelApp");
        String siteID = MapUtils.getString(inParam, "siteID").trim();
        String departmentID = MapUtils.getString(inParam, "departmentID");
        Timestamp beginTime;
        Timestamp endTime;
        try{
            beginTime = TimeUtil.StringToTimestamp(MapUtils.getString(inParam, "beginTime").trim());
            endTime = TimeUtil.StringToTimestamp(MapUtils.getString(inParam, "endTime").trim());
        } catch (Exception e){
            throw new E5Exception("Invalid Time Data!");
        }
        int pageSize = MapUtils.getIntValue(inParam, "pageSize", 40);
        int pageNum = MapUtils.getIntValue(inParam, "pageNum", 1);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("exportData",statisticsDAO.getPersonalTotalWorkStatisticsOfTimeByDepartmentID(siteID, departmentID, webTableName, appTableName, beginTime, endTime, pageSize, pageNum, "export"));
        return resultMap;
    }
}
