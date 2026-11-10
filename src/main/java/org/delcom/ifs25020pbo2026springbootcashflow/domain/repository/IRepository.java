package org.delcom.ifs25020pbo2026springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Generic Repository Port untuk operasi CRUD dan pencarian.
 *
 * @param <T>  tipe entitas domain
 * @param <ID> tipe identifier entitas
 */
public interface IRepository<T, ID> {
    /** Mengambil semua data. */
    List<T> findAll();

    /** Mencari satu entitas berdasarkan ID. */
    Optional<T> findById(ID id);

    /** Menyimpan entitas baru. */
    T save(T entity);

    /** Menghapus berdasarkan ID; true jika berhasil dihapus. */
    boolean deleteById(ID id);

    /** Menyimpan perubahan entitas yang sudah ada. */
    void update(T entity);

    /** Mencari entitas yang memenuhi predikat. */
    List<T> findBy(Predicate<T> predicate);
}
