package org.ditto.easyhan.controller;

import com.google.gson.Gson;
import org.apache.ignite.Ignite;
import org.ditto.easyhan.model.Breed;
import org.ditto.easyhan.model.Word;
import org.ditto.easyhan.repository.BreedRepository;
import org.ditto.easyhan.repository.WordRepository;
import org.easyhan.common.grpc.HanziLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.bus.Event;
import reactor.bus.EventBus;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@EnableAutoConfiguration
public class HelloController {
    private final static Gson gson = new Gson();
    @Autowired
    private Ignite ignite;
    @Autowired
    private BreedRepository breedRepository;

    @Autowired
    private WordRepository wordRepository;

    @RequestMapping(value = "/hello")
    public String hello() {

        //fill the repository with data and Save
        Breed collie = new Breed();
        collie.setId(1L);
        collie.setName("collie");
        //save Breed with name collie
        breedRepository.save(1L, collie);

        System.out.println("Add one breed in the repository!");
        // Query the breed
        List<Breed> getAllBreeds = breedRepository.getAllBreedsByName("collie");

        List<Long> ids = breedRepository.getById(1L, new PageRequest(0, 4));
        String breeds = "";
        for (Breed breed : getAllBreeds) {
            breeds += ("Breed:" + breed + "\n");
        }

        String images = insertAndGetImage();

        return "hello@" + DateFormat.getInstance().format(new Date()) + ",Spring Boot " + breeds
                + String.format(" breeds.ids=[%s]", gson.toJson(ids))
                + String.format(" images=[%s]", images);
    }

    private String insertAndGetImage() {
        for (int i = 0; i < 3; i++) {
            Word word = Word.builder()
                    .setWord(String.format("word%d", i))
                    .setLevel(HanziLevel.ONE)
                    .setLastUpdated(System.currentTimeMillis())
                    .build();
            wordRepository.save(word.getWord(), word);
        }
        List<Word> ids = wordRepository.getAllBy(HanziLevel.ONE, 0, new PageRequest(0, 10));
        return gson.toJson(ids);
    }

    @Autowired
    EventBus eventBus;

    @RequestMapping(value = "/publish/{num}/{seconds}")
    public String publish(@PathVariable("num") int num,@PathVariable("seconds") int seconds) throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(1);

        for (int i = 0; i < num; i++) {
            eventBus.notify("quotes", Event.wrap(counter.getAndIncrement()));
            Thread.sleep(seconds);
        }

        return String.format("publish@%s, num=%d, seconds=%d. seconds must be > 0", DateFormat.getInstance().format(new Date()),num,seconds);
    }
}