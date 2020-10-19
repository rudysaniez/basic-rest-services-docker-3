package com.me.microservices.core.composite.services;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.me.api.Api;
import com.me.api.core.common.Paged;
import com.me.api.core.product.Product;
import com.me.api.core.product.ProductService;
import com.me.api.core.recommendation.Recommendation;
import com.me.api.core.recommendation.RecommendationService;
import com.me.api.core.review.Review;
import com.me.api.core.review.ReviewService;
import com.me.handler.exception.InvalidInputException;
import com.me.handler.exception.NotFoundException;
import com.me.handler.http.HttpErrorInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * @author rudysaniez @since 0.0.1
 */
@Slf4j
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

	private final ObjectMapper jack;
	private final RestTemplate restTemplate;

	private final String productServiceUrl;
	private final String recommendationServiceUrl;
	private final String reviewServiceUrl;
	
	/**
	 * @param jack
	 * @param restTemplate
	 * @param productServiceHost
	 * @param productServicePort
	 * @param recommendationServiceHost
	 * @param recommendationServicePort
	 * @param reviewServiceHost
	 * @param reviewServicePort
	 * @param basePath
	 */
	@Autowired
	public ProductCompositeIntegration(ObjectMapper jack, RestTemplate restTemplate,
			
			/**
			 * Products services : URL + PORT.
			 */
			@Value("${app.product-service.host}") String productServiceHost,
			@Value("${app.product-service.port}") int productServicePort,
			
			/**
			 * Recommendations services : URL + PORT.
			 */
			@Value("${app.recommendation-service.host}") String recommendationServiceHost,
			@Value("${app.recommendation-service.port}") int recommendationServicePort,
			
			/**
			 * Reviews services : URL + PORT.
			 */
			@Value("${app.review-service.host}") String reviewServiceHost,
			@Value("${app.review-service.port}") int reviewServicePort,
			
			/**
			 * Base path : /api/v1
			 */
			@Value("${spring.webflux.base-path}") String basePath) {
		
		this.jack = jack;
		this.restTemplate = restTemplate;
		
		log.debug("Parameters : app.product-service.host={}, app.product-service.port={},"
				+ "app.recommendation-service.host={}, app.recommendation-service.port={},"
				+ "app.review-service.host={}, app.review-service.port={},"
				+ "spring.webflux.base-path={}", 
				productServiceHost, productServicePort,
				recommendationServiceHost, recommendationServicePort,
				reviewServiceHost, reviewServicePort,
				basePath);
		
		//Product-service
		StringBuilder sb = new StringBuilder("http://");
		sb.append(productServiceHost).append(":").append(productServicePort).append(basePath).
		append("/").append(Api.PRODUCT_PATH);
		
		this.productServiceUrl = sb.toString();
		log.debug("L'URL du service product : " + this.productServiceUrl);
		
		//Recommendation-service
		sb = new StringBuilder("http://");
		sb.append(recommendationServiceHost).append(":").append(recommendationServicePort).append(basePath).
		append("/").append(Api.RECOMMENDATION_PATH);
		
		this.recommendationServiceUrl = sb.toString();
		log.debug("L'URL du service recommendation : " + this.recommendationServiceUrl);
		
		//Review-service
		sb = new StringBuilder("http://");
		sb.append(reviewServiceHost).append(":").append(reviewServicePort).append(basePath).
		append("/").append(Api.REVIEW_PATH);
		
		this.reviewServiceUrl = sb.toString();
		log.debug("L'URL du service review : " + this.reviewServiceUrl);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Product> getProduct(Integer id) {
		
		try {
			
			log.debug("Call product services ({}) : {} - path variable : {}", HttpMethod.GET.name(), productServiceUrl, id);
			
			return restTemplate.getForEntity(this.productServiceUrl + "/" + id, Product.class);
		}
		catch(HttpClientErrorException e) {
			
			log.error(e.getMessage(), e);
			throw handleHttpClientException(e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Paged<Product>> findByName(String name, Integer pageNumber, Integer pageSize) {
		
		try {
			
			StringBuilder params = new StringBuilder();
			params.append("?name=").append(name);
			
			if(pageNumber != null) 	params.append("&pageNumber=").append(pageNumber);
			if(pageSize != null)	params.append("&pageSize=").append(pageSize);
			
			log.debug("Call product services ({}) : {} - parameters : {}", HttpMethod.GET.name(), productServiceUrl, 
					params.toString());
			
			return restTemplate.exchange(productServiceUrl + params.toString(), HttpMethod.GET, null, 
					new ParameterizedTypeReference<Paged<Product>>() {});
		}
		catch(HttpClientErrorException e) {
			
			log.error(e.getMessage(), e);
			throw handleHttpClientException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Product> save(Product product) {
		
		try {
			
			log.debug("Call product services ({}) : {} - body : {}", HttpMethod.POST.name(), productServiceUrl, 
					product.toString());
			
			return restTemplate.exchange(productServiceUrl, HttpMethod.POST, new HttpEntity<>(product), Product.class);
		}
		catch(HttpClientErrorException e) {
			
			log.error(e.getMessage(), e);
			throw handleHttpClientException(e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Product> update(Product product, Integer productID) {
		
		try {
			
			log.debug("Call product services ({}) : {} - body : {} - path variable : {}", HttpMethod.PUT.name(), 
					productServiceUrl, product.toString(), productID);
			
			return restTemplate.exchange(productServiceUrl + "/" + productID, HttpMethod.PUT, new HttpEntity<>(product), Product.class);
		}
		catch(HttpClientErrorException e) {
			
			log.error(e.getMessage(), e);
			throw handleHttpClientException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteProduct(Integer productID) {
		
		try {
			
			log.debug("Call product services ({}) : {} - path variable : {}", HttpMethod.DELETE.name(), productServiceUrl, 
					productID);
			
			restTemplate.delete(productServiceUrl + "/" + productID);
		}
		catch(HttpClientErrorException e) {
			
			log.error(e.getMessage(), e);
			throw handleHttpClientException(e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Review> getReview(Integer reviewID) {
		
		try {
			
			log.debug("Call review services ({}) : {} - path variable : {}", HttpMethod.GET.name(), reviewServiceUrl, 
					reviewID);
			
			return this.restTemplate.getForEntity(reviewServiceUrl + "/" + reviewID, Review.class);
		}
		catch(HttpClientErrorException e) {
			
			log.error(e.getMessage(), e);
			throw handleHttpClientException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Paged<Review>> getReviewByProductId(Integer productID, Integer pageNumber, Integer pageSize) {
		
		try {
			
			StringBuilder params = new StringBuilder();
			params.append("?productId=").append(productID);
			
			if(pageNumber != null) 	params.append("&pageNumber=").append(pageNumber);
			if(pageSize != null)	params.append("&pageSize=").append(pageSize);
			
			log.debug("Call review services ({}) : {} - parameters : {}", HttpMethod.GET.name(), reviewServiceUrl, 
					params.toString());
			
			ResponseEntity<Paged<Review>> result =  restTemplate.exchange(reviewServiceUrl + params.toString(), HttpMethod.GET, 
					null, new ParameterizedTypeReference<Paged<Review>>() {});  
			
			return result;
		}
		catch(HttpClientErrorException e) {
			
			log.error(e.getMessage(), e);
			throw handleHttpClientException(e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Review> save(Review review) {
		
		try {
			
			log.debug("Call review services ({}) : {} - body : {}", HttpMethod.POST.name(), reviewServiceUrl, review.toString());
			
			return restTemplate.postForEntity(reviewServiceUrl, review, Review.class);
		}
		catch(HttpClientErrorException e) {
			
			log.error(e.getMessage(), e);
			throw handleHttpClientException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Review> update(Review review, Integer reviewID) {
		
		try {
			
			log.debug("Call review services ({}) : {} - body : {} - path variable : {}", HttpMethod.PUT.name(), 
					reviewServiceUrl, review.toString(), reviewID);
			
			return restTemplate.exchange(reviewServiceUrl + "/" + reviewID, HttpMethod.PUT, new HttpEntity<>(review), Review.class);
		}
		catch(HttpClientErrorException e) {
			
			log.error(e.getMessage(), e);
			throw handleHttpClientException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteReview(Integer reviewID) {
		
		try {
			
			log.debug("Call review services ({}) : {} - path variable : {}", HttpMethod.DELETE.name(), 
					reviewServiceUrl, reviewID);
			
			restTemplate.delete(reviewServiceUrl + "/" + reviewID);
		}
		catch(HttpClientErrorException e) {
			
			log.error(e.getMessage(), e);
			throw handleHttpClientException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Recommendation> getRecommendation(Integer recommendationID) {
		
		try {
			
			log.debug("Call recommendation services ({}) : {} - path variable : {}", HttpMethod.GET.name(), 
					recommendationServiceUrl, recommendationID);
			
			return this.restTemplate.getForEntity(recommendationServiceUrl + "/" + recommendationID, Recommendation.class);
		}
		catch(HttpClientErrorException e) {
			
			log.error(e.getMessage(), e);
			throw handleHttpClientException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Paged<Recommendation>> getRecommendationByProductId(Integer productID, Integer pageNumber, Integer pageSize) {
		
		try {
			
			StringBuilder params = new StringBuilder();
			params.append("?productId=").append(productID);
			
			if(pageNumber != null) 	params.append("&pageNumber=").append(pageNumber);
			if(pageSize != null)	params.append("&pageSize=").append(pageSize);
			
			log.debug("Call recommendation services ({}) : {} - parameters : {}", HttpMethod.GET.name(), recommendationServiceUrl, 
					params.toString());
			
			return restTemplate.exchange(recommendationServiceUrl + params.toString(), HttpMethod.GET, 
					null, new ParameterizedTypeReference<Paged<Recommendation>>() {});
		}
		catch(HttpClientErrorException e) {
			
			log.error(e.getMessage(), e);
			throw handleHttpClientException(e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Recommendation> save(Recommendation recommendation) {
		
		try {
			
			log.debug("Call recommendation services ({}) : {} - body : {}", HttpMethod.POST.name(), recommendationServiceUrl, 
					recommendation.toString());
			
			return restTemplate.postForEntity(recommendationServiceUrl, recommendation, Recommendation.class);
		}
		catch(HttpClientErrorException e) {
			
			log.error(e.getMessage(), e);
			throw handleHttpClientException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Recommendation> update(Recommendation recommendation, Integer recommendationID) {
		
		try {
			
			log.debug("Call recommendation services ({}) : {} - body : {} - path variable : {}", HttpMethod.PUT.name(), 
					recommendationServiceUrl, recommendation.toString(), recommendationID);
			
			return restTemplate.exchange(recommendationServiceUrl + "/" + recommendationID, HttpMethod.PUT, 
					new HttpEntity<Recommendation>(recommendation), Recommendation.class);
		}
		catch(HttpClientErrorException e) {
			
			log.error(e.getMessage(), e);
			throw handleHttpClientException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteRecommendation(Integer recommendationID) {
		
		try {
			
			log.debug("Call recommendation services ({}) : {} - path variable : {}", HttpMethod.DELETE.name(), 
					recommendationServiceUrl, recommendationID);
			
			restTemplate.delete(recommendationServiceUrl + "/" + recommendationID);
		}
		catch(HttpClientErrorException e) {
			
			log.error(e.getMessage(), e);
			throw handleHttpClientException(e);
		}
	}

	/**
	 * @param ex
	 * @return {@link RuntimeException}
	 */
	private RuntimeException handleHttpClientException(HttpClientErrorException e) {
		
		switch(e.getStatusCode()) {
		
			case NOT_FOUND:
				return new NotFoundException(getMessage(e));
	
			case UNPROCESSABLE_ENTITY:
				return new InvalidInputException(getMessage(e));
				
			default:
				log.warn("Got a unexpected http error: {}", e.getStatusCode());
				log.warn("{}", e.getResponseBodyAsString());
				return e;
		}
	}
	
	/**
	 * @param ex
	 * @return the error message
	 */
	private String getMessage(HttpClientErrorException ex) {
		
		try {
			HttpErrorInfo info = this.jack.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class);
			return info.getMessage();
		}
		catch(IOException io) {
			return ex.getMessage();
		}
	}
}