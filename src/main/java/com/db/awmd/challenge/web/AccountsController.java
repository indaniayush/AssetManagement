package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.exception.SameAccountIdException;
import com.db.awmd.challenge.service.AccountsService;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController implements Runnable{

  private final AccountsService accountsService;
  
  private Transfer transfer;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }
  
  @RequestMapping("/instantTransfer")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> instantTransfer(@RequestBody @Valid Transfer transfer) {
	log.info("Initiating transfer {}", transfer);
	this.transfer = transfer;
    try {
    	/*
    	 * Creating a thread based implementation, to add dynamic threads 
    	 * for processing multiple request in the existing application.
    	 * */
    	
    	Thread t1 = new Thread(this);
    	t1.start(); // would invoke run method on 'this' class via thread 't1'
    	
    } catch (SameAccountIdException sameaccounts) {
      return new ResponseEntity<>(sameaccounts.getMessage(), HttpStatus.BAD_REQUEST);
    }catch (InsufficientFundsException fundsException) {
        return new ResponseEntity<>(fundsException.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

@Override
public void run() {
	// calling instant transfer on thread 't1' to perform transfer operation.
	this.accountsService.instantTransfer(transfer);
}

}
