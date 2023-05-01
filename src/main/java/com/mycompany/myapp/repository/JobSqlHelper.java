package com.mycompany.myapp.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class JobSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("job_title", table, columnPrefix + "_job_title"));
        columns.add(Column.aliased("min_salary", table, columnPrefix + "_min_salary"));
        columns.add(Column.aliased("max_salary", table, columnPrefix + "_max_salary"));

        columns.add(Column.aliased("employee_id", table, columnPrefix + "_employee_id"));
        return columns;
    }
}
