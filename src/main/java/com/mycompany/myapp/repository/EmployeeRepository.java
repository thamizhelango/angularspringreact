package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Employee;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Employee entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EmployeeRepository extends ReactiveCrudRepository<Employee, Long>, EmployeeRepositoryInternal {
    Flux<Employee> findAllBy(Pageable pageable);

    @Query("SELECT * FROM employee entity WHERE entity.manager_id = :id")
    Flux<Employee> findByManager(Long id);

    @Query("SELECT * FROM employee entity WHERE entity.manager_id IS NULL")
    Flux<Employee> findAllWhereManagerIsNull();

    @Query("SELECT * FROM employee entity WHERE entity.department_id = :id")
    Flux<Employee> findByDepartment(Long id);

    @Query("SELECT * FROM employee entity WHERE entity.department_id IS NULL")
    Flux<Employee> findAllWhereDepartmentIsNull();

    @Override
    <S extends Employee> Mono<S> save(S entity);

    @Override
    Flux<Employee> findAll();

    @Override
    Mono<Employee> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface EmployeeRepositoryInternal {
    <S extends Employee> Mono<S> save(S entity);

    Flux<Employee> findAllBy(Pageable pageable);

    Flux<Employee> findAll();

    Mono<Employee> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Employee> findAllBy(Pageable pageable, Criteria criteria);

}
