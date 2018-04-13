package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Transfer {

	  @NotNull
	  @NotEmpty
	  private final String toAccountId;
	  
	  @NotNull
	  @NotEmpty
	  private final String fromAccountId;

	  @NotNull
	  @Min(value = 0, message = "Amount must be positive.")
	  private BigDecimal amount;

	  public Transfer(String toAccountId, String fromAccountId) {
	    this.toAccountId = toAccountId;
	    this.fromAccountId = fromAccountId;
	  }

	  @JsonCreator
	  public Transfer(@JsonProperty("toAccountId") String toAccountId, @JsonProperty("fromAccountId") String fromAccountId,
	    @JsonProperty("amount") BigDecimal amount) {
		  	this.toAccountId = toAccountId;
		    this.fromAccountId = fromAccountId;
		    this.amount = amount;
	  }
	
}
