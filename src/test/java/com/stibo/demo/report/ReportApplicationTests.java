package com.stibo.demo.report;

import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAutoConfiguration
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
public class ReportApplicationTests {
	
	@Value("${authorization.user}")
	private String user;
	
	@Value("${authorization.password}")
	private String password;
	
	@Value("${test.results.datafile}")
	private String resultsDatafile;
	
	@Autowired
	private MockMvc mvc;
	
	@Test
	public void contextLoads() {
	}

	@Test
	public void ReportEndpointTestOnRealData() throws Exception {
		
		MvcResult result = mvc.perform(
				get("/report/acme/T_SHIRTS")
						.with(httpBasic(user, password))
		)
				.andExpect(status().isOk())
				.andReturn();
		
		Files.write(Paths.get(resultsDatafile), result.getResponse().getContentAsString().getBytes());
	}
	
}

