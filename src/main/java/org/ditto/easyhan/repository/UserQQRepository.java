package org.ditto.easyhan.repository;

import org.apache.ignite.springdata.repository.IgniteRepository;
import org.apache.ignite.springdata.repository.config.Query;
import org.apache.ignite.springdata.repository.config.RepositoryConfig;
import org.ditto.easyhan.model.qq.UserQQ;
import org.ditto.easyhan.model.qq.UserQQKey;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RepositoryConfig(cacheName = Constants.USER_QQ_CACHE_NAME)
public interface UserQQRepository extends IgniteRepository<UserQQ, UserQQKey> {

    @Query("SELECT * FROM UserQQ WHERE lastUpdated >= ? ORDER BY lastUpdated ASC")
    List<UserQQ> getAllBy(long startLastUpdated, Pageable pageable);

}