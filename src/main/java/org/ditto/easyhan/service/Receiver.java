package org.ditto.easyhan.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.bus.Event;
import reactor.fn.Consumer;

import java.util.concurrent.CountDownLatch;

@Service
public class Receiver implements Consumer<Event<Integer>> {

	@Autowired
    CountDownLatch latch;

	RestTemplate restTemplate = new RestTemplate();

	public void accept(Event<Integer> ev) {
		System.out.print(",Quote " + ev.getData()  );
		latch.countDown();
	}

}