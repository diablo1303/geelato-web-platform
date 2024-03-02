package org.geelato.web.platform.m.base.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.util.Strings;
import org.geelato.core.gql.parser.FilterGroup;
import org.geelato.core.orm.Dao;
import org.geelato.web.platform.m.base.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author geemeta
 */
@ControllerAdvice
public class BaseController {
    @Autowired
    @Qualifier("primaryDao")
    protected Dao dao;

    @Autowired
    @Qualifier("secondaryDao")
    protected Dao dao2;
    @Autowired
    protected RuleService ruleService;
    /**
     * 创建session、Request、Response等对象
     */
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected HttpSession session;

    /**
     * 在每个子类方法调用之前先调用
     * 设置request,response,session这三个对象
     *
     * @param request
     * @param response
     */
    @ModelAttribute
    public void setReqAndRes(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.session = request.getSession(true);

        //可以在此处拿到当前登录的用户
    }

    public Map<String, Object> getQueryParameters(HttpServletRequest request) {
        Map<String, Object> queryParamsMap = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            List<String> values = List.of(entry.getValue());
            if (values.size() == 1) {
                queryParamsMap.put(entry.getKey(), values.get(0));
            } else {
                queryParamsMap.put(entry.getKey(), values.toArray(new String[values.size()]));
            }
        }

        return queryParamsMap;
    }

    public Map<String, Object> getQueryParameters(Class elementType, HttpServletRequest request) {
        Map<String, Object> queryParamsMap = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            Set<String> fieldNames = getClassFieldNames(elementType);
            if (fieldNames == null || fieldNames.contains(entry.getKey())) {
                List<String> values = List.of(entry.getValue());
                if (values.size() == 1) {
                    queryParamsMap.put(entry.getKey(), values.get(0));
                } else {
                    queryParamsMap.put(entry.getKey(), values.toArray(new String[values.size()]));
                }
            }
        }


        return queryParamsMap;
    }

    private Set<String> getClassFieldNames(Class elementType) {
        Set<String> fieldNameList = new HashSet<>();
        List<Field> fieldsList = getClassFields(elementType);
        for (Field field : fieldsList) {
            fieldNameList.add(field.getName());
        }
        return fieldNameList;
    }

    private List<Field> getClassFields(Class elementType) {
        List<Field> fieldsList = new ArrayList<>();
        while (elementType != null) {
            Field[] declaredFields = elementType.getDeclaredFields();
            fieldsList.addAll(Arrays.asList(declaredFields));
            elementType = elementType.getSuperclass();
        }

        return fieldsList;
    }

    /**
     * 构建查询条件
     *
     * @param params
     * @param operatorMap
     * @return
     * @throws ParseException
     */
    public FilterGroup getFilterGroup(Map<String, Object> params, Map<String, List<String>> operatorMap) throws ParseException {
        FilterGroup filterGroup = new FilterGroup();
        if (params != null && !params.isEmpty()) {
            if (operatorMap != null && !operatorMap.isEmpty()) {
                // 模糊查询
                List<String> contains = operatorMap.get("contains");
                if (contains != null && !contains.isEmpty()) {
                    for (String list : contains) {
                        if (Strings.isNotBlank((String) params.get(list))) {
                            filterGroup.addFilter(list, FilterGroup.Operator.contains, (String) params.get(list));
                            params.remove(list);
                        }
                    }
                }
                // 时间查询
                List<String> intervals = operatorMap.get("intervals");
                if (intervals != null && !intervals.isEmpty()) {
                    for (String list : intervals) {
                        String[] times = (String[]) params.get(list);
                        if (times != null && Strings.isNotBlank(times[1]) && Strings.isNotBlank(times[1])) {
                            filterGroup.addFilter(list, FilterGroup.Operator.gte, new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(new SimpleDateFormat("yyyy-MM-dd").parse(times[0])));
                            filterGroup.addFilter(list, FilterGroup.Operator.lte, new SimpleDateFormat("yyyy-MM-dd 23:59:59").format(new SimpleDateFormat("yyyy-MM-dd").parse(times[1])));
                            params.remove(list);
                        }
                    }
                }
            }
            // 对等查询
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() != null && Strings.isNotBlank(entry.getValue().toString())) {
                    filterGroup.addFilter(entry.getKey(), entry.getValue().toString());
                }
            }
        }

        return filterGroup;
    }
}
