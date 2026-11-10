package org.delcom.ifs25020pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25020pbo2026springbootcashflow.domain.repository.IRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Base generic in-memory repository.
 *
 * @param <T>  tipe entitas
 * @param <ID> tipe identifier
 */
public abstract class InMemoryRepository<T, ID> implements IRepository<T, ID> {
    protected final List<T> data = new ArrayList<>();
    private final Function<T, ID> idExtractor;

    protected InMemoryRepository(Function<T, ID> idExtractor) {
        this.idExtractor = idExtractor;
    }

    @Override
    public List<T> findAll() {
        // Salinan defensif agar data internal tidak termutasi dari luar
        return new ArrayList<>(data);
    }

    @Override
    public Optional<T> findById(ID id) {
        return data.stream().filter(item -> idExtractor.apply(item).equals(id)).findFirst();
    }

    @Override
    public T save(T entity) {
        data.add(entity);
        return entity;
    }

    @Override
    public boolean deleteById(ID id) {
        return data.removeIf(item -> idExtractor.apply(item).equals(id));
    }

    @Override
    public void update(T entity) {
        // Objek mutable disimpan by-reference dalam memori.
    }

    @Override
    public List<T> findBy(Predicate<T> predicate) {
        return data.stream().filter(predicate).toList();
    }
}
