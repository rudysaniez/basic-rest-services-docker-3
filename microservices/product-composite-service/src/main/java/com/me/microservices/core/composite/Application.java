package com.me.microservices.core.composite;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.me.api.core.product.health.ProductHealth;
import com.me.api.core.recommendation.health.RecommendationHealth;
import com.me.api.core.review.health.ReviewHealth;
import com.me.microservices.core.composite.mapper.PagedMapper;
import com.me.microservices.core.composite.mapper.PagedMapperImpl;
import com.me.microservices.core.composite.mapper.RecommendationMapper;
import com.me.microservices.core.composite.mapper.RecommendationMapperImpl;
import com.me.microservices.core.composite.mapper.ReviewMapper;
import com.me.microservices.core.composite.mapper.ReviewMapperImpl;
import com.me.microservices.core.composite.producer.MessageProcessor;

import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Mono;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableBinding(value = MessageProcessor.class)
@EnableConfigurationProperties(value=Application.PaginationInformation.class)
@ComponentScan(basePackages= {"com.me.microservices.core", "com.me.handler.http"})
@SpringBootApplication
public class Application {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Bean
	public RecommendationMapper recommendationMapper() {
		return new RecommendationMapperImpl();
	}
	
	@Bean
	public ReviewMapper reviewMapper() {
		return new ReviewMapperImpl();
	}
	
	@Bean
	public PagedMapper pagedMapper() {
		return new PagedMapperImpl();
	}
	
	@Getter @Setter
	@ConfigurationProperties(prefix="api.pagination")
	public static class PaginationInformation {
		
		private int pageNumber;
		private int pageSize;
	}
	
	
	@Autowired private ProductHealth productHealth;
	@Autowired private RecommendationHealth recommendationHealth;
	@Autowired private ReviewHealth reviewHealth;
	
	@Bean
	public CompositeReactiveHealthContributor coreMicroservices() {
		
		ReactiveHealthContributor productContributor = new ReactiveHealthIndicator() {
			@Override
			public Mono<Health> health() {
				return productHealth.getProductHealth();
			}
		};
		
		ReactiveHealthContributor recommendationContributor = new ReactiveHealthIndicator() {
			@Override
			public Mono<Health> health() {
				return recommendationHealth.getRecommendationHealth();
			}
		};
		
		ReactiveHealthContributor reviewContributor = new ReactiveHealthIndicator() {
			@Override
			public Mono<Health> health() {
				return reviewHealth.getReviewHealth();
			}
		};
		
		Map<String, ReactiveHealthContributor> contributors = new HashMap<>();
		contributors.put("product", productContributor);
		contributors.put("recommendation", recommendationContributor);
		contributors.put("review", reviewContributor);
		
		return CompositeReactiveHealthContributor.fromMap(contributors);
	}
	
	/**
	 * <pre>
	 * http://$HOST:$PORT/api/v1/swagger-ui.html
	 * </pre>
	 * @author rudysaniez
	 */
	@Profile("product-composite-swagger")
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
	          .apis(RequestHandlerSelectors.basePackage("com.me.work.example.microservices.core.composite"))              
	          .paths(PathSelectors.any())                          
	          .build()
	          .apiInfo(new ApiInfo(title, description, version, termsOfServiceUrl, 
	        		  new Contact(contactName, contactUrl, contactEmail), 
	        		  	license, licenseUrl, java.util.Collections.emptyList()));
	    }
	}
}
