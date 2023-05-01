package com.mycompany.myapp.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import com.mycompany.myapp.domain.Country;
import com.mycompany.myapp.repository.rowmapper.CountryRowMapper;
import com.mycompany.myapp.repository.rowmapper.RegionRowMapper;
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
 * Spring Data R2DBC custom repository implementation for the Country entity.
 */
@SuppressWarnings("unused")
class CountryRepositoryInternalImpl extends SimpleR2dbcRepository<Country, Long> implements CountryRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final RegionRowMapper regionMapper;
    private final CountryRowMapper countryMapper;

    private static final Table entityTable = Table.aliased("country", EntityManager.ENTITY_ALIAS);
    private static final Table regionTable = Table.aliased("region", "region");

    public CountryRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        RegionRowMapper regionMapper,
        CountryRowMapper countryMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Country.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.regionMapper = regionMapper;
        this.countryMapper = countryMapper;
    }

    @Override
    public Flux<Country> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Country> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = CountrySqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(RegionSqlHelper.getColumns(regionTable, "region"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(regionTable)
            .on(Column.create("region_id", entityTable))
            .equals(Column.create("id", regionTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Country.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Country> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Country> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private Country process(Row row, RowMetadata metadata) {
        Country entity = countryMapper.apply(row, "e");
        entity.setRegion(regionMapper.apply(row, "region"));
        return entity;
    }

    @Override
    public <S extends Country> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
