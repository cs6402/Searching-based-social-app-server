package com.tdsoft.bro.taguploader;

import static springfox.documentation.builders.PathSelectors.regex;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SpringConfig {

	@Bean
	public Docket swaggerSpringMvcPlugin() {
		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any()).paths(regex("/.*")).build()
				.pathMapping("/tc").apiInfo(metadata());

		// .groupName("business-api")
		// .select()
		// Ignores controllers annotated with @CustomIgnore
		// .apis("") //Selection by RequestHandler
		// .paths(paths()) // and by paths
		// .build();
		// .apiInfo(apiInfo())
		// .securitySchemes(securitySchemes())
		// .securityContext(securityContext());
	}


	private ApiInfo metadata() {
		return new ApiInfoBuilder().title("Bro Tag Center API").description("Hostname請保留，目前此模組位址暫定https://127.0.0.1/tc/**。建議彈性設計HttpClient的URL設定").version("Pre-alpha").contact("cs6402@gmail")
				.licenseUrl("bitbucket.org/cs6402/bro").build();
	}
}
