package com.mycompany.myapp.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import com.mycompany.myapp.domain.JobHistory;
import com.mycompany.myapp.domain.enumeration.Language;
import com.mycompany.myapp.repository.rowmapper.DepartmentRowMapper;
import com.mycompany.myapp.repository.rowmapper.EmployeeRowMapper;
import com.mycompany.myapp.repository.rowmapper.JobHistoryRowMapper;
import com.mycompany.myapp.repository.rowmapper.JobRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the JobHistory entity.
 */
@SuppressWarnings("unused")
class JobHistoryRepositoryInternalImpl extends SimpleR2dbcRepository<JobHistory, Long> implements JobHistoryRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final JobRowMapper jobMapper;
    private final DepartmentRowMapper departmentMapper;
    private final EmployeeRowMapper employeeMapper;
    private final JobHistoryRowMapper jobhistoryMapper;

    private static final Table entityTable = Table.aliased("job_history", EntityManager.ENTITY_ALIAS);
    private static final Table jobTable = Table.aliased("job", "job");
    private static final Table departmentTable = Table.aliased("department", "department");
    private static final Table employeeTable = Table.aliased("employee", "employee");

    public JobHistoryRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        JobRowMapper jobMapper,
        DepartmentRowMapper departmentMapper,
        EmployeeRowMapper employeeMapper,
        JobHistoryRowMapper jobhistoryMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(JobHistory.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.jobMapper = jobMapper;
        this.departmentMapper = departmentMapper;
        this.employeeMapper = employeeMapper;
        this.jobhistoryMapper = jobhistoryMapper;
    }

    @Override
    public Flux<JobHistory> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<JobHistory> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = JobHistorySqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(JobSqlHelper.getColumns(jobTable, "job"));
        columns.addAll(DepartmentSqlHelper.getColumns(departmentTable, "department"));
        columns.addAll(EmployeeSqlHelper.getColumns(employeeTable, "employee"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(jobTable)
            .on(Column.create("job_id", entityTable))
            .equals(Column.create("id", jobTable))
            .leftOuterJoin(departmentTable)
            .on(Column.create("department_id", entityTable))
            .equals(Column.create("id", departmentTable))
            .leftOuterJoin(employeeTable)
            .on(Column.create("employee_id", entityTable))
            .equals(Column.create("id", employeeTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, JobHistory.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<JobHistory> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<JobHistory> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private JobHistory process(Row row, RowMetadata metadata) {
        JobHistory entity = jobhistoryMapper.apply(row, "e");
        entity.setJob(jobMapper.apply(row, "job"));
        entity.setDepartment(departmentMapper.apply(row, "department"));
        entity.setEmployee(employeeMapper.apply(row, "employee"));
        return entity;
    }

    @Override
    public <S extends JobHistory> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
