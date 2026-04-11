package org.example.dao.base;

import java.util.List;

public interface IGenericDao<T> {
    boolean insert(T entity);
    boolean update(T entity);
    boolean delete(int id);
    T getById(int id);
    List<T> getAll();
}
