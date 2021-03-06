package org.ditto.easyhan.repository;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.*;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.springdata.repository.config.EnableIgniteRepositories;
import org.ditto.easyhan.model.*;
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

        cfg.setMemoryConfiguration(memoryConfig());

        Ignite ignite = Ignition.start(cfg);
        ignite.active(true);


        // Defining and creating a new cache to be used by Ignite Spring Data
        // repository.
        CacheConfiguration<Long, Dog> ccfgDog = new CacheConfiguration<>("DogCache");
        CacheConfiguration<Long, Breed> ccfgBreed = new CacheConfiguration<>("BreedCache");

        ccfgBreed.setIndexedTypes(Long.class, Breed.class);
        // Setting a memory policy name to bind to a specific memory region.
        ccfgBreed.setMemoryPolicyName("500MB_Region_Eviction");
        ccfgDog.setIndexedTypes(Long.class, Dog.class);
// Setting a memory policy name to bind to a specific memory region.
        ccfgDog.setMemoryPolicyName("500MB_Region_Eviction");

        ignite.getOrCreateCache(ccfgDog);
        ignite.getOrCreateCache(ccfgBreed);

        getOrCreateWordCache(ignite);
        getOrCreateUserWordCache(ignite);
        getOrCreateUserCache(ignite);
        return ignite;
    }

    private MemoryConfiguration memoryConfig() {
        // Durable Memory configuration.
        MemoryConfiguration memCfg = new MemoryConfiguration();

// Creating a custom memory policy for a new memory region.
        MemoryPolicyConfiguration plCfg = new MemoryPolicyConfiguration();

// Policy/region name.
        plCfg.setName("500MB_Region_Eviction");

// Setting initial size.
        plCfg.setInitialSize(100L * 1024 * 1024);

// Setting maximum size.
        plCfg.setMaxSize(500L * 1024 * 1024);

// Setting data pages eviction algorithm.
//         plCfg.setPageEvictionMode(DataPageEvictionMode.RANDOM_2_LRU);

// Applying the memory policy.
        memCfg.setMemoryPolicies(plCfg);
        return memCfg;
    }

    private IgniteCache<String, Word> getOrCreateWordCache(Ignite ignite) {
        CacheConfiguration<String, Word> ccfgWord = new CacheConfiguration<>(Constants.WORD_CACHE_NAME);
        // Setting SQL schema for the cache.
        ccfgWord.setIndexedTypes(String.class, Word.class);
        // Setting a memory policy name to bind to a specific memory region.
        ccfgWord.setMemoryPolicyName("500MB_Region_Eviction");
        return ignite.getOrCreateCache(ccfgWord);
    }

    private IgniteCache<UserWordKey, UserWord> getOrCreateUserWordCache(Ignite ignite) {
        CacheConfiguration<UserWordKey, UserWord> ccfgUserWord = new CacheConfiguration<>(Constants.USER_WORD_CACHE_NAME);
        // Setting SQL schema for the cache.
        ccfgUserWord.setIndexedTypes(UserWordKey.class, UserWord.class);
        // Setting a memory policy name to bind to a specific memory region.
        ccfgUserWord.setMemoryPolicyName("500MB_Region_Eviction");
        return ignite.getOrCreateCache(ccfgUserWord);
    }

    private IgniteCache<UserKey, User> getOrCreateUserCache(Ignite ignite) {
        CacheConfiguration<UserKey, User> ccfg = new CacheConfiguration<>(Constants.USER_CACHE_NAME);
        // Setting SQL schema for the cache.
        ccfg.setIndexedTypes(UserKey.class, User.class);
        // Setting a memory policy name to bind to a specific memory region.
        ccfg.setMemoryPolicyName("500MB_Region_Eviction");
        return ignite.getOrCreateCache(ccfg);
    }
}