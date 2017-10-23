package org.ditto.easyhan.repository;

import org.apache.ignite.springdata.repository.IgniteRepository;
import org.apache.ignite.springdata.repository.config.Query;
import org.apache.ignite.springdata.repository.config.RepositoryConfig;
import org.ditto.easyhan.model.UserWord;
import org.ditto.easyhan.model.UserWordKey;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RepositoryConfig(cacheName = Constants.USER_WORD_CACHE_NAME)
public interface UserWordRepository extends IgniteRepository<UserWord, UserWordKey> {

    @Query("SELECT * FROM UserWord WHERE userId = ? AND lastUpdated >= ? ORDER BY lastUpdated ASC")
    List<UserWord> getAllBy(String userId, long startLastUpdated, Pageable pageable);

}