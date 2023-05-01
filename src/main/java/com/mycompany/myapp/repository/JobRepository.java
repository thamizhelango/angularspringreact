package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Job;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Job entity.
 */
@SuppressWarnings("unused")
@Repository
public interface JobRepository extends ReactiveCrudRepository<Job, Long>, JobRepositoryInternal {
    Flux<Job> findAllBy(Pageable pageable);

    @Override
    Mono<Job> findOneWithEagerRelationships(Long id);

    @Override
    Flux<Job> findAllWithEagerRelationships();

    @Override
    Flux<Job> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT entity.* FROM job entity JOIN rel_job__task joinTable ON entity.id = joinTable.task_id WHERE joinTable.task_id = :id")
    Flux<Job> findByTask(Long id);

    @Query("SELECT * FROM job entity WHERE entity.employee_id = :id")
    Flux<Job> findByEmployee(Long id);

    @Query("SELECT * FROM job entity WHERE entity.employee_id IS NULL")
    Flux<Job> findAllWhereEmployeeIsNull();

    @Override
    <S extends Job> Mono<S> save(S entity);

    @Override
    Flux<Job> findAll();

    @Override
    Mono<Job> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface JobRepositoryInternal {
    <S extends Job> Mono<S> save(S entity);

    Flux<Job> findAllBy(Pageable pageable);

    Flux<Job> findAll();

    Mono<Job> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Job> findAllBy(Pageable pageable, Criteria criteria);

    Mono<Job> findOneWithEagerRelationships(Long id);

    Flux<Job> findAllWithEagerRelationships();

    Flux<Job> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
