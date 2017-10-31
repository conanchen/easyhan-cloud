package org.ditto.easyhan.repository;

import org.apache.ignite.springdata.repository.IgniteRepository;
import org.apache.ignite.springdata.repository.config.Query;
import org.apache.ignite.springdata.repository.config.RepositoryConfig;
import org.ditto.easyhan.model.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RepositoryConfig(cacheName = Constants.USER_CACHE_NAME)
public interface UserRepository extends IgniteRepository<User, String> {

    @Query("SELECT * FROM User WHERE level = ? AND lastUpdated >= ? ORDER BY lastUpdated ASC")
    List<org.ditto.easyhan.model.User> getAllBy(long startLastUpdated, Pageable pageable);

}