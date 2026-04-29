package stereovision.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T> {
    T create(T entity);
    Optional<T> read(int id);
    List<T> readAll();
    void update(T entity);
    void delete(int id);
}
