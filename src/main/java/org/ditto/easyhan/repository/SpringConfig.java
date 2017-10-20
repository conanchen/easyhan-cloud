package org.ditto.easyhan.repository;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.PersistentStoreConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.springdata.repository.config.EnableIgniteRepositories;
import org.ditto.easyhan.model.Breed;
import org.ditto.easyhan.model.Dog;
import org.ditto.easyhan.model.Word;
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

        // Setting SQL schema for the cache.
        ccfgWord.setIndexedTypes(String.class, Word.class);
        ccfgBreed.setIndexedTypes(Long.class, Breed.class);
        ccfgDog.setIndexedTypes(Long.class, Dog.class);

        ignite.getOrCreateCache(ccfgDog);
        ignite.getOrCreateCache(ccfgBreed);
        ignite.getOrCreateCache(ccfgWord);


        return ignite;
    }
}