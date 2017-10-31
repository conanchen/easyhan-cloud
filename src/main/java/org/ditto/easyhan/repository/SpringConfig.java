package org.ditto.easyhan.repository;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.PersistentStoreConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.springdata.repository.config.EnableIgniteRepositories;
import org.ditto.easyhan.model.*;
import org.ditto.easyhan.model.qq.UserQQ;
import org.ditto.easyhan.model.qq.UserQQKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@EnableIgniteRepositories
public class SpringConfig {
//    private static final Logger slf4jLogger = LoggerFactory.getLogger(SpringConfig.class);

    @Bean
    public Ignite igniteInstance() {
        IgniteConfiguration cfg = new IgniteConfiguration();
        // Setting some custom name for the node.
        cfg.setIgniteInstanceName("EasyhanDataNode");

        // Enabling peer-class loading feature.
        cfg.setPeerClassLoadingEnabled(true);
//        Slf4jLogger gridLog = new Slf4jLogger(slf4jLogger); // Provide correct SLF4J logger here.

//        cfg.setGridLogger(gridLog);
        cfg.setPersistentStoreConfiguration(new PersistentStoreConfiguration());

        TcpDiscoverySpi discovery = new TcpDiscoverySpi();

        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();

        ipFinder.setAddresses(Arrays.asList("127.0.0.1:47500..47510"));

        discovery.setIpFinder(ipFinder);

        cfg.setDiscoverySpi(discovery);


        Ignite ignite = Ignition.start(cfg);
        ignite.active(true);


        // Defining and creating a new cache to be used by Ignite Spring Data
        // repository.
        CacheConfiguration<Long, Dog> ccfgDog = new CacheConfiguration<>("DogCache");
        CacheConfiguration<Long, Breed> ccfgBreed = new CacheConfiguration<>("BreedCache");
        CacheConfiguration<String, Word> ccfgWord = new CacheConfiguration<>("ZhwordCache");

        ccfgWord.setIndexedTypes(String.class, Word.class);
        ccfgBreed.setIndexedTypes(Long.class, Breed.class);
        ccfgDog.setIndexedTypes(Long.class, Dog.class);

        ignite.getOrCreateCache(ccfgDog);
        ignite.getOrCreateCache(ccfgBreed);
        ignite.getOrCreateCache(ccfgWord);

        getOrCreateUserWordCache(ignite);
        getOrCreateUserCache(ignite);
        getOrCreateUserQQCache(ignite);
        return ignite;
    }

    private IgniteCache<UserWordKey, UserWord> getOrCreateUserWordCache(Ignite ignite) {
        CacheConfiguration<UserWordKey, UserWord> ccfgUserWord = new CacheConfiguration<>(Constants.USER_WORD_CACHE_NAME);
        // Setting SQL schema for the cache.
        ccfgUserWord.setIndexedTypes(UserWordKey.class, UserWord.class);
        return ignite.getOrCreateCache(ccfgUserWord);
    }

    private IgniteCache<String, User> getOrCreateUserCache(Ignite ignite) {
        CacheConfiguration<String, User> ccfg = new CacheConfiguration<>(Constants.USER_CACHE_NAME);
        // Setting SQL schema for the cache.
        ccfg.setIndexedTypes(String.class, User.class);
        return ignite.getOrCreateCache(ccfg);
    }

    private IgniteCache<UserQQKey, UserQQ> getOrCreateUserQQCache(Ignite ignite) {
        CacheConfiguration<UserQQKey, UserQQ> ccfg = new CacheConfiguration<>(Constants.USER_QQ_CACHE_NAME);
        // Setting SQL schema for the cache.
        ccfg.setIndexedTypes(UserQQKey.class, UserQQ.class);
        return ignite.getOrCreateCache(ccfg);
    }
}