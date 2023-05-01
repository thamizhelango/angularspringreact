package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Region;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Region entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RegionRepository extends ReactiveCrudRepository<Region, Long>, RegionRepositoryInternal {
    @Override
    <S extends Region> Mono<S> save(S entity);

    @Override
    Flux<Region> findAll();

    @Override
    Mono<Region> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface RegionRepositoryInternal {
    <S extends Region> Mono<S> save(S entity);

    Flux<Region> findAllBy(Pageable pageable);

    Flux<Region> findAll();

    Mono<Region> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Region> findAllBy(Pageable pageable, Criteria criteria);

}
