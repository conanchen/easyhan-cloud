package org.ditto.easyhan.repository;

import org.apache.ignite.springdata.repository.IgniteRepository;
import org.apache.ignite.springdata.repository.config.Query;
import org.apache.ignite.springdata.repository.config.RepositoryConfig;
import org.ditto.easyhan.model.Word;
import org.easyhan.common.grpc.HanziLevel;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RepositoryConfig(cacheName = Constants.WORD_CACHE_NAME)
public interface WordRepository extends IgniteRepository<Word, String> {

    @Query("SELECT * FROM Word WHERE level = ? AND lastUpdated >= ? ORDER BY lastUpdated ASC")
    List<Word> getAllBy(HanziLevel level, long startLastUpdated, Pageable pageable);

}