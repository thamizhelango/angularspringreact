package com.mycompany.myapp.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import com.mycompany.myapp.domain.Department;
import com.mycompany.myapp.repository.rowmapper.DepartmentRowMapper;
import com.mycompany.myapp.repository.rowmapper.LocationRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
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
 * Spring Data R2DBC custom repository implementation for the Department entity.
 */
@SuppressWarnings("unused")
class DepartmentRepositoryInternalImpl extends SimpleR2dbcRepository<Department, Long> implements DepartmentRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final LocationRowMapper locationMapper;
    private final DepartmentRowMapper departmentMapper;

    private static final Table entityTable = Table.aliased("department", EntityManager.ENTITY_ALIAS);
    private static final Table locationTable = Table.aliased("location", "location");

    public DepartmentRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        LocationRowMapper locationMapper,
        DepartmentRowMapper departmentMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Department.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.locationMapper = locationMapper;
        this.departmentMapper = departmentMapper;
    }

    @Override
    public Flux<Department> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Department> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = DepartmentSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(LocationSqlHelper.getColumns(locationTable, "location"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(locationTable)
            .on(Column.create("location_id", entityTable))
            .equals(Column.create("id", locationTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Department.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Department> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Department> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private Department process(Row row, RowMetadata metadata) {
        Department entity = departmentMapper.apply(row, "e");
        entity.setLocation(locationMapper.apply(row, "location"));
        return entity;
    }

    @Override
    public <S extends Department> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
