package com.github.everything;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

@SpringBootTest
class EverythingApplicationTests {
	@Autowired
	DataSource dataSource;
	@Autowired
	OneDao oneDao;

	@Test
	void contextLoads() {
		System.out.println(dataSource);
		oneDao.select("select * from product");
	}

}
