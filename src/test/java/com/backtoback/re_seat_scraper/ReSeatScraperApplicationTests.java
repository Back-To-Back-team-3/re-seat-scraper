package com.backtoback.re_seat_scraper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "scraper.seed.enabled=false",
        "scraper.collect.enabled=false"
})
class ReSeatScraperApplicationTests {

	@Test
	void contextLoads() {
	}

}
