package edu.ban7.springdemo.upload;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SpringDemoUploadApplicationTests {


	private MockMvc mockMvc;

	@BeforeEach
	public void setUp(WebApplicationContext webApplicationContext) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	public void testSaveUserWithInvalidAvatarFileExtension_shouldSend400BadRequest() throws Exception {
		MockMultipartFile invalidFile = new MockMultipartFile("avatar", "invalid.txt", "text/plain", "Invalid content".getBytes());
		MockMultipartFile userJson = new MockMultipartFile("user", "", "application/json", "{\"pseudo\":\"test\"}".getBytes());

		mockMvc.perform(multipart("/utilisateur")
						.file(userJson)
						.file(invalidFile))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testSaveUserWithInvalidAvatarFileSize_shouldSend400BadRequest() throws Exception {
		// Create a mock file with invalid size
		MockMultipartFile invalidFile = new MockMultipartFile(
				"avatar",
				"invalid.png",
				"image/png",
				new byte[2*1024*1024 + 1]//2mo + 1 octet
		);

		MockMultipartFile userJson = new MockMultipartFile(
				"user",
				"",
				"application/json",
				"{\"pseudo\":\"test\"}".getBytes()
		);

		// Perform the request and verify the response
		mockMvc.perform(multipart("/utilisateur")
						.file(userJson)
						.file(invalidFile))
				.andExpect(status().isBadRequest());
	}

}
