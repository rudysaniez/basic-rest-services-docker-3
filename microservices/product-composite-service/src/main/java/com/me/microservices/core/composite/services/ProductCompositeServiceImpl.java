package com.me.microservices.core.composite.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.me.api.core.common.Paged;
import com.me.api.core.composite.ProductAggregate;
import com.me.api.core.composite.ProductComposite;
import com.me.api.core.composite.ProductCompositeService;
import com.me.api.core.product.Product;
import com.me.api.core.recommendation.Recommendation;
import com.me.api.core.review.Review;
import com.me.microservices.core.composite.Application.PaginationInformation;
import com.me.microservices.core.composite.mapper.PagedMapper;
import com.me.microservices.core.composite.mapper.RecommendationMapper;
import com.me.microservices.core.composite.mapper.ReviewMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author rudysaniez @since 0.0.1
 */
@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

	@Autowired private ProductCompositeIntegration integration;
	@Autowired private PagedMapper pagedMapper;
	@Autowired private RecommendationMapper recommendationMapper;
	@Autowired private ReviewMapper reviewMapper;
	@Autowired private PaginationInformation pagination;
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public Mono<ProductAggregate> getCompositeProduct(Integer productID, Integer pageNumber, Integer pageSize) {
		
		if(pageNumber == null) pageNumber = pagination.getPageNumber();
		if(pageSize == null) pageSize = pagination.getPageSize();
		
		return Mono.zip(values -> createProductAggregate((Product)values[0], (Paged<Recommendation>)values[1], (Paged<Review>)values[2]), 
				integration.getProduct(productID),
				integration.getRecommendationByProductId(productID, pageNumber, pageSize),
				integration.getReviewByProductId(productID, pageNumber, pageSize)).log();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@ResponseStatus(value=HttpStatus.CREATED)
	@Override
	public Mono<ProductComposite> createCompositeProduct(ProductComposite body) {
		
		return Mono.zip(values -> createProductComposite((Product)values[0], (List<Recommendation>)values[1], (List<Review>)values[2]), 
				
					integration.save(new Product(body.getProductID(), body.getName(), body.getWeight())),
					
					Flux.fromIterable(body.getRecommendations()).
					map(recommendationMapper::toMsModel).map(r -> {r.setProductID(body.getProductID());return r;}).
					flatMap(r -> integration.save(r)).buffer().single(),
					
					Flux.fromIterable(body.getReviews()).
					map(reviewMapper::toMsModel).map(r -> {r.setProductID(body.getProductID());return r;}).
					flatMap(r -> integration.save(r)).buffer().single()
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public Mono<Void> deleteCompositeProduct(Integer productID) {
		throw new UnsupportedOperationException("An event will be sent (Asynchronous event-driven).");
	}

	/**
	 * @return {@link ProductAggregate}
	 */
	private ProductAggregate createProductAggregate(Product msProduct, Paged<Recommendation> pageOfMsRecommendation, 
			Paged<Review> pageOfMsReview) {
		
		return new ProductAggregate(msProduct.getProductID(), msProduct.getName(), msProduct.getWeight(), 
				pagedMapper.toPageRecommendationModel(pageOfMsRecommendation), 
				pagedMapper.toPageReviewModel(pageOfMsReview));
	}
	
	/**
	 * @param product
	 * @param listOfMsRecommendations
	 * @param listOfMsReview
	 * @return {@link ProductComposite}
	 */
	private ProductComposite createProductComposite(Product product, List<Recommendation> listOfMsRecommendations, List<Review> listOfMsReview) {
		
		return new ProductComposite(product.getProductID(), product.getName(), product.getWeight(), 
				recommendationMapper.toListOfModel(listOfMsRecommendations), 
				reviewMapper.toListOfModel(listOfMsReview));
	}
}
