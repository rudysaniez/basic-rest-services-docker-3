package com.me.work.example.microservices.core.product;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@ComponentScan(basePackages= {"com.me.work.example.microservices.core", "com.me.work.example.handler.http"})
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	/**
	 * <pre>
	 * http://$HOST:$PORT/api/v1/swagger-ui.html
	 * </pre>
	 * @author rudysaniez
	 */
	@Profile("product-swagger")
	@Configuration
	@EnableSwagger2
	public class SpringFoxSwagger {
		
		@Value("${api.common.version}") String version;
		@Value("${api.common.title}") String title;
		@Value("${api.common.description}") String description;
		@Value("${api.common.termsOfServiceUrl}") String termsOfServiceUrl;
		@Value("${api.common.license}") String license;
		@Value("${api.common.licenseUrl}") String licenseUrl;
		@Value("${api.common.contact.name}") String contactName;
		@Value("${api.common.contact.url}") String contactUrl;
		@Value("${api.common.contact.email}") String contactEmail;
		
		@Bean
	    public Docket api() { 
			
	        return new Docket(DocumentationType.SWAGGER_2)  
	          .select()                                  
	          .apis(RequestHandlerSelectors.basePackage("com.me.work.example.microservices.core.product"))              
	          .paths(PathSelectors.any())                          
	          .build()
	          .apiInfo(new ApiInfo(title, description, version, termsOfServiceUrl, 
	        		  new Contact(contactName, contactUrl, contactEmail), 
	        		  	license, licenseUrl, java.util.Collections.emptyList()));
	    }
	}
}
