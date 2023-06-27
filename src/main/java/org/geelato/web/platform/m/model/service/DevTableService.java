package org.geelato.web.platform.m.model.service;

import org.apache.logging.log4j.util.Strings;
import org.geelato.core.constants.ColumnDefault;
import org.geelato.core.constants.MetaDaoSql;
import org.geelato.core.enums.DeleteStatusEnum;
import org.geelato.core.enums.EnableStatusEnum;
import org.geelato.core.enums.LinkedEnum;
import org.geelato.core.enums.TableTypeEnum;
import org.geelato.core.meta.model.entity.TableMeta;
import org.geelato.core.meta.schema.SchemaTable;
import org.geelato.core.util.SchemaUtils;
import org.geelato.web.platform.m.base.service.BaseSortableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author diabl
 */
@Component
public class DevTableService extends BaseSortableService {
    private static final String DELETE_COMMENT_PREFIX = "已删除；";
    private static final String UPDATE_COMMENT_PREFIX = "已变更；";
    private static final Logger logger = LoggerFactory.getLogger(DevTableService.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Lazy
    @Autowired
    private DevTableColumnService devTableColumnService;
    @Lazy
    @Autowired
    private DevTableForeignService devTableForeignService;

    /**
     * 全量查询
     *
     * @param entity 查询实体
     * @param params 条件参数
     * @param <T>
     * @return
     */
    @Override
    public <T> List<T> queryModel(Class<T> entity, Map<String, Object> params) {
        dao.SetDefaultFilter(true, filterGroup);
        return dao.queryList(entity, params, "entity_name ASC");
    }

    /**
     * 从数据库同步至模型
     *
     * @param tableMeta
     * @throws ParseException
     */
    public void resetTableByDataBase(TableMeta tableMeta) throws ParseException, InvocationTargetException, IllegalAccessException {
        // database_table
        List<Map<String, Object>> tableList = queryInformationSchemaTables(tableMeta.getEntityName());
        List<SchemaTable> schemaTables = SchemaUtils.buildData(SchemaTable.class, tableList);
        // handle
        if (schemaTables != null && schemaTables.size() > 0) {
            SchemaTable schemaTable = schemaTables.get(0);
            tableMeta.setTitle(schemaTable.getTableComment());
            tableMeta.setTableComment(schemaTable.getTableComment());
            tableMeta.setTableName(schemaTable.getTableName());
            tableMeta.setUpdateAt(sdf.parse(schemaTable.getCreateTime()));
            updateModel(tableMeta);
            devTableColumnService.resetTableColumnByDataBase(tableMeta, false);
            devTableForeignService.resetTableForeignByDataBase(tableMeta, false);
        } else {
            tableMeta.setEnableStatus(EnableStatusEnum.DISABLED.getCode());
            isDeleteModel(tableMeta);
            devTableColumnService.resetTableColumnByDataBase(tableMeta, true);
            devTableForeignService.resetTableForeignByDataBase(tableMeta, false);
        }
    }

    /**
     * 表名变更，需要同步数据库的方法
     *
     * @param form  变更数据
     * @param model 源数据
     * @return
     */
    public TableMeta handleForm(TableMeta form, TableMeta model) {
        // 表名是否修改
        if (model.getEntityName().equals(form.getEntityName())) {
            return form;
        }
        // 数据库表方可
        if (!TableTypeEnum.TABLE.getCode().equals(model.getTableType())) {
            form.setTableName(null);
            return form;
        }
        // 数据库是否存在表
        Map<String, Object> sqlParams = new HashMap<>();
        List<Map<String, Object>> tableList = queryInformationSchemaTables(model.getEntityName());
        if (tableList == null || tableList.isEmpty()) {
            form.setTableName(null);
            sqlParams.put("isTable", false);
        } else {
            sqlParams.put("isTable", true);
            form.setTableName(form.getEntityName());
        }
        // 修正当前表
        //form.setDescription(String.format("update %s from %s[%s]。\n", sdf.format(new Date()), model.getTitle(), model.getEntityName()) + form.getDescription());
        // 备份原来的表
        model.setId(null);
        model.setLinked(LinkedEnum.NO.getValue());
        model.setTableName(null);
        // update 2023-06-25 13:14:15 用户[user]=>组织[org]。
        model.setDescription(String.format("update %s %s[%s]=>%s[%s]。\n", sdf.format(new Date()), model.getTitle(), model.getEntityName(), form.getTitle(), form.getEntityName()) + model.getDescription());
        model.setTableComment(UPDATE_COMMENT_PREFIX + model.getTableComment());
        model.setTitle(UPDATE_COMMENT_PREFIX + model.getTitle());
        model.setEnableStatus(EnableStatusEnum.DISABLED.getCode());
        model.setDelStatus(DeleteStatusEnum.IS.getCode());
        model.setSeqNo(ColumnDefault.SEQ_NO_DELETE);
        //dao.save(model);
        // 数据库表修正
        sqlParams.put("newEntityName", form.getEntityName());// 新
        sqlParams.put("entityName", model.getEntityName());// 旧
        sqlParams.put("newComment", form.getTitle());
        sqlParams.put("delStatus", DeleteStatusEnum.NO.getCode());
        sqlParams.put("enableStatus", EnableStatusEnum.ENABLED.getCode());
        sqlParams.put("remark", "");
        dao.execute("metaResetOrDeleteTable", sqlParams);

        return form;
    }

    /**
     * 逻辑删除
     *
     * @param model
     */
    public void isDeleteModel(TableMeta model) {
        // 格式：table_name_20230625141315
        String newTableName = String.format("%s_d%s", model.getEntityName(), System.currentTimeMillis());
        String newTitle = DELETE_COMMENT_PREFIX + model.getTitle();
        String newComment = DELETE_COMMENT_PREFIX + (Strings.isNotBlank(model.getTableComment()) ? model.getTableComment() : model.getTitle());
        // delete 2023-06-25 13:14:15 用户[user]=>[user_2023...]。
        String newDescription = String.format("delete %s %s[%s]=>[%s]。\n", sdf.format(new Date()), model.getTitle(), model.getEntityName(), newTableName) + model.getDescription();
        // 数据表处理
        Map<String, Object> sqlParams = new HashMap<>();
        sqlParams.put("newEntityName", newTableName);// 新
        sqlParams.put("entityName", model.getEntityName());// 旧
        sqlParams.put("newComment", newComment);
        sqlParams.put("delStatus", DeleteStatusEnum.IS.getCode());
        sqlParams.put("enableStatus", EnableStatusEnum.DISABLED.getCode());
        sqlParams.put("remark", "delete table. \n");
        if (TableTypeEnum.TABLE.getCode().equals(model.getTableType())) {
            List<Map<String, Object>> tableList = queryInformationSchemaTables(model.getEntityName());
            if (tableList != null && !tableList.isEmpty()) {
                sqlParams.put("isTable", true);
                model.setTableName(newTableName);
            } else {
                model.setTableName(null);
            }
        } else {
            model.setTableName(null);
        }
        // 修正字段、外键、视图
        dao.execute("metaResetOrDeleteTable", sqlParams);
        // 删除，信息变更
        model.setDescription(newDescription);
        model.setTableComment(newComment);
        model.setTitle(newTitle);
        model.setLinked(LinkedEnum.NO.getValue());
        // 删除，表名变更
        model.setEntityName(newTableName);
        // 删除，标记变更
        model.setEnableStatus(EnableStatusEnum.DISABLED.getCode());
        model.setDelStatus(DeleteStatusEnum.IS.getCode());
        model.setSeqNo(ColumnDefault.SEQ_NO_DELETE);
        dao.save(model);
    }

    /**
     * 查询数据库表信息
     *
     * @param tableName 表名称
     * @return
     */
    private List<Map<String, Object>> queryInformationSchemaTables(String tableName) {
        List<Map<String, Object>> tableList = new ArrayList<>();
        if (Strings.isNotBlank(tableName)) {
            String tableSql = String.format(MetaDaoSql.INFORMATION_SCHEMA_TABLES, MetaDaoSql.TABLE_SCHEMA_METHOD, " AND TABLE_NAME='" + tableName + "'");
            logger.info(tableSql);
            tableList = dao.getJdbcTemplate().queryForList(tableSql);
        }
        return tableList;
    }
}
