/*
 * Copyright 2018-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;

@SpringBootTest
public class ApplicationTests {
	@Autowired
	private KafkaTemplate<Object, Object> template;
	int counter;
	private final Logger LOGGER = LoggerFactory.getLogger(ApplicationTests.class);

	@Test
	public void contextLoads() {
		await().atLeast(Duration.ofSeconds(5)).until(()->true); // give few seconds to load up.
		sendMessages("bar1,bar2");
		await().atLeast(Duration.ofSeconds(20)).until(()->true);
		sendMessages("bar1,bar2");
		await().atLeast(Duration.ofSeconds(80)).until(()->true); // should blow up by end of it.

	}

	private void sendMessages(String data){
		this.template.executeInTransaction(kafkaTemplate -> {
			StringUtils.commaDelimitedListToSet(data).stream()
					.map(s -> new String(s))
					.forEach(foo -> template.send("topic2", foo, foo));
			if (counter % 2 != 0) {
				LOGGER.info("Waiting to producer transaction commit");
				try {
					Thread.sleep(20000);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				LOGGER.info("Producer committed");
			}
			return null;
		});
		counter++;
	}

}
