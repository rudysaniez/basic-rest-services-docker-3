package com.me.api.composite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@Deprecated
@Data @AllArgsConstructor
public class ReviewSummary {

	private Integer reviewID;
	
	@Exclude
	private String author;
	
	@Exclude
	private String subject;
	
	@Exclude
	private String content;
	
	public ReviewSummary() {
		
		this.reviewID = 0;
		this.author = null;
		this.subject = null;
		this.content = null;
	}
}
