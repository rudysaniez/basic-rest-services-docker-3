package com.me.work.example.api.core.composite;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.me.work.example.api.Api;

public interface ProductCompositeService {

	/**
	 * @param id
	 * @return {@link ProductAggregate}
	 */
	@GetMapping(value=Api.PRODUCT_COMPOSITE_PATH + "/{productId}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProductAggregate> getCompositeProduct(@PathVariable(name="productId", required=true) Integer productID,
			@RequestParam(name="pageNumber", required=false) Integer pageNumber, 
			@RequestParam(name="pageSize", required=false) Integer pageSize);
	
	/**
	 * @param body
	 */
	@PostMapping(value=Api.PRODUCT_COMPOSITE_PATH, consumes=MediaType.APPLICATION_JSON_VALUE)
	public void createCompositeProduct(@RequestBody ProductComposite body);
	
	/**
	 * @param productID
	 */
	@DeleteMapping(value=Api.PRODUCT_COMPOSITE_PATH + "/{productId}")
	public void deleteCompositeProduct(@PathVariable(name="productId", required=true) Integer productID);
}
